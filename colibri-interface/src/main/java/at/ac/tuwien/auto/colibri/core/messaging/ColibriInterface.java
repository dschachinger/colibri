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

import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.queue.InputListener;
import at.ac.tuwien.auto.colibri.core.messaging.queue.OutputListener;

/**
 * This class is the central element of the Colibri interface.
 */
public class ColibriInterface
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ColibriInterface.class.getName());

	/**
	 * Central registry object for observations, connectors, transmissions
	 */
	private Registry registry = null;

	/**
	 * Listener to input queue
	 */
	private InputListener inputListener = null;

	/**
	 * Listener to output queue
	 */
	private OutputListener outputListener = null;

	/**
	 * Standard constructor for initialization of Colibri interface.
	 */
	public ColibriInterface(Datastore datastore)
	{
		log.info("Initializing Colibri interface");

		// create registry
		this.registry = new Registry();

		// create listeners
		this.inputListener = new InputListener(datastore, registry);
		this.outputListener = new OutputListener(datastore, registry);
	}

	/**
	 * Starts the Colibri interface including activation of queue listeners.
	 */
	public void start()
	{
		log.info("Starting Colibri interface");

		// start listeners
		this.inputListener.start();
		this.outputListener.start();
	}

	/**
	 * Stops the Colibri interface.
	 */
	public void stop()
	{
		log.info("Stopping Colibri interface");

		// clear registry
		this.registry.clean();

		// stop listeners
		this.inputListener.stop();
		this.outputListener.stop();
	}
}
