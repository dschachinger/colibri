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

package at.ac.tuwien.auto.colibri.optimization.connection;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import at.ac.tuwien.auto.colibri.messaging.Peer;
import at.ac.tuwien.auto.colibri.messaging.Storage;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.handling.Marshaller;
import at.ac.tuwien.auto.colibri.messaging.handling.Unmarshaller;
import at.ac.tuwien.auto.colibri.messaging.types.Message;
import at.ac.tuwien.auto.colibri.optimization.messaging.handling.ClientUnmarshaller;

/**
 * This is the WebSocket client endpoint for the Colibri optimization.
 */
@ClientEndpoint
public class ClientSocketEndpoint implements Peer
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ClientSocketEndpoint.class.getName());

	/**
	 * Reference to own session
	 */
	private Session session;

	/**
	 * Connection indicator
	 */
	private CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Deserializer for incoming messages
	 */
	private Unmarshaller unmarshaller = null;

	/**
	 * Serializer for outgoing messages
	 */
	private Marshaller marshaller = null;

	/**
	 * Buffer message
	 */
	private Message incoming = null;

	/**
	 * Central message storage
	 */
	private Storage storage = null;

	/**
	 * Returns currently arrived message.
	 * 
	 * @return Incoming message
	 */
	public Message poll()
	{
		Message m = incoming;
		incoming = null;
		return m;
	}

	/**
	 * Checks if there is a new message.
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return (this.incoming == null);
	}

	/**
	 * Waits until connection is opened.
	 */
	public void await()
	{
		try
		{
			this.latch.await();
		}
		catch (InterruptedException e)
		{
			log.severe(e.getMessage());
		}
	}

	/**
	 * Sends a message to the remote endpoint.
	 * 
	 * @param message Message that needs to be sent
	 */
	public void send(Message message)
	{
		log.info("Outgoing message (peer = " + this.toString() + ")");

		try
		{
			// set receiver
			message.setPeer(this);

			// create serialized message
			String msg = marshaller.convert(message);

			// store message
			storage.addMessage(message);

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
	 * Method is called when connection is established.
	 * 
	 * @param session Reference to current session
	 */
	@OnOpen
	public void onOpen(Session session)
	{
		// initialize references
		this.session = session;

		// disable idle timeout
		this.session.setMaxIdleTimeout(0);

		// create log message
		log.info("Session opened (peer = " + this.toString() + ")");

		// initialization of objects
		this.storage = new Storage();
		this.unmarshaller = new ClientUnmarshaller(this, storage);
		this.marshaller = new Marshaller(this);

		// continue
		latch.countDown();
	}

	/**
	 * Method is called when incoming message is received.
	 * 
	 * @param session Reference to current session
	 * @param message Incoming message
	 */
	@OnMessage
	public void onMessage(String message, Session session)
	{
		log.info("Incoming message (peer = " + this.toString() + ")");

		try
		{
			// parse new message string
			Message msg = unmarshaller.convert(message);

			// add message to storage
			storage.addMessage(msg);

			// TODO: IMPORTANT ISSUE: handle not expected messages (e.g. detach from core)

			// set message
			this.incoming = msg;

			// notify waiting object
			synchronized (this)
			{
				this.notify();
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
}
