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

import at.ac.tuwien.auto.colibri.commons.Configuration;

/**
 * Configuration file for Colibri datastore
 */
public class Config extends Configuration
{
	/**
	 * Singleton member variable
	 */
	private static Config configuration = null;

	/**
	 * Singleton method
	 * 
	 * @return instance of configuration
	 */
	public static Config getInstance()
	{
		if (configuration == null)
		{
			configuration = new Config();
		}
		return configuration;
	}

	/**
	 * Enable database for data values
	 */
	public boolean databaseEnabled = false;

	/**
	 * URI of data model
	 */
	public String dataModelUri = "http://www.auto.tuwien.ac.at/model";

	/**
	 * URI of Colibri ontology model
	 */
	public String colibriModelUri = "https://raw.githubusercontent.com/dschachinger/colibri/master/colibri-commons/src/main/resources/colibri.owl";

	/**
	 * Directory of triple store
	 */
	public String tdbDirectory;

	/**
	 * Path to base file for ontology loading
	 */
	public String ontologyFileBase;

	/**
	 * Path to ontology out file
	 */
	public String ontologyFileOut;

	/**
	 * Path to temporary ontology file
	 */
	public String ontologyFileTemp;

	/**
	 * Path of buffer file
	 */
	public String buffer;

	@Override
	public String getResource()
	{
		return "/datastore.properties";
	}

	/**
	 * Private constructor
	 */
	private Config()
	{
		this.load();
	}
}
