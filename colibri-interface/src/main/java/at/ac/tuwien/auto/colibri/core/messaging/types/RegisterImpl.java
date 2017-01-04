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

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;

import at.ac.tuwien.auto.colibri.core.messaging.Config;
import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.IllegalContentException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;

public class RegisterImpl extends MessageImpl implements Register, Message
{
	public RegisterImpl()
	{
		super();

		this.setConfirmable(true);
	}

	@Override
	public String getMessageType()
	{
		return "REG";
	}

	@Override
	public void process(Datastore store, Registry registry) throws InterfaceException
	{
		// check if peer is already registered
		if (registry.getConnector(this.getPeer()) != null)
			throw new ProcessingException("peer is already registered in connector registry", this);

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
					throw new ProcessingException("content type " + this.getContentType() + " is not supported for message type + " + this.getMessageType(), this.getPeer());
			}
		}
		catch (Exception e)
		{
			throw new SyntaxException("content cannot be parsed", this);
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
		{
			model.add(plain);
		}
		else
		{
			model = plain;
		}

		// technology connector class
		RDFNode n = model.createResource(Config.getInstance().namespace + "TechnologyConnector");

		// get all resources of type technology connector
		ResIterator iter = model.listSubjectsWithProperty(RDF.type, n);

		// model to store
		Model result = ModelFactory.createDefaultModel();
		URI uri = null;

		// get technology connector
		if (iter.hasNext())
		{
			Resource i = iter.next();
			Resource r = plain.getResource(i.getURI());

			// check resource
			if (r == null)
				throw new ProcessingException("resource " + i.getURI() + " cannot be found.", this);

			// check URI
			try
			{
				uri = new URI(r.getURI());
			}
			catch (URISyntaxException e)
			{
				throw new SyntaxException("URI is not valid (" + r.getURI() + ")", this);
			}

			// // do not check if URI exists in ontology as connector can reregister after shutdown
			// of system
			// boolean exists = false;
			// try
			// {
			// String query = QueryBuilder.getPrefixedQuery("SELECT ?s WHERE { ?s rdf:type
			// colibri:TechnologyConnector. FILTER (?s = <" + uri.toString() + ">)}");
			// exists = store.exists(query);
			// }
			// catch (Exception e)
			// {
			// throw new DatastoreException(e, this);
			// }
			//
			// if (exists)
			// throw new ObjectExistsException("URI is already used (" + r.getURI() + ")", this);

			// add technology connector to result model
			result.add(r.listProperties());
		}
		else
		{
			// no connector was found
			throw new IllegalContentException("no technology connector is defined in message content", this);
		}

		// only one technology connector is allowed
		if (iter.hasNext())
		{
			throw new IllegalContentException("only one technology connector is allowed", this);
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

		// add connector to registry
		registry.addConnector(this.getPeer(), uri);

		// send status message
		StatusImpl status = new StatusImpl();

		status.setPeer(this.getPeer());
		status.setReference(this);

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(status);
	}
}
