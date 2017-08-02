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

package at.ac.tuwien.auto.colibri.core.connection;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.ac.tuwien.auto.colibri.core.messaging.InterfaceUnmarshaller;
import at.ac.tuwien.auto.colibri.core.messaging.Observations;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.messaging.Peer;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.handling.Marshaller;
import at.ac.tuwien.auto.colibri.messaging.handling.Unmarshaller;
import at.ac.tuwien.auto.colibri.messaging.types.Message;

/**
 * This is the WebSocket server endpoint for the Colibri semantic core.
 * 
 * Important information: No other imported package can have the same package name. Otherwise the
 * class file will not be found at Tomcat startup!
 */
@ServerEndpoint("/colibri")
public class ServerSocketEndpoint implements Peer
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ServerSocketEndpoint.class.getName());

	/**
	 * Reference to own session
	 */
	private Session session = null;

	/**
	 * Deserializer for incoming messages
	 */
	private Unmarshaller unmarshaller = null;

	/**
	 * Serializer for outgoing messages
	 */
	private Marshaller marshaller = null;

	/**
	 * Method is called when connection is established.
	 * 
	 * @param session Reference to current session
	 * @param conf Endpoint configuration
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig conf)
	{
		// initialize references
		this.session = session;

		// disable idle timeout
		this.session.setMaxIdleTimeout(0);

		// create log message
		log.info("Session opened (peer = " + this.toString() + ")");

		// initialization of objects
		this.unmarshaller = new InterfaceUnmarshaller(this, Observations.getInstance().storage);
		this.marshaller = new Marshaller(this);
	}

	/**
	 * Method is called when incoming message is received.
	 * Message is parsed and sent to the input buffer.
	 * 
	 * @param session Reference to current session
	 * @param message Incoming message
	 */
	@OnMessage
	public void onMessage(Session session, String message)
	{
		log.info("Incoming message (peer = " + this.toString() + ")");

		try
		{
			// parse new message string
			Message msg = unmarshaller.convert(message);

			// put the new message into the queue
			QueueHandler.getInstance().getQueue(QueueType.INPUT).addExternal(msg);
		}
		catch (InterfaceException e)
		{
			// send status message back
			this.send(e.getStatus());

			// set error log message
			log.severe(e.getMessage());
		}
	}

	/**
	 * Method is called when connection is closed.
	 * It is important that WebSocket connection is correctly terminated.
	 * 
	 * @param session Reference to current session
	 * @param closeReason Reason why connection was closed
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason)
	{
		log.info("Session closed (peer = " + this.toString() + ")");

		// clean queues
		QueueHandler.getInstance().getQueue(QueueType.INPUT).clean(this);
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).clean(this);
	}

	/**
	 * This method is called when an error occurs in the endpoint.
	 * 
	 * @param t Throwable object with error message
	 */
	@OnError
	public void onError(Throwable t)
	{
		log.severe("Error in session (peer = " + this.toString() + ")\n" + t.getStackTrace());
	}

	/**
	 * Sends a message to the remote peer via WebSocket connection.
	 * 
	 * @param message Message that needs to be sent
	 */
	@Override
	public void send(Message message)
	{
		log.info("Outgoing message (peer = " + this.toString() + ")");

		try
		{
			// create serialized message
			String msg = marshaller.convert(message);

			// send message to connected peer
			try
			{
				session.getBasicRemote().sendText(msg);
			}
			catch (IOException e)
			{
				log.severe(e.getMessage());
			}
		}
		catch (InterfaceException e)
		{
			// send status message back
			this.send(e.getStatus());

			// set error log message
			log.severe(e.getMessage());
		}
	}

	/**
	 * This represents the standard serialization.
	 */
	@Override
	public String toString()
	{
		return session.getId().toString();
	}

	/**
	 * Checks if two objects are equal.
	 */
	@Override
	public boolean equals(Object obj)
	{
		// type check
		if (!(obj instanceof ServerSocketEndpoint))
			return super.equals(obj);

		// equality check
		return (this.session.getId() == ((ServerSocketEndpoint) obj).session.getId());
	}
}
