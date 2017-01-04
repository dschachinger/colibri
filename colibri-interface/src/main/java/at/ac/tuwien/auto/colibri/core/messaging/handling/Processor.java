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

package at.ac.tuwien.auto.colibri.core.messaging.handling;

import java.util.Date;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueListener;
import at.ac.tuwien.auto.colibri.core.messaging.types.MessageImpl;

/**
 * A processor is used to read a message from a given queue and processes it.
 */
public class Processor extends Thread
{
	/**
	 * Logging instance
	 */
	private static final Logger log = Logger.getLogger(Processor.class.getName());

	/**
	 * Internal identifier
	 */
	private int id = 0;

	/**
	 * Data store reference
	 */
	private Datastore datastore = null;

	/**
	 * Registry reference
	 */
	private Registry registry = null;

	/**
	 * Type of queue that is processed
	 */
	private QueueType type = null;

	/**
	 * Parent listener of this processor
	 */
	private QueueListener listener = null;

	/**
	 * Constructor for initialization.
	 * 
	 * @param listener Parent listener
	 * @param id Thread ID
	 * @param datastore Data store reference
	 * @param registry Registry reference
	 * @param type Queue type
	 */
	public Processor(QueueListener listener, int id, Datastore datastore, Registry registry, QueueType type)
	{
		// set references
		this.listener = listener;
		this.id = id;
		this.datastore = datastore;
		this.registry = registry;
		this.type = type;
	}

	@Override
	public void run()
	{
		try
		{
			// set start message
			log.info("Message handler started (processor = " + this.toString() + ")");

			// run forever
			while (true)
			{
				// process new message
				this.handle();
			}
		}
		catch (InterruptedException e)
		{
			// exit after interrupt
			log.info("Message handler interrupted (processor = " + this.toString() + ")");
			return;
		}
	}

	@Override
	public String toString()
	{
		return Integer.toString(id) + "/" + this.type.toString().toUpperCase();
	}

	private void handle() throws InterruptedException
	{
		// wait for new message and activation
		while (QueueHandler.getInstance().getQueue(this.type).isEmpty())
		{
			// notify parent that executing is waiting
			synchronized (this)
			{
				this.notify();
			}

			// wait for new messages
			synchronized (listener)
			{
				listener.wait();
			}
		}

		// set start message
		log.info("Message handler activated (processor = " + this.toString() + ")");

		try
		{
			// get current time
			Date now = new Date();

			// read message from buffer
			MessageImpl m = (MessageImpl) QueueHandler.getInstance().getQueue(this.type).poll();

			// run message checks
			if (m.getExpires() != null && m.getExpires().getTime() < now.getTime())
				throw new ProcessingException("message expired (expires:" + m.getExpires().toString() + ", now: " + now.toString(), m);
			if (m.getDate() != null && m.getDate().getTime() > now.getTime())
				throw new ProcessingException("message date is in future (date:" + m.getDate().toString() + ", now: " + now.toString(), m);

			// input processing
			if (this.type == QueueType.INPUT)
			{
				// acknowledge referenced message
				registry.removeTransmission(m.getReference());

				// process message
				m.process(datastore, registry);
			}

			// output processing
			if (this.type == QueueType.OUTPUT)
			{
				// add message to confirmable list
				registry.addTransmission(m);
			}
		}
		catch (InterfaceException e)
		{
			// set error message to output queue
			QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(e.getStatus());
		}

		// set end message
		log.info("Message handler finished (processor = " + this.toString() + ")");
	}
}
