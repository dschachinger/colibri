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

package at.ac.tuwien.auto.colibri.core.datastore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.postgresql.ds.PGPoolingDataSource;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;

public class ColibriDatastore implements Datastore
{
	private static final Logger log = Logger.getLogger(ColibriDatastore.class.getName());

	private Ontology ontology = null;
	private DatabasePostgre database = null;
	private HashMap<Parameters, DataConfigurationMapping> mappings = null;
	private boolean databaseEnabled = false;

	public ColibriDatastore(DataSource ds)
	{
		log.info("Starting Colibri data store ...");

		// TODO: temp
		try
		{
			File tdbDir = new File(Config.getInstance().tdbDirectory);
			if (tdbDir.exists())
				FileUtils.deleteDirectory(tdbDir);
		}
		catch (IOException e)
		{			
			e.printStackTrace();
		}

		ontology = new Ontology();
		database = new DatabasePostgre(ds);
		mappings = new HashMap<Parameters, DataConfigurationMapping>();

		databaseEnabled = Config.getInstance().databaseEnabled;

		log.info("DB: " + databaseEnabled);

		if (!database.initialDatabaseSetup())
			log.severe("Initializing database failed");

		if (!readMappings())
		{
			log.severe("Reading Mappings from database failed");
			mappings.clear();
		}
	}

	/**
	 * Read mappings from database and write them to internal hashmap
	 * 
	 * @return True if mappings were read successfully, otherwise false
	 */
	private boolean readMappings()
	{
		ArrayList<DataConfigurationMapping> mappingList = null;
		try
		{
			mappingList = database.getAllMappings();
		}
		catch (RuntimeException e)
		{
			return false;
		}

		for (DataConfigurationMapping m : mappingList)
		{
			if (m.getParameterURIs().length != 2)
				return false;

			mappings.put(new Parameters(m.getParameterURIs()[0], m.getParameterURIs()[1]), m);
		}
		return true;
	}

