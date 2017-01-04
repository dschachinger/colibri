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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.Peer;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.AmbiguityException;
import at.ac.tuwien.auto.colibri.core.messaging.types.Message;

/**
 * A message queue hosts a message buffer and informs registered listeners about new messages.
 */
public class MessageQueue
{
	/**
	 * Enumeration of available queue types.
	 */
	public enum QueueType
	{
		INPUT, OUTPUT
	}

	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(MessageQueue.class.getName());

	/**
	 * List of registered listeners
	 */
	private List<QueueListener> listeners = null;

	/**
	 * Internal message queue
	 */
	private Queue<Message> messages = null;

	/**
	 * Message storage object
	 */
	private MessageStorage storage = null;

	/**
	 * Queue type
	 */
	private QueueType type = null;

	/**
	 * Constructor for initialization
	 */
	public MessageQueue(MessageStorage storage, QueueType type)
	{
		// set references
		this.storage = storage;
		this.type = type;

		// create collections
		this.messages = new LinkedList<Message>();
		this.listeners = new ArrayList<QueueListener>();
	}

	/**
	 * Adds an internal message to the queue.
	 * 
	 * @param message New message
	 */
	public synchronized void addInternal(Message message)
	{
		try
		{
			this.add(message, false);
		}
		catch (AmbiguityException e)
		{
			// do nothing
		}
	}

	/**
	 * Adds an external message to the queue.
	 * 
	 * @param message New message
	 * @throws AmbiguityException If the message-id is not unique
	 */
	public synchronized void addExternal(Message message) throws AmbiguityException
	{
		this.add(message, true);
	}

	/**
	 * Adds a message to the queue.
	 * 
	 * @param message New message
	 * @param external Indication of external or internal message source
	 * @throws AmbiguityException If the message-id is not unique
	 */
	private synchronized void add(Message message, boolean external) throws AmbiguityException
	{
		// no message available
		if (message == null)
			return;

		// guarantee uniqueness of message id for internally produced messages
		if (!external)
		{
			int attempts = 1;
			while (this.storage.get(message.getMessageId()) != null)
			{
				// write log
				log.info("Message-Id is not unique (id = " + message.getMessageId() + ", attempt = " + attempts + ")");
				attempts++;

				// set unique message ID
				String id = message.getMessageId();
				id = id + Integer.toString(id.hashCode());
				message.setMessageId(id);
			}
		}

		// add message to storage
		this.storage.put(message);

		// add message to queue
		this.messages.add(message);

		// set log message
		log.info("Message added to queue (type = " + type.toString() + ", size = " + this.messages.size() + ")");

		// inform registered listeners
		for (QueueListener listener : listeners)
			listener.onAdd();
	}

	/**
	 * Add queue action listeners.
	 * 
	 * @param listener Listener to be added
	 */
	public synchronized void addListener(QueueListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Indicates if the queue is empty.
	 * 
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return this.messages.isEmpty();
	}

	/**
	 * Returns the head of the queue and removes it from the queue.
	 * 
	 * @return
	 */
	public synchronized Message poll()
	{
		// get message
		Message msg = this.messages.poll();

		// set log message
		log.info("Message read from the queue head (type = " + type.toString() + ", size = " + this.messages.size() + ")");

		return msg;
	}

	/**
	 * Removes all messages in the queue that correspond to a specific peer.
	 * 
	 * @param peer Peer that should be removed
	 */
	public synchronized void clean(Peer peer)
	{
		// temporary list
		List<Message> temp = new ArrayList<Message>();

		// find messages
		for (Message m : this.messages)
		{
			if (m.getPeer().equals(peer))
				temp.add(m);
		}

		// remove messages of the given peer
		this.messages.removeAll(temp);

		// set log message
		log.info("Messages removed for peer (type = " + type.toString() + ", peer = " + peer.toString() + ")");

		// notify listeners
		for (QueueListener listener : listeners)
			listener.onClean(peer);
	}
}
