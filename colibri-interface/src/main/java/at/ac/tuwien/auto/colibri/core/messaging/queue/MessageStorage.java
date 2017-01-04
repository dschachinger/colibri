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

import java.util.HashMap;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.exceptions.AmbiguityException;
import at.ac.tuwien.auto.colibri.core.messaging.types.Message;

/**
 * The message storage hosts all messages that are sent and received during runtime.
 */
public class MessageStorage
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(MessageStorage.class.getName());

	/**
	 * Map of sent and received messages
	 */
	private HashMap<String, Message> messages = null;

	/**
	 * Constructor for initialization
	 */
	public MessageStorage()
	{
		this.messages = new HashMap<String, Message>();
	}

	/**
	 * Adds a message to the central storage.
	 * 
	 * @param message New message
	 */
	public synchronized void put(Message message) throws AmbiguityException
	{
		// check message ID
		if (this.messages.containsKey(message.getMessageId()))
			throw new AmbiguityException("Message-Id is not unique (id = " + message.getMessageId() + ")", message);

		// store message ID
		this.messages.put(message.getMessageId(), message);

		log.info("Message stored (message-id = " + message.toString() + ")");
	}

	/**
	 * Returns a message with the given ID.
	 * 
	 * @param messageId Message ID
	 * @return Found message or null
	 */
	public synchronized Message get(String messageId)
	{
		return this.messages.get(messageId);
	}
}
