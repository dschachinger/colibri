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

package at.ac.tuwien.auto.colibri.optimization;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.websocket.jsr356.JettyClientContainerProvider;

import at.ac.tuwien.auto.colibri.optimization.connection.ClientSocketEndpoint;

/**
 * This class is the central controller for the optimization application.
 */
public class ColibriOptimization
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ColibriOptimization.class.getName());

	/**
	 * Client socket
	 */
	private ClientSocketEndpoint endpoint = null;

	/**
	 * Optimizer control logic
	 */
	private ColibriOptimizationController optimizer;

	/**
	 * Thread for application
	 */
	private Thread controller = null;

	/**
	 * Standard constructor for initialization of Colibri optimizer.
	 */
	public ColibriOptimization()
	{
		log.info("Initializing Colibri optimizer");

		// set global exception handling
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			public void uncaughtException(Thread t, Throwable e)
			{
				// print log message
				Logger.getLogger(ColibriOptimization.class.getName()).severe("Uncaught Exception in thread '" + t.getName() + "': " + e.getMessage());

				// print stack trace
				e.printStackTrace();

				// exit core
				System.exit(1);
			}
		});
	}

	/**
	 * Main function to run the application.
	 * 
	 * @param args input parameters
	 */
	public static void main(String[] args) throws IOException
	{
		// initialize optimizer
		ColibriOptimization optimizer = new ColibriOptimization();

		// start optimizer
		optimizer.start();

		// wait
		optimizer.await();

		// stop optimizer
		optimizer.stop();

		// exit
		System.exit(0);
	}

	/**
	 * Starts the optimizer application.
	 */
	public void start()
	{
		log.info("Starting Colibri optimizer");

		try
		{
			// create client endpoint
			this.endpoint = new ClientSocketEndpoint();

			// create optimizer controller
			this.optimizer = new ColibriOptimizationController(endpoint);

			// connect to WebSocket server endpoint
			WebSocketContainer container = new JettyProvider().getContainer();
			container.connectToServer(this.endpoint, URI.create(Config.getInstance().endpoint));

			// wait for connection is opened
			this.endpoint.await();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}

		// create thread for GUI frame
		this.controller = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// create frame
				ColibriOptimizationView frame = new ColibriOptimizationView(optimizer);

				// set frame visible
				frame.pack();
				frame.setVisible(true);

				// wait until frame is closed
				frame.await();
			}
		});

		// execute thread
		this.controller.start();
	}

	/**
	 * Stops the optimizer application.
	 */
	public void stop()
	{
		log.info("Stopping Colibri optimizer");

		// stop controller thread
		if (this.controller != null)
			this.controller.interrupt();

		try
		{
			// stop socket endpoint
			JettyClientContainerProvider.stop();
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
	}

	/**
	 * Waits for closing the application.
	 */
	private void await()
	{
		try
		{
			if (this.controller != null)
			{
				// wait for thread
				this.controller.join();
			}
		}
		catch (InterruptedException e)
		{
			log.severe(e.getMessage());
		}
	}
}
