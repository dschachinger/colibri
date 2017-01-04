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

import java.util.HashMap;

public class DataConfigurationMapping
{
	private String dataconfig_uri;
	private String service_uri;
	private String table_name;
	private String parent_uri;
	private HashMap<String, String> parameters;

	public DataConfigurationMapping(String dataconfig_uri, String service_uri, String table_name)
	{
		this(dataconfig_uri, service_uri, table_name, null);
	}

	public DataConfigurationMapping(String dataconfig_uri, String service_uri, String table_name, String parent_uri)
	{
		this.service_uri = service_uri;
		this.dataconfig_uri = dataconfig_uri;
		this.table_name = table_name;
		this.parent_uri = parent_uri;

		parameters = new HashMap<String, String>();
	}

	public String getDataConfigurationURI()
	{
		return dataconfig_uri;
	}

	public String getServiceURI()
	{
		return service_uri;
	}

	public String getTableName()
	{
		return table_name;
	}

	public String getParentDataConfigurationURI()
	{
		return parent_uri;
	}

	public void addParameter(String uri, String column)
	{
		parameters.put(uri, column);
	}

	public String getParameterColumn(String uri)
	{
		return parameters.get(uri);
	}

	public HashMap<String, String> getParameters()
	{
		return parameters;
	}

	public String[] getParameterURIs()
	{
		return parameters.keySet().toArray(new String[0]);
	}
}
