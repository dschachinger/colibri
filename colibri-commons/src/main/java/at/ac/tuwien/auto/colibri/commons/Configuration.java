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

package at.ac.tuwien.auto.colibri.commons;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class is used to load properties from configuration files
 */
public abstract class Configuration
{
	/**
	 * Logger instance
	 */
	protected static final Logger log = Logger.getLogger(Configuration.class.getName());

	/**
	 * Gets path of configuration file
	 * 
	 * @return resource path
	 */
	public abstract String getResource();

	/**
	 * Loads configuration from properties file.
	 */
	public void load()
	{
		try
		{
			// read property file
			Properties properties = new Properties();
			URL url = this.getClass().getResource(this.getResource());
			BufferedInputStream stream = new BufferedInputStream(url.openStream());
			properties.load(stream);
			stream.close();

			// set field values
			for (Field field : this.getClass().getFields())
			{
				if (properties.containsKey(field.getName()))
				{
					try
					{
						// integer field
						if (field.getType().equals(Integer.class) || field.getType().equals(int.class))
							field.set(this, Integer.valueOf(properties.getProperty(field.getName())));

						// string field
						else if (field.getType().equals(String.class))
							field.set(this, properties.getProperty(field.getName()));

						// boolean field
						else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))
							field.set(this, Boolean.valueOf(properties.getProperty(field.getName())));
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{
						log.severe(e.getMessage());
					}
				}
			}
		}
		catch (IOException e)
		{
			log.severe(e.getMessage());
		}
	}
}
