/**************************************************************************************************
 * Copyright (c) 2016, Automation Systems Group, Institute of Computer Aided Automation, TU Wien
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *************************************************************************************************/

package at.ac.tuwien.auto.colibri.core.messaging.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;

import at.ac.tuwien.auto.colibri.core.messaging.Config;
import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.IllegalContentException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InvalidObjectException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;

public class AddImpl extends MessageApprovedImpl implements Add, Message
{
	public AddImpl()
	{
		super();
		this.setConfirmable(true);
	}

	@Override
	public String getMessageType()
	{
		return "ADD";
	}

	@Override
	public void process(Datastore store, Registry registry) throws InterfaceException
	{
		super.process(store, registry);

		// create model for querying
		Model model = null;

		// create plain model without resoner
		Model plain = ModelFactory.createDefaultModel();

		if (Config.getInstance().reasoner)
		{
			// creating reasoner
			Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
			reasoner = reasoner.bindSchema(registry.getOntology());

			// create ontology model specification
			OntModelSpec ontModelSpec = OntModelSpec.RDFS_MEM_RDFS_INF;
			ontModelSpec.setReasoner(reasoner);

			// create inferred model with RDFS reasoner
			model = ModelFactory.createOntologyModel(ontModelSpec);
		}

		// create input stream
		InputStream is = new ByteArrayInputStream(this.getContent().getBytes(StandardCharsets.UTF_8));

		try
		{
			// read model from content
			switch (this.getContentType())
			{
				case RDF_XML:
					plain.read(is, null, "RDF/XML");
					break;
				case TURTLE:
					plain.read(is, null, "TURTLE");
					break;
				default:
					throw new ProcessingException("Content type " + this.getContentType() + " is not supported for message type + " + this.getMessageType(), this.getPeer());
			}
		}
		catch (Exception e)
		{
			throw new SyntaxException("Content cannot be parsed", this);
		}

		try
		{
			// close input stream
			is.close();
		}
		catch (IOException e)
		{
			throw new ProcessingException(e.getMessage(), this);
		}

		// set query model
		if (Config.getInstance().reasoner)
			model.add(plain);
		else
			model = plain;

		// technology connector class
		RDFNode service = model.createResource(Config.getInstance().namespace + "Service");
		Property hasConfiguration = model.createProperty(Config.getInstance().namespace + "hasDataConfiguration");
		Property hasTechnologyConnector = model.createProperty(Config.getInstance().namespace + "hasTechnologyConnector");

		// get all resources of type service
		ResIterator iter = model.listSubjectsWithProperty(RDF.type, service);

		// model to store
		Model result = ModelFactory.createDefaultModel();

		// list of observe message
		List<Observe> observes = new ArrayList<Observe>();

		if (!iter.hasNext())
		{
			// no service was found
			throw new IllegalContentException("no service is defined in message content", this);
		}

		// get services
		while (iter.hasNext())
		{
			Resource i = iter.next();
			Resource r = plain.getResource(i.getURI());

			// check resource
			if (r == null)
				throw new ProcessingException("resource " + i.getURI() + " cannot be found.", this);

			// check URI validity
			URI uri = null;
			try
			{
				uri = new URI(r.getURI().trim());
			}
			catch (URISyntaxException e)
			{
				throw new SyntaxException("URI is not valid (" + r.getURI() + ")", this);
			}

			// // do not check if service URI exists in ontology as service can be re-added
			// boolean exists = false;
			// try
			// {
			// String query = QueryBuilder.getPrefixedQuery("SELECT ?s WHERE { ?s rdf:type
			// colibri:Service. FILTER (?s = <" + uri.toString() + ">)}");
			// exists = store.exists(query);
			// }
			// catch (Exception e)
			// {
			// throw new DatastoreException(e, this);
			// }
			//
			// if (exists)
			// throw new ObjectExistsException("URI is already used (" + r.getURI() + ")", this);

			// add service to result model
			result.add(r.listProperties());

			// check technology connector
			StmtIterator tcIter = i.listProperties(hasTechnologyConnector);

			if (!tcIter.hasNext())
			{
				// no service was found
				throw new IllegalContentException("no technology connector is defined for service " + uri.toString(), this);
			}
			else
			{
				Resource tc = tcIter.nextStatement().getResource();

				// check if technology connector URI exists in ontology
				boolean exists = false;
				try
				{
					String query = QueryBuilder.getPrefixedQuery("SELECT ?s WHERE { ?s  rdf:type colibri:TechnologyConnector. FILTER (?s = <" + tc.getURI() + ">)}");
					exists = store.exists(query);
				}
				catch (Exception e)
				{
					throw new DatastoreException(e, this);
				}

				if (exists)
					throw new UnknownObjectException("technology connector URI is not known (" + tc.getURI() + ")", this);

				// check permission
				URI temp = registry.getConnector(this.getPeer());

				if (temp == null || !temp.toString().toUpperCase().equals(tc.getURI().toUpperCase()))
					throw new InvalidObjectException("peer can only add services to its own technology connector", this);
			}

			if (tcIter.hasNext())
			{
				// too many configurations
				throw new IllegalContentException("only one technology connector can be defined for service " + uri.toString(), this);
			}

			// add data configuration
			List<Resource> configurations = new ArrayList<Resource>();

			for (Statement s : i.listProperties(hasConfiguration).toList())
			{
				configurations.add(s.getObject().asResource());
			}

			if (configurations.size() == 0)
			{
				// no service was found
				throw new IllegalContentException("no data configuration is defined for service " + uri.toString(), this);
			}
			if (configurations.size() > 1)
			{
				// too many configurations
				throw new IllegalContentException("only one data configuration can be defined for service " + uri.toString(), this);
			}

			// TODO read and store control Variations and states of control services/data services
			// (state parameter)

			this.addDataConfiguration(configurations, hasConfiguration, plain, store, result);

			// create observe message
			ObserveImpl observe = new ObserveImpl();
			observe.setPeer(this.getPeer());
			observe.setContent(uri.toString());
			observes.add(observe);
		}

		// add connector to datastore
		try
		{
			store.insert(result);
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		// create status message
		StatusImpl status = new StatusImpl();
		status.setPeer(this.getPeer());
		status.setReference(this);

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(status);

		// send observe message
		// for (Observe o : observes)
		// {
		// TODO observe only data services, use management tool for specifying observed services
		// QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(o);
		// }
	}

	private void addDataConfiguration(List<Resource> configurations, Property hasConfiguration, Model plain, Datastore store, Model result) throws InterfaceException
	{
		Property hasParameter = plain.createProperty(Config.getInstance().namespace + "hasParameter");

		for (Resource c : configurations)
		{
			Resource configuration = plain.getResource(c.getURI());

			// // check if data configuration URI exists in ontology
			// boolean exists = false;
			// try
			// {
			// String query = QueryBuilder.getPrefixedQuery("SELECT ?s WHERE { ?s ?p ?o. FILTER (?s
			// = <" + c.getURI() + ">)}");
			// exists = store.exists(query);
			// }
			// catch (Exception e)
			// {
			// throw new DatastoreException(e, this);
			// }
			//
			// if (exists)
			// throw new ObjectExistsException("URI is already used (" + c.getURI() + ")", this);

			// add resource
			result.add(configuration.listProperties());

			// add parameter configuration
			StmtIterator paramIter = c.listProperties(hasParameter);

			int count = 0;
			while (paramIter.hasNext())
			{
				Resource p = paramIter.nextStatement().getResource();
				Resource parameter = plain.getResource(p.getURI());

				// // check if parameter URI exists in ontology
				// exists = false;
				// try
				// {
				// String query = QueryBuilder.getPrefixedQuery("SELECT ?s WHERE { ?s ?p ?o. FILTER
				// (?s = <" + p.getURI() + ">)}");
				// exists = store.exists(query);
				// }
				// catch (Exception e)
				// {
				// throw new DatastoreException(e, this);
				// }
				//
				// if (exists)
				// throw new ObjectExistsException("URI is already used (" + p.getURI() + ")",
				// this);

				// add resource
				result.add(parameter.listProperties());

				count++;
			}

			if (count == 0)
			{
				// no service was found
				throw new IllegalContentException("no parameter is defined for data configuration " + c.getURI(), this);
			}

			if (count >= 3)
			{
				// too many configurations
				throw new IllegalContentException("only 2 parameters can be defined for data configuration " + c.getURI(), this);
			}

			// add sub data configuration
			List<Resource> subs = new ArrayList<Resource>();

			for (Statement s : c.listProperties(hasConfiguration).toList())
				subs.add(s.getObject().asResource());

			this.addDataConfiguration(subs, hasConfiguration, plain, store, result);
		}
	}
}
