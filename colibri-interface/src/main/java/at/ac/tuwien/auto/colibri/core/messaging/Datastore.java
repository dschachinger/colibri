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

package at.ac.tuwien.auto.colibri.core.messaging;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * This interface describes the method that need to be provided by the actual data store.
 */
public interface Datastore
{
	/**
	 * This method takes a SPARUL query and runs an insert or delete on the data store.
	 * 
	 * @param query SPARUL graph
	 * 
	 * @throws Exception
	 */
	void update(String query) throws Exception;

	/**
	 * This method is used to query the data store and returns a result set or null.
	 * 
	 * @param query SPARQL SELECT query
	 * @return result set, which can also be empty, or null
	 * 
	 * @throws Exception
	 */
	ResultSet select(String query) throws Exception;

	/**
	 * This method indicates whether there are results of the query or not.
	 * 
	 * @param query SPARQL SELECT query
	 * @return returns true if results were found, returns false if no results were found
	 * 
	 * @throws Exception
	 */
	boolean exists(String query) throws Exception;

	/**
	 * This method returns the answer of an ASK query.
	 * 
	 * @param query SPARQL ASK query
	 * @return returns true if ask query returns true, false otherwise
	 * 
	 * @throws Exception
	 */
	boolean ask(String query) throws Exception;

	/**
	 * This method takes an ontology model and inserts its content in the data store.
	 * 
	 * @param model ontology model that should be inserted
	 * 
	 * @throws Exception
	 */
	void insert(Model model) throws Exception;
	
	/**
	 * This method takes an ontology model and deletes its content in the data store.
	 * 
	 * @param model ontology model that should be deleted
	 * 
	 * @throws Exception
	 */
	void delete(Model model) throws Exception;
}
