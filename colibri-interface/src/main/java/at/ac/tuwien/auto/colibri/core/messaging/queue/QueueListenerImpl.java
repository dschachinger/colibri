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

package at.ac.tuwien.auto.colibri.core.messaging.queue;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.handling.Processor;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;

/**
 * Implements the queue listener interface.
 */
public abstract class QueueListenerImpl implements QueueListener
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(QueueListenerImpl.class.getName());

	/**
	 * Available message handlers
	 */
	private List<Processor> messageHandlers = null;

	/**
	 * Number of message handlers
	 */
	private int threads = 0;

	/**
	 * Data store object
	 */
	private Datastore datastore = null;

	/**
	 * Queue type
	 */
	private QueueType type = null;

	/**
	 * Standard constructor
	 * 
	 * @param store Data store reference
	 * @param registry Registry reference
	 * @param type Type of queue
	 * @param log Logging instance
	 */
	public QueueListenerImpl(Datastore store, QueueType type, int threads)
	{
		log.info("Initializing queue listener (" + type.toString() + ")");

		// set variables
		this.datastore = store;
		this.type = type;
		this.threads = threads;

		// register queue listener
		QueueHandler.getInstance().getQueue(type).addListener(this);
	}

	/**
	 * Loads and starts a number of message handler threads
	 */
	public void start()
	{
		log.info("Starting queue listener (" + type.toString() + ")");

		// initialize array
		this.messageHandlers = new ArrayList<Processor>();

		// create and start threads
		for (int i = 0; i < this.threads; i++)
		{
			Processor h = new Processor(this, i, datastore, type);
			h.start();

			this.messageHandlers.add(h);
		}
	}

	/**
	 * Stops the queue listener
	 */
	public void stop()
	{
		log.info("Stopping queue listener (" + type.toString() + ")");

		for (Processor h : this.messageHandlers)
		{
			// wait until processor has stopped
			if (h.getState() != State.WAITING)
			{
				log.info("Wait for thread " + h.getId() + " (" + type.toString() + ")");

				synchronized (h)
				{
					try
					{
						h.wait();
					}
					catch (InterruptedException e)
					{
						// do nothing
					}
				}
			}

			log.info("Interrupt thread " + h.getId() + " (" + type.toString() + ")");

			// interrupt thread
			h.interrupt();
		}
	}

	@Override
	public void onAdd()
	{
		synchronized (this)
		{
			log.info("Message handler notified (" + type.toString() + ")");

			// notify one arbitrary, available message handler
			this.notify();
		}

	}
}