	/**
	 * Adds triples to hybrid store. DataValues are written into the database, while all the rest is
	 * stored
	 * in the ontology.
	 *
	 * @param data Dataset containing the triples to insert
	 * @throws InsertDataValueException
	 */
	private void processTriples(Dataset data) throws InsertDataValueException
	{
		HashMap<String, DataValue> dataValueMap = new HashMap<String, DataValue>();
		ArrayList<DataValue> topDataValues = new ArrayList<DataValue>();
		ArrayList<String> failedDataValues = new ArrayList<String>();
		Node nType = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Node nDataValue = NodeFactory.createURI(Config.getInstance().colibriModelUri + "#DataValue");
		Node nHasValue = NodeFactory.createURI(Config.getInstance().colibriModelUri + "#hasValue");
		Node nValue = NodeFactory.createURI(Config.getInstance().colibriModelUri + "#value");
		Node nHasParameter = NodeFactory.createURI(Config.getInstance().colibriModelUri + "#hasParameter");
		Node nHasDataValue = NodeFactory.createURI(Config.getInstance().colibriModelUri + "#hasDataValue");

		// Get instances of type DataValue
		Iterator<Quad> datavalues = data.asDatasetGraph().find(Node.ANY, Node.ANY, nType, nDataValue);
		if (datavalues != null)
		{
			while (datavalues.hasNext())
			{
				Quad dv = datavalues.next();

				// Get Values for the DataValue
				Iterator<Quad> values = data.asDatasetGraph().find(Node.ANY, dv.getSubject(), nHasValue, Node.ANY);
				Map<String, String> paramvalues = new HashMap<String, String>();
				while (values != null && values.hasNext())
				{
					Quad v = values.next();
					String parameter = null;
					String value = null;

					// Get value literal for Value
					Iterator<Quad> valueLits = data.asDatasetGraph().find(Node.ANY, v.getObject(), nValue, Node.ANY);
					if (valueLits != null && valueLits.hasNext())
					{
						Quad vl = valueLits.next();
						Node n = vl.getObject();
						if (n.isLiteral())
						{
							value = n.getLiteral().getLexicalForm();
						}
					}

					// Get parameter for Value
					Iterator<Quad> params = data.asDatasetGraph().find(Node.ANY, v.getObject(), nHasParameter, Node.ANY);
					if (params != null && params.hasNext())
					{
						Quad p = params.next();
						parameter = p.getObject().getURI();
					}

					if (parameter != null && value != null)
						paramvalues.put(parameter, value);

					data.asDatasetGraph().deleteAny(Node.ANY, v.getObject(), Node.ANY, Node.ANY);
				}
				data.asDatasetGraph().deleteAny(Node.ANY, dv.getSubject(), nHasValue, Node.ANY);

				// Check if DataValue has two Values (each with parameter and value)
				if (paramvalues.size() == 2)
				{
					Parameters parameters = new Parameters(paramvalues.keySet().toArray()[0].toString(),
							paramvalues.keySet().toArray()[1].toString());

					DataValue dvo = new DataValue(dv.getSubject().getURI(), null, parameters.getParameter1(),
							paramvalues.get(parameters.getParameter1()), parameters.getParameter2(),
							paramvalues.get(parameters.getParameter2()));

					// Check if DataValue is child of another DataValue
					Iterator<Quad> parents = data.asDatasetGraph().find(Node.ANY, Node.ANY, nHasDataValue, dv.getSubject());
					if (parents != null && parents.hasNext())
					{
						Quad parent = parents.next();
						dvo.setParentURI(parent.getSubject().getURI());
					}

					data.asDatasetGraph().deleteAny(Node.ANY, Node.ANY, nHasDataValue, dv.getSubject());
					dataValueMap.put(dvo.getURI(), dvo);
				}
				else
				{
					failedDataValues.add(dv.getSubject().getURI());
				}
			}
			data.asDatasetGraph().deleteAny(Node.ANY, Node.ANY, nType, nDataValue);

			// Add child data values to parents and create ArrayList with top data values (without
			// parent)
			for (DataValue d : dataValueMap.values())
			{
				if (d.getParentURI() != null && dataValueMap.containsKey(d.getParentURI()))
					dataValueMap.get(d.getParentURI()).addChild(d);
				else if (d.getParentURI() == null)
					topDataValues.add(d);
			}

			// Get mappings and insert data
			for (DataValue d : topDataValues)
			{
				if (!mappings.containsKey(d.getParameters()))
				{
					createConfigurationMapping(d);
				}

				if (!mappings.containsKey(d.getParameters()))
				{
					failedDataValues.add(d.getURI());
					continue;
				}

				d.setDataConfigurationMapping(mappings.get(d.getParameters()));
				boolean child_error = false;
				for (DataValue child : d.getChildren())
				{
					if (!mappings.containsKey(child.getParameters()))
					{
						child_error = true;
						break;
					}
					child.setDataConfigurationMapping(mappings.get(child.getParameters()));
				}

				if (child_error)
				{
					failedDataValues.add(d.getURI());
					continue;
				}

				if (!database.insertDataValue(d))
					failedDataValues.add(d.getURI());
			}
		}

		ontology.addTriplesFromDataset(data);

		if (failedDataValues.size() > 0)
			throw new InsertDataValueException("Inserting some DataValues failed",
					failedDataValues.toArray(new String[failedDataValues.size()]));
	}

	/**
	 * Create a mapping of a DataConfiguration into the database corresponding
	 * to a DataValue.
	 *
	 * @param dv DataValue for which the mapping shall be created
	 * @return True if mapping was created successful, otherwise false
	 */
	private boolean createConfigurationMapping(DataValue dv)
	{
		ArrayList<DataConfigurationMapping> dcms = new ArrayList<DataConfigurationMapping>();

		if (!createConfigurationMappingObjects(dv, dcms))
			return false;

		if (!database.createMappings(dcms))
			return false;

		for (DataConfigurationMapping dcm : dcms)
		{
			if (dcm.getParameterURIs().length != 2)
				return false;

			mappings.put(new Parameters(dcm.getParameterURIs()[0], dcm.getParameterURIs()[1]), dcm);
		}

		return true;
	}

