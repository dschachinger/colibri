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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;

public class ColibriCore
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ColibriCore.class.getName());

	/**
	 * Tomcat server
	 */
	private TomcatServer server = null;

	/**
	 * Constructor for initialization of exception handling.
	 */
	public ColibriCore()
	{
		log.info("Initializing Colibri core");

		// set global exception handling
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			public void uncaughtException(Thread t, Throwable e)
			{
				// print log message
				Logger.getLogger(ColibriCore.class.getName()).severe("Uncaught Exception in thread '" + t.getName() + "': " + e.getMessage());

				// print stack trace
				e.printStackTrace();

				// exit core
				System.exit(1);
			}
		});
	}

	/**
	 * Starts the Colibri semantic core.
	 * 
	 * @throws LifecycleException
	 * @throws IOException
	 * @throws ServletException
	 */
	public void start() throws IOException, ServletException
	{
		log.info("Starting Colibri core");

		// initialize and start Tomcat
		this.server = new TomcatServer();
		this.server.start();

	}

	/**
	 * Stops the Colibri semantic core.
	 * 
	 * @throws LifecycleException
	 * @throws InterruptedException
	 */
	public void stop()
	{
		log.info("Stopping Colibri core");

		// shutdown Tomcat
		if (this.server != null)
			this.server.stop();
	}

	/**
	 * Main method to run Colibri semantic core.
	 * 
	 * @param args
	 * @throws LifecycleException
	 * @throws IOException
	 * @throws ServletException
	 */
	public static void main(String[] args) throws IOException, ServletException
	{
		// initialize core
		ColibriCore core = new ColibriCore();

		// start core
		core.start();

		// get buffered reader from Console input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// stop core if character was read
		in.read();
		core.stop();

		// exit
		System.exit(0);
	}
}
