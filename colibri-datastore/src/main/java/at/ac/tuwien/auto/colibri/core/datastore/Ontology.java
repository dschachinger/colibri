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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import at.ac.tuwien.auto.colibri.core.datastore.reasoner.ReasonerRunnable;
import at.ac.tuwien.auto.colibri.core.datastore.reasoner.TaskListener;

public class Ontology implements TaskListener
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(Ontology.class.getName());
	
	private Model dataModel = null;
	private Model colibriModel = null;
	private Dataset dataset = null;

	private boolean updateBufferingEnabled = false;
	private UpdateBuffer buffer = null;

	public Ontology()
	{
		loadTDB(Config.getInstance().tdbDirectory);
		loadDefaultModel();
		loadOntology(Config.getInstance().ontologyFileBase);

		buffer = new UpdateBuffer(Config.getInstance().buffer);
	}

	/**
	 * Get dataset from the TDB
	 *
	 * @param directoryPath Path of the directory that should be created to store the TDB
	 */
	private void loadTDB(String directoryPath)
	{
		if (dataset != null)
			TDBFactory.release(dataset);

		dataset = TDBFactory.createDataset(Config.getInstance().tdbDirectory);
		TDB.getContext().set(TDB.symUnionDefaultGraph, true);
	}

	/**
	 * Load default model from the global data set
	 */
	private void loadDefaultModel()
	{
		if (dataset == null)
			return;

		dataModel = dataset.getNamedModel(Config.getInstance().dataModelUri);
		colibriModel = dataset.getNamedModel(Config.getInstance().colibriModelUri);
		log.info("Default model read from dataset");
	}

	/**
	 * Load an ontology from the file system into the model
	 *
	 * @param filePath Path of the file containing the ontology
	 */
	private void loadOntology(String filePath)
	{
		if (dataset == null || dataModel == null || colibriModel == null)
			return;

		if (dataModel.isEmpty())
		{
			dataset.begin(ReadWrite.WRITE);
			FileManager.get().readModel(dataModel, filePath);
			dataset.commit();
			dataset.end();
			log.info("Info]: Ontolgy loaded from file: " + filePath);
		}

		if (colibriModel.isEmpty())
		{
			dataset.begin(ReadWrite.WRITE);
			colibriModel.read(Config.getInstance().colibriModelUri);
			dataset.commit();
			dataset.end();
		}
	}

	/**
	 * Perform a select query against the global data set
	 *
	 * @param query SPARQL query string
	 * @return Result set containing the result of the query
	 */
	public ResultSet doQuerySelect(String query)
	{
		ResultSet rs = null;

		if (dataset == null)
			return null;

		dataset.begin(ReadWrite.READ);

		// https://jena.apache.org/documentation/tdb/datasets.html
		// Note that setting tdb:unionDefaultGraph does not affect the default graph or default
		// model obtained with dataset.getDefaultModel(). The RDF merge of all named graph can be
		// accessed as the named graph urn:x-arq:UnionGraph using
		// Dataset.getNamedModel("urn:x-arq:UnionGraph")
		try (QueryExecution qExec = QueryExecutionFactory.create(query, dataset.getNamedModel("urn:x-arq:UnionGraph")))
		{
			rs = qExec.execSelect();
			rs = ResultSetFactory.copyResults(rs);
		}
		dataset.end();

		return rs;
	}

	/**
	 * Perform an ask query against the global data set
	 *
	 * @param query SPARQL query string
	 * @return true if a solution exists
	 */
	public boolean doQueryAsk(String query)
	{
		boolean ret;

		dataset.begin(ReadWrite.READ);
		try (QueryExecution qExec = QueryExecutionFactory.create(query, dataset))
		{
			ret = qExec.execAsk();
		}
		dataset.end();

		return ret;
	}

	/**
	 * Perform an update on the global data set
	 *
	 * @param updateQuery SPARUL string
	 */
	public void doUpdate(String updateQuery)
	{
		doUpdate(updateQuery, false);
	}

	/**
	 * Perform an update on the global data set
	 *
	 * @param updateQuery SPARUL string
	 * @param noBuffering Force no buffering for this update
	 */
	private void doUpdate(String updateQuery, boolean noBuffering)
	{
		try
		{
			dataset.begin(ReadWrite.WRITE);
			UpdateAction.parseExecute(updateQuery, dataset.getNamedModel(Config.getInstance().dataModelUri));

			if (updateBufferingEnabled && !noBuffering)
			{
				try
				{
					buffer.writeToBuffer(updateQuery);
				}
				catch (IOException e)
				{
					log.severe("Writing to query buffer failed");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			dataset.commit();
			dataset.end();
		}
	}

	/**
	 * Add triples to data model in the global dataset
	 *
	 * @param insert Dataset containing triples in default model
	 * @throws IOException
	 * @throws OWLOntologyStorageException
	 * @throws OWLOntologyCreationException
	 */
	public void addTriplesFromDataset(Dataset insert)
	{
		dataset.begin(ReadWrite.WRITE);
		dataset.getNamedModel(Config.getInstance().dataModelUri).add(insert.getDefaultModel());
		dataset.commit();
		dataset.end();
	}

	/**
	 * Delete triples from data model in the global dataset
	 *
	 * @param delete Dataset containing triples to delete in default model
	 */
	public void deleteTriplesFromDataset(Dataset delete)
	{
		dataset.begin(ReadWrite.WRITE);
		dataset.getNamedModel(Config.getInstance().dataModelUri).remove(delete.getDefaultModel());
		dataset.commit();
		dataset.end();
	}

	/**
	 * Delete all triples from the TDB (model containing individuals)
	 */
	public void clearTDB()
	{
		if (dataset == null)
			return;

		String sparqlUpdateString = StrUtils.strjoinNL(
				"CLEAR ALL");

		doUpdate(sparqlUpdateString, true);
	}

	/**
	 * Get the number of triples present in the TDB
	 *
	 * @return Number of triples in the TDB
	 */
	public int getTripleCount()
	{
		ResultSet rs = null;
		int count = 0;

		if (dataset == null)
			return -1;

		dataset.begin(ReadWrite.READ);
		try (QueryExecution qExec = QueryExecutionFactory.create(
				"SELECT (count(*) AS ?count) { ?s ?p ?o} ", dataset))
		{
			rs = qExec.execSelect();

			QuerySolution row = rs.next();
			Literal countLit = ((Literal) row.get("count"));
			count = countLit.getInt();
		}
		dataset.end();

		return count;
	}

	/**
	 * Perform reasoning on the TDB as separate thread.
	 * Due to lacking interfaces to the current version of Jena, the ontology is first
	 * written into a file and than read into an OWL API ontology. Reasoning is done
	 * on that OWL API ontology. Afterwards, the inferred ontology is written to the
	 * file system.
	 */
	public void doReasoning() throws IOException, OWLOntologyCreationException, OWLOntologyStorageException
	{
		if (dataset == null || dataModel == null || colibriModel == null)
			return;

		updateBufferingEnabled = true;

		FileOutputStream tempoutstrm = new FileOutputStream(Config.getInstance().ontologyFileTemp);
		dataset.begin(ReadWrite.READ);
		loadDefaultModel();
		dataset.end();
		dataModel.write(tempoutstrm, "RDF/XML");
		tempoutstrm.close();
		log.info("Ontology from TDB has been written to file");

		ReasonerRunnable reasoner = new ReasonerRunnable();
		reasoner.addListener(this);
		reasoner.setOntologyFile(Config.getInstance().ontologyFileTemp);
		reasoner.setOutputFile(Config.getInstance().ontologyFileOut);

		(new Thread(reasoner)).start();
	}

	/**
	 * Performs all updates stored in the buffer and clear buffer
	 */
	private void doBufferedUpdates()
	{
		try
		{
			for (String query : buffer.getBufferedQueryStrings())
			{
				doUpdate(query);
			}

			buffer.clearBuffer();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void threadFinished(Runnable r)
	{
		clearTDB();
		loadTDB(Config.getInstance().tdbDirectory);
		loadDefaultModel();
		loadOntology(Config.getInstance().ontologyFileOut);

		try
		{
			Files.deleteIfExists(Paths.get(Config.getInstance().ontologyFileTemp));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		log.info("Triple count:" + getTripleCount());

		updateBufferingEnabled = false;
		doBufferedUpdates();
	}

	public static String prepareSparqlURI(String uri)
	{
		String ret = uri.trim();
		if (!ret.startsWith("<"))
			ret = "<" + ret;
		if (!ret.endsWith(">"))
			ret = ret + ">";
		return ret;
	}
}
