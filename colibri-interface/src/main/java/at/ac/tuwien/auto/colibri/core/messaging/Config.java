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

import java.util.HashMap;
import java.util.StringTokenizer;

import at.ac.tuwien.auto.colibri.commons.Configuration;

/**
 * Configuration file for Colibri interface
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
	 * Max number of retries for each confirmable transmission
	 */
	public int retries = 0;

	/**
	 * Retry interval in milliseconds
	 */
	public int interval = 0;

	/**
	 * Number of message handler threads for input queue
	 */
	public int input = 0;

	/**
	 * Number of message handler threads for output queue
	 */
	public int output = 0;

	/**
	 * URI of ontology
	 */
	public String ontology = "";

	/**
	 * Namespace of ontology
	 */
	public String namespace = "";
	
	/**
	 * Available prefixes
	 */
	public String prefixes = "";
	
	/**
	 * Reasoner
	 */
	public boolean reasoner = false;

	@Override
	public String getResource()
	{
		return "/interface.properties";
	}

	/**
	 * Private constructor
	 */
	private Config()
	{		
		this.input = Runtime.getRuntime().availableProcessors();
		this.output = Runtime.getRuntime().availableProcessors();
		
		this.load();
	}
	
	/**
	 * Parses the prefixes string into a hash map.
	 * 
	 * @return map of prefixes
	 * 
	 * @throws Exception
	 */
	public HashMap<String,String> getPrefixes() throws Exception
	{
		HashMap<String,String> map = new HashMap<String,String>();
		
		StringTokenizer st = new StringTokenizer(this.prefixes,";");
		
		while(st.hasMoreTokens())
		{
			String prefix = st.nextToken().trim();
			StringTokenizer t = new StringTokenizer(prefix," ");
			
			if (t.countTokens() != 2)
				throw new Exception("Prefixes string in properties file is wrong");
			
			map.put(t.nextToken().trim(), t.nextToken().trim());
		}		
		return map;
	}
}
