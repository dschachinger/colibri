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
 * 
 * This file is part of the Colibri project.
 *************************************************************************************************/

package at.ac.tuwien.auto.colibri.semantics.impl;

import at.ac.tuwien.auto.colibri.data.AccessService;

/**
 * This class manages the data access to the native triple store (to the OWL ontology)
 * 
 * @author dschachinger
 *
 */
public class AccessServiceImpl implements AccessService
{
	@Override
	public boolean open(String address)
	{
		// TODO open connection to triple store

		return true;
	}

	@Override
	public Object read()
	{
		String source = "https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/EnergyResourceOntologyExample.owl";

		// TODO read values from triple store

		System.out.println("read from " + source);

		// todo: manage ontology access

		// // String NS = SOURCE + "#";
		// // create a model using reasoner
		// Model model1 =
		// ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF);
		// // create a model which doesn't use a reasoner
		// Model model2 =
		// ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		//
		// // read the RDF/XML file
		// model1.read(SOURCE, "RDF/XML");
		// model2.read(SOURCE, "RDF/XML");
		// // prints out the RDF/XML structure
		//
		// System.out.println(" ");
		//
		// // Create a new query
		// String queryString = "PREFIX rdf:
		// <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " PREFIX owl:
		// <http://www.w3.org/2002/07/owl#>" + " PREFIX rdfs:
		// <http://www.w3.org/2000/01/rdf-schema#>" + " PREFIX xsd:
		// <http://www.w3.org/2001/XMLSchema#>" + " PREFIX think:
		// <https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/EnergyResourceOntology.owl#>"
		// + " SELECT ?subject ?object " + " WHERE { ?subject
		// think:consumesEnergy ?object } \n ";
		//
		// try
		// {
		// Query query = QueryFactory.create(queryString);
		//
		// System.out.println("----------------------");
		//
		// System.out.println("Query Result Sheet");
		//
		// System.out.println("----------------------");
		//
		// System.out.println("Direct&Indirect Descendants (model1)");
		//
		// System.out.println("-------------------");
		//
		// // Execute the query and obtain results
		// QueryExecution qe = QueryExecutionFactory.create(query);
		// ResultSet results = qe.execSelect();
		//
		// // Output query results
		// ResultSetFormatter.out(System.out, results);
		//
		// qe.close();
		// }
		// catch (Exception ex)
		// {
		// ex.printStackTrace(System.out);
		// }

		// TODO fill values to appropriate data structure

		return new Object();
	}

	@Override
	public void write(Object obj)
	{
		// TODO write received object to data store

		// TODO convert values to SPARUL query
	}
}