	/**
	 * Creates DataConfigurationMappings for a data value and its children. The
	 * DataConfigurations to create are added to the provided ArrayList.
	 *
	 * @param dv DataValue for which the mapping shall be created
	 * @param toCreate ArrayList to which the result DataConfigurationMappings are added
	 * @return True if mapping was created successful, otherwise false
	 */
	private boolean createConfigurationMappingObjects(DataValue dv, ArrayList<DataConfigurationMapping> toCreate)
	{
		DataConfigurationMapping dcm = createConfigurationMappingObject(dv.getParameters());

		if (dcm == null)
			return false;

		toCreate.add(dcm);

		for (DataValue child : dv.getChildren())
		{
			if (!createConfigurationMappingObjects(child, toCreate))
				return false;
		}

		return true;
	}

	/**
	 * Create a mapping of a DataConfiguration into the database.
	 *
	 * @param parameters Parameters of a DataConfiguration
	 * @return True if mapping was created successful, otherwise false
	 */
	private DataConfigurationMapping createConfigurationMappingObject(Parameters parameters)
	{
		DataConfigurationMapping dcm = null;
		String sparqlQueryString = StrUtils.strjoinNL(
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ",
				"PREFIX colibri: <" + Config.getInstance().colibriOntologyFile + "#> ",
				"SELECT ?service ?dataconf ?parent ",
				"WHERE { ?service rdf:type colibri:DataService . ",
				"?dataconf rdf:type colibri:DataConfiguration . ",
				"?service colibri:hasDataConfiguration* ?dataconf . ",
				"?parent colibri:hasDataConfiguration ?dataconf . ",
				"?dataconf colibri:hasParameter " + Ontology.prepareSparqlURI(parameters.getParameter1()) + " . ",
				"?dataconf colibri:hasParameter " + Ontology.prepareSparqlURI(parameters.getParameter2()) + "  }");

		ResultSet rs = ontology.doQuerySelect(sparqlQueryString);
		if (rs.hasNext())
		{
			QuerySolution qs = rs.next();
			log.info(qs.get("service") + " " + qs.get("dataconf") + " " + qs.get("parent"));
			String tableName = "datavalue_" + UUID.randomUUID().toString().replace('-', '_');
			String columnName1 = "param1_value";
			String columnName2 = "param2_value";
			if (qs.get("parent").toString().equals(qs.get("service").toString()))
				dcm = new DataConfigurationMapping(qs.get("dataconf").toString(), qs.get("service").toString(), tableName);
			else
				dcm = new DataConfigurationMapping(qs.get("dataconf").toString(), qs.get("service").toString(), tableName, qs.get("parent").toString());
			dcm.addParameter(parameters.getParameter1(), columnName1);
			dcm.addParameter(parameters.getParameter2(), columnName2);

			return dcm;
		}

		return null;
	}

