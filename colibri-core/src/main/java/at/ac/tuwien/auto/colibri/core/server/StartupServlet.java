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

package at.ac.tuwien.auto.colibri.core.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import at.ac.tuwien.auto.colibri.core.datastore.ColibriDatastore;
import at.ac.tuwien.auto.colibri.core.messaging.ColibriInterface;

/**
 * This servlet is executed on startup of Tomcat and initializes interface and data store in the
 * Tomcat context.
 */
public class StartupServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(StartupServlet.class.getName());

	/**
	 * Colibri interface instance
	 */
	private ColibriInterface colibriInterface = null;

	/**
	 * Colibri data store instance
	 */
	private ColibriDatastore colibriDatastore = null;

	/**
	 * Default constructor for initialization of interface and data store.
	 * 
	 * @throws NamingException
	 * @throws IOException
	 */
	public StartupServlet() throws NamingException, IOException
	{
		log.info("Initializing startup servlet");

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");

		// load properties
		Properties properties = this.loadProperties();

		// get context
		InitialContext ctx = new InitialContext();
		Context envCtx = (Context) ctx.lookup("java:comp/env");

		// get registered data source
		DataSource ds = (DataSource) envCtx.lookup(properties.getProperty("jdbc", "jdbc/postgresql"));

		// initialize and start Colibri datastore
		this.colibriDatastore = new ColibriDatastore(ds);
		this.colibriDatastore.start();

		// initialize and start Colibri interface
		this.colibriInterface = new ColibriInterface(this.colibriDatastore);
		this.colibriInterface.start();
	}

	/**
	 * Load properties from config file.
	 * 
	 * @throws IOException
	 */
	private Properties loadProperties() throws IOException
	{
		Properties properties = new Properties();
		URL url = this.getClass().getResource("/core.properties");
		BufferedInputStream stream = new BufferedInputStream(url.openStream());
		properties.load(stream);
		stream.close();

		return properties;
	}

	/**
	 * This method is executed when Tomcat is shut down and objects need to be destroyed.
	 */
	@Override
	public void destroy()
	{
		log.info("Destroying startup servlet");

		// shutdown Colibri interface
		if (this.colibriInterface != null)
			this.colibriInterface.stop();

		// shutdown Colibri data store
		if (this.colibriDatastore != null)
			this.colibriDatastore.stop();

		// call super method
		super.destroy();
	}
}
