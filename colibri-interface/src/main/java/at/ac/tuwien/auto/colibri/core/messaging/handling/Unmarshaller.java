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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import at.ac.tuwien.auto.colibri.core.messaging.Peer;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.UnmarshallingException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.core.messaging.types.AddImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.DeregisterImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.DetachImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.GetImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.Message;
import at.ac.tuwien.auto.colibri.core.messaging.types.Message.ContentType;
import at.ac.tuwien.auto.colibri.core.messaging.types.MessageImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.ObserveImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.PutImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.QueryImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.QueryResultImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.RegisterImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.RemoveImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.StatusImpl;

/**
 * An unmarshaller creates a message based on a serialized message string.
 */
public class Unmarshaller
{
	/**
	 * Reference to parent peer
	 */
	private Peer peer;

	/**
	 * Formatter for parsing date strings
	 */
	private SimpleDateFormat formatter = null;

	/**
	 * Constructor for initialization of unmarshaller.
	 * 
	 * @param peer Reference to peer
	 */
	public Unmarshaller(Peer peer)
	{
		// set peer reference
		this.peer = peer;

		// create formatter for UTC
		this.formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Converts the incoming string into a message object
	 * 
	 * @param message Incoming message as string
	 * @return Message object
	 * @throws UnmarshallingException
	 * @throws SyntaxException
	 */
	public Message convert(String message) throws UnmarshallingException, SyntaxException
	{
		// empty message
		if (message == null || message.trim().isEmpty())
			throw new UnmarshallingException("Message is empty", this.peer);

		// split message into lines
		String[] lines = message.trim().split("\n");

		// line counter
		int i = 0;

		// read message type
		String type = lines[i++].trim().toUpperCase();

		// create message
		MessageImpl msg = null;
		switch (type)
		{
			case "ADD":
				msg = new AddImpl();
				break;
			case "REM":
				msg = new RemoveImpl();
				break;
			case "REG":
				msg = new RegisterImpl();
				break;
			case "DRE":
				msg = new DeregisterImpl();
				break;
			case "OBS":
				msg = new ObserveImpl();
				break;
			case "DET":
				msg = new DetachImpl();
				break;
			case "GET":
				msg = new GetImpl();
				break;
			case "PUT":
				msg = new PutImpl();
				break;
			case "QUE":
				msg = new QueryImpl();
				break;
			case "QRE":
				msg = new QueryResultImpl();
				break;
			case "STA":
				msg = new StatusImpl();
				break;
			default:
				throw new UnmarshallingException("Message type is not known [" + type + "]", this.peer);
		}

		// set peer
		msg.setPeer(peer);

		// temporary line
		String line;

		// flags for mandatory fields
		boolean messageId = false;
		boolean contentType = false;

		// read all lines before the empty line
		while (i < lines.length && !(line = lines[i++]).trim().isEmpty())
		{
			// syntax error if header is empty or ':' is missing
			if (line.indexOf(':') <= 0)
				throw new UnmarshallingException("Header field has wrong syntax [" + line + "]", msg);

			// read header and value
			String header = line.substring(0, line.indexOf(':')).trim().toUpperCase();
			String value = line.substring(line.indexOf(':') + 1).trim();

			// set header fields
			switch (header)
			{
				case "MESSAGE-ID":

					// set message id
					msg.setMessageId(value);

					// set mandatory flag
					messageId = true;
					break;

				case "CONTENT-TYPE":

					// look for content type
					ContentType ct = ContentType.getContentType(value);

					// no content type found
					if (ct == null)
						throw new UnmarshallingException("Content-Type is not known [" + value + "]", msg);

					// set content type
					msg.setContentType(ct);

					// set mandatory flag
					contentType = true;
					break;

				case "REFERENCE-ID":

					// read reference message
					Message ref = QueueHandler.getInstance().getMessage(value);

					// check if reference id was used before as message id
					if (ref == null)
						throw new UnmarshallingException("Reference id does not exist [" + value + "]", msg);

					// set reference id
					msg.setReference(ref);

					break;

				case "DATE":

					// parse and set date
					try
					{
						msg.setDate(formatter.parse(value));
					}
					catch (ParseException e)
					{
						throw new UnmarshallingException("Date cannot be formatted [" + value + ", error: " + e.getMessage() + "]", msg);
					}
					break;

				case "EXPIRES":

					// parse and set expires
					try
					{
						msg.setExpires(formatter.parse(value));
					}
					catch (ParseException e)
					{
						throw new UnmarshallingException("Expires cannot be formatted [" + value + ", error: " + e.getMessage() + "]", msg);
					}
					break;
			}
		}

		// check mandatory field message id
		if (!messageId)
			throw new UnmarshallingException("Message-Id is not set", msg);

		// check mandatory field content type
		if (!contentType)
			throw new UnmarshallingException("Content-Type is not set", msg);

		// read content
		String content = "";
		for (int j = i; j < lines.length; j++)
		{
			if (!content.isEmpty())
				content += "\n";
			content += lines[j].trim();
		}

		// set content
		msg.setContent(content);

		return msg;
	}
}
