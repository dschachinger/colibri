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

import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;

/**
 * This static class is used as central message queue and storage.
 */
public class QueueHandler
{
	/**
	 * Static instance
	 */
	private static QueueHandler instance = null;

	/**
	 * Input message queue
	 */
	private MessageQueue inputQueue = null;

	/**
	 * Output message queue
	 */
	private MessageQueue outputQueue = null;

	/**
	 * Private constructor for initialization
	 */
	private QueueHandler()
	{
		this.inputQueue = new MessageQueue(QueueType.INPUT);
		this.outputQueue = new MessageQueue(QueueType.OUTPUT);
	}

	/**
	 * Returns the central message queue object.
	 * 
	 * @return Singleton queue handler
	 */
	public synchronized static QueueHandler getInstance()
	{
		if (instance == null)
			instance = new QueueHandler();
		return instance;
	}

	/**
	 * Returns a queue based on the given type.
	 * 
	 * @param type Type of the requested queue
	 * @return Queue
	 */
	public MessageQueue getQueue(QueueType type)
	{
		if (type == QueueType.INPUT)
			return inputQueue;
		if (type == QueueType.OUTPUT)
			return outputQueue;
		return null;
	}
}
