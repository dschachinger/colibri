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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import at.ac.tuwien.auto.colibri.core.datastore.reasoner.Reasoner;
import at.ac.tuwien.auto.colibri.core.datastore.reasoner.ReasonerLevel;
import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.messaging.Config;
import at.ac.tuwien.auto.colibri.messaging.Registry;
import at.ac.tuwien.auto.colibri.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.IllegalContentException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.messaging.types.Register;

public class RegisterImpl extends Register implements Processible
{
	public void process(Datastore store) throws InterfaceException
	{
		// check if peer is already registered
		if (Registry.getInstance().getConnector(this.getPeer()) != null)
			throw new ProcessingException("peer is already registered in connector registry", this);

		// create model for querying
		Model model = this.getModel();

		// do reasoning
		if (Config.getInstance().reasoner)
		{
			OntModel ontm = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
			Ontology o = ontm.createOntology("http://temp");
			o.addImport(ontm.createResource(Config.getInstance().ontology));
			model = ontm;

			try
			{
				Reasoner reasoner = new Reasoner(model);
				Model inferredModel = reasoner.doReasoning(ReasonerLevel.REASONING_MINIMAL, false);
				model = model.union(inferredModel);
			}
			catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e)
			{
				throw new ProcessingException("Internal reasoning error (" + e.getMessage() + ")", this);
			}
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

			// check URI
			try
			{
				uri = new URI(i.getURI());
			}
			catch (URISyntaxException e)
			{
				throw new SyntaxException("URI is not valid (" + i.getURI() + ")", this);
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
			result.add(i.listProperties());
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
		Registry.getInstance().addConnector(this.getPeer(), uri);

		// send status message
		StatusImpl status = new StatusImpl();

		status.setPeer(this.getPeer());
		status.setReference(this);

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(status);
	}
}
