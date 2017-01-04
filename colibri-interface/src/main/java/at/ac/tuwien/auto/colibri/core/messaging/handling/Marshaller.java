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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import at.ac.tuwien.auto.colibri.core.messaging.Peer;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.MarshallingException;
import at.ac.tuwien.auto.colibri.core.messaging.types.Message;

/**
 * The marshaller transforms a message into its serialization.
 */
public class Marshaller
{
	/**
	 * Date formatter for conversion to UTC
	 */
	private SimpleDateFormat formatter = null;

	/**
	 * Constructor for initialization of the marshaller.
	 * 
	 * @param peer Reference to parent peer
	 */
	public Marshaller(Peer peer)
	{
		// create date formatter for UTC
		this.formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Converts the message to a serialized string.
	 * 
	 * @param message Outgoing message object
	 * @return Serialized string
	 * @throws MarshallingException
	 */
	public String convert(Message message) throws MarshallingException
	{
		// check message
		if (message == null)
			throw new MarshallingException("Message is empty", message);

		// check mandatory message type
		if (message.getMessageType() == null || message.getMessageType().isEmpty())
			throw new MarshallingException("Message type is not set", message);

		// check mandatory message id
		if (message.getMessageId() == null || message.getMessageId().isEmpty())
			throw new MarshallingException("Message-Id is not set", message);

		// check mandatory content type
		if (message.getContentType() == null)
			throw new MarshallingException("Content-Type is not set", message);

		// build message
		String msg = message.getMessageType();
		msg += "\nMessage-Id:" + message.getMessageId();
		msg += "\nContent-Type:" + message.getContentType();

		if (message.getDate() != null)
			msg += "\nDate: " + formatter.format(message.getDate());

		if (message.getExpires() != null)
			msg += "\nExpires: " + formatter.format(message.getExpires());

		if (message.getReference() != null)
			msg += "\nReference-Id: " + message.getReference().getMessageId();

		if (message.getContent() != null && !message.getContent().isEmpty())
			msg += "\n\n" + message.getContent();

		return msg;
	}
}