	/**
	 * Start reasoning
	 */
	public void doFullReasoning()
	{
		try
		{
			ontology.doFullReasoning();
		}
		catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Performs an update on the hybrid store
	 *
	 * @param graph SPARUL string that should be executed
	 * @throws InsertDataValueException
	 */
	@Override
	public synchronized void update(String graph) throws InsertDataValueException
	{
		Dataset temp = DatasetFactory.create();

		UpdateRequest request = UpdateFactory.create(graph);
		UpdateProcessor proc = UpdateExecutionFactory.create(request, temp);
		proc.execute();

		if (databaseEnabled)
			processTriples(temp);
		else
			ontology.addTriplesFromDataset(temp);
	}

	/**
	 * Check if a SPARQL select query has a solution (Result set is not empty)
	 *
	 * @param query SPARQL query string (select)
	 * @return True if Result set is not empty, otherwise false
	 */
	@Override
	public synchronized boolean exists(String query)
	{
		return ontology.doQuerySelect(query).hasNext();
	}

	/**
	 * Execute a SPARQL select query
	 *
	 * @param query SPARQL query string (select)
	 * @return Result set containing the result of the query
	 */
	@Override
	public synchronized ResultSet select(String query) throws Exception
	{
		return ontology.doQuerySelect(query);
	}

	/**
	 * Execute a SPARQL ask query
	 *
	 * @param query SPARQL query string (ask)
	 * @return True if there is a solution, otherwise false
	 */
	@Override
	public synchronized boolean ask(String query) throws Exception
	{
		return ontology.doQueryAsk(query);
	}

	/**
	 * Insert all statements from a model into the data store
	 *
	 * @param model Model containing the statements to insert
	 * @throws InsertDataValueException
	 */
	@Override
	public synchronized void insert(Model model) throws Exception
	{
		Dataset temp = DatasetFactory.create();
		temp.setDefaultModel(model);

		if (databaseEnabled)
			processTriples(temp);
		else
			ontology.addTriplesFromDataset(temp);
	}

	/**
	 * Delete all statements from a model from the data store
	 *
	 * @param model Model containing the statements to delete
	 */
	@Override
	public synchronized void delete(Model model) throws Exception
	{
		Dataset temp = DatasetFactory.create();
		temp.setDefaultModel(model);

		ontology.deleteTriplesFromDataset(temp);
	}

	public void start()
	{
		log.info("Starting Colibri data store");
	}

	public void stop()
	{
		log.info("Stopping Colibri data store");
	}

	public static void main(String[] args)
	{
		PGPoolingDataSource source = new PGPoolingDataSource();
		source.setDataSourceName("jdbc/colibri");
		source.setServerName("localhost");
		source.setDatabaseName("EnergyBase");
		source.setUser("postgres");
		source.setPassword("sa");
		source.setMaxConnections(10);

		ColibriDatastore ds = new ColibriDatastore(source);

		ds.doFullReasoning();

		// Long l = Math.round(Math.random() * 100);
		// String sparqlUpdateString = StrUtils.strjoinNL(
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
		// "PREFIX def: <http://www.auto.tuwien.ac.at/example/>",
		// "PREFIX colibri:
		// <https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#>",
		// "INSERT DATA { ",
		// "def:dv1 rdf:type colibri:DataValue . ",
		// "def:dv2 rdf:type colibri:DataValue . ",
		// "def:v1.1 rdf:type colibri:Value . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.2><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Value>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/dv1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasValue><http://www.auto.tuwien.ac.at/example/v1.1>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/dv1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasValue><http://www.auto.tuwien.ac.at/example/v1.2>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#value>\"2016-10-25T13:00:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.2><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#value>\"100.0\"^^<http://www.w3.org/2001/XMLSchema#decimal>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasParameter><http://www.auto.tuwien.ac.at/example/time_param_1>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.2><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasParameter><http://www.auto.tuwien.ac.at/example/offer_param_1>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/dv1.1><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#DataValue>.
		// ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1.1><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Value>.
		// ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1.2><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#Value>.
		// ",
		// "<http://www.auto.tuwien.ac.at/example/dv1.1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasValue><http://www.auto.tuwien.ac.at/example/v1.1.1>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/dv1.1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasValue><http://www.auto.tuwien.ac.at/example/v1.1.2>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1.1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#value>\"2016-10-26T13:30:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1.2><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#value>\"30.0\"^^<http://www.w3.org/2001/XMLSchema#decimal>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1.1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasParameter><http://www.auto.tuwien.ac.at/example/time_param_1_1>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/v1.1.2><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasParameter><http://www.auto.tuwien.ac.at/example/power_param_1_1>
		// . ",
		// "<http://www.auto.tuwien.ac.at/example/dv1><https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#hasDataValue><http://www.auto.tuwien.ac.at/example/dv1.1>
		// . ",
		// "def:test" + l + " rdf:type colibri:window . ",
		// "}"
		// );
		//
		// try {
		// ds.update(sparqlUpdateString);
		// } catch (InsertDataValueException e2) {
		// // TODO Auto-generated catch block
		// e2.printStackTrace();
		// }
		//
		// String query = StrUtils.strjoinNL(
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
		// "PREFIX def: <http://www.auto.tuwien.ac.at/example/>",
		// "PREFIX colibri:
		// <https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl#>",
		// "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
		// "SELECT ?s WHERE {",
		// "?s a ?test . ",
		// "?test rdfs:subClassOf colibri:Equipment . ",
		// "}"
		// );
		//
		// try {
		// ResultSet test = ds.select(query);
		// ResultSetFormatter.out(test);
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			in.read();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
