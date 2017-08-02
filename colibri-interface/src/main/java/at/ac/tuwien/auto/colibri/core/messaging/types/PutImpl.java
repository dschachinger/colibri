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
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import at.ac.tuwien.auto.colibri.core.datastore.reasoner.Reasoner;
import at.ac.tuwien.auto.colibri.core.datastore.reasoner.ReasonerLevel;
import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.Observations;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.core.messaging.tasks.Observation;
import at.ac.tuwien.auto.colibri.messaging.Config;
import at.ac.tuwien.auto.colibri.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InconsistencyException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.messaging.types.Put;

public class PutImpl extends Put implements Processible
{
	public void process(Datastore store) throws InterfaceException
	{
		// create model for querying
		Model model = this.getModel();

		// TODO: reasoning bei put wegnehmen!

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

		// model to store
		Model result = ModelFactory.createDefaultModel();

		// types and properties
		Resource rDataValue = result.createResource(Config.getInstance().namespace + "DataValue");
		Resource rValue = result.createResource(Config.getInstance().namespace + "Value");
		Property pHasValue = result.createProperty(Config.getInstance().namespace + "hasValue");
		Property pHasDataValue = result.createProperty(Config.getInstance().namespace, "hasDataValue");
		Property pHasParameter = result.createProperty(Config.getInstance().namespace + "hasParameter");

		// get all resources of type technology connector
		List<Resource> dataValues = model.listSubjectsWithProperty(RDF.type, rDataValue).toList();

		HashMap<String, Model> addedServices = new HashMap<String, Model>();

		if (dataValues.size() == 0)
		{
			List<Resource> values = model.listSubjectsWithProperty(RDF.type, rValue).toList();

			if (values.size() != 2)
				throw new InconsistencyException("only two values are allowed without a data value resource", this);

			// get service
			String service = this.getService(values, pHasParameter, store);

			// add observe model
			if (!addedServices.containsKey(service))
			{
				addedServices.put(service, ModelFactory.createDefaultModel());
			}

			// get observe model
			Model temp = addedServices.get(service);

			// update models: data value and service
			String datavalueUri = service + "_dv_intern_" + System.currentTimeMillis();

			Resource datavalue = result.createResource(datavalueUri);

			result.add(datavalue, RDF.type, rDataValue);
			result.add(result.createResource(service), pHasDataValue, datavalue);

			temp.add(datavalue, RDF.type, rDataValue);
			temp.add(result.createResource(service), pHasDataValue, datavalue);

			// update models: values
			for (Resource r : values)
			{
				result.add(r.listProperties());
				temp.add(r.listProperties());

				result.add(datavalue, pHasValue, r);
			}
		}
		else
		{
			// add each data value
			for (int i = 0; i < dataValues.size(); i++)
			{
				Resource v = dataValues.get(i);

				// get values to data value
				List<Resource> values = new ArrayList<Resource>();

				for (Statement s : v.listProperties(pHasValue).toList())
				{
					values.add(s.getObject().asResource());
				}

				// get service
				String service = this.getService(values, pHasParameter, store);

				// add observe model
				if (!addedServices.containsKey(service))
				{
					addedServices.put(service, ModelFactory.createDefaultModel());
				}

				// get observe model
				Model temp = addedServices.get(service);

				// update models: data value and service
				result.add(v.listProperties());
				result.add(result.createResource(service), pHasDataValue, v);

				temp.add(v.listProperties());
				temp.add(result.createResource(service), pHasDataValue, v);

				// update models: values
				for (Resource r : values)
				{
					result.add(r.listProperties());
					temp.add(r.listProperties());
				}
			}
		}

		// Dataset dataset = DatasetFactory.create() ;
		// dataset.setDefaultModel(model) ;
		// dataset.addNamedModel("http://example/named-1", modelX) ;
		// dataset.addNamedModel("http://example/named-2", modelY) ;
		// try(QueryExecution qExec = QueryExecutionFactory.create(query, dataset)) {
		// ...
		// }

		// add connector to datastore
		try
		{
			store.insert(result);
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		// send put messages to observers
		for (String service : addedServices.keySet())
		{
			try
			{
				Model m = addedServices.get(service);

				List<Observation> observes = null;

				observes = Observations.getInstance().getObservations(new URI(service), false);

				// write model
				StringWriter sw = new StringWriter();
				m.write(sw, "RDF/XML");
				String content = sw.toString();
				sw.close();

				for (Observation o : observes)
				{
					// create put message
					PutImpl put = new PutImpl();

					put.setContent(content);
					// put.setContent(this.getContent());

					put.setPeer(o.getObserve().getPeer());
					put.setContentType(ContentType.RDF_XML);
					put.setReference(o.getObserve());

					// send put message
					QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(put);
				}

			}
			catch (URISyntaxException | IOException e)
			{
				throw new ProcessingException(e.getMessage(), this);
			}

		}

		// send status message
		StatusImpl status = new StatusImpl();

		status.setPeer(this.getPeer());
		status.setReference(this);

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(status);
	}

	private String getService(List<Resource> values, Property pHasParameter, Datastore store) throws InterfaceException
	{
		// check value count
		if (values.size() != 2)
			throw new InconsistencyException("an even number of values is required", this);

		// service URI
		String service = null;

		// compare services of both parameters of the values
		for (int j = 0; j < values.size(); j++)
		{
			// get parameter
			List<Statement> parameters = values.get(j).listProperties(pHasParameter).toList();

			if (parameters.size() != 1)
				throw new InconsistencyException("a value needs an assigned parameter", this);

			String parameterUri = parameters.get(0).getObject().asResource().getURI();

			String query = "select ?s "
					+ "where { "
					+ "?c colibri:hasParameter ?p. "
					+ "?s colibri:hasDataConfiguration ?c."
					+ "?s rdf:type colibri:DataService. "
					+ "FILTER (?p = <" + parameterUri + ">) "
					+ "}";

			ResultSet rs = null;
			try
			{
				rs = store.select(QueryBuilder.getPrefixedQuery(query));
			}
			catch (Exception e)
			{
				throw new DatastoreException(e, this);
			}

			// read and compare service
			if (rs.hasNext())
			{
				QuerySolution s = rs.nextSolution();

				if (service == null)
					service = s.get("s").toString();
				else if (!service.toUpperCase().equals(s.get("s").toString().toUpperCase()))
					throw new InconsistencyException("values have parameters of different services (" + service + "," + s.get("s").toString(), this);
			}
			else
			{
				throw new UnknownObjectException("no service was found for the parameter " + parameterUri, this);
			}

			if (rs.hasNext())
			{
				throw new UnknownObjectException("more than one service was found for parameter " + parameterUri, this);
			}

		}
		return service;
	}
}
