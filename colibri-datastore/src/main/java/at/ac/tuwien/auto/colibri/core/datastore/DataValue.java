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

import java.util.ArrayList;
import java.util.HashMap;

public class DataValue
{
	private String uri;
	private DataConfigurationMapping mapping;
	private Parameters parameters;
	private HashMap<String, String> parametervalues;
	private ArrayList<DataValue> children;
	private String parentURI = null;

	public DataValue(String uri, DataConfigurationMapping mapping, String parameter1_uri, String parameter1_value,
			String parameter2_uri, String parameter2_value)
	{
		children = new ArrayList<DataValue>();
		this.mapping = mapping;
		this.uri = uri;
		this.parameters = new Parameters(parameter1_uri, parameter2_uri);
		parametervalues = new HashMap<String, String>();
		parametervalues.put(parameter1_uri, parameter1_value);
		parametervalues.put(parameter2_uri, parameter2_value);
	}

	public DataConfigurationMapping getDataConfigurationMapping()
	{
		return mapping;
	}

	public void setDataConfigurationMapping(DataConfigurationMapping mapping)
	{
		this.mapping = mapping;
	}

	public String getValue(String parameter_uri)
	{
		return parametervalues.get(parameter_uri);
	}

	public Parameters getParameters()
	{
		return parameters;
	}

	public void setParentURI(String uri)
	{
		this.parentURI = uri;
	}

	public String getParentURI()
	{
		return parentURI;
	}

	public String getURI()
	{
		return uri;
	}

	public void addChild(DataValue dv)
	{
		children.add(dv);
	}

	public ArrayList<DataValue> getChildren()
	{
		return children;
	}
}
