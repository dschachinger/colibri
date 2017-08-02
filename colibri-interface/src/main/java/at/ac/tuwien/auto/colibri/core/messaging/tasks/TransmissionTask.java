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

package at.ac.tuwien.auto.colibri.core.messaging.tasks;

import java.util.TimerTask;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.Observations;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.TransmissionException;
import at.ac.tuwien.auto.colibri.messaging.types.Message;

/**
 * This task is
 * import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
 * import at.ac.tuwien.auto.colibri.messaging.exceptions.TransmissionException;
 * import at.ac.tuwien.auto.colibri.messaging.types.Message; used to send a message to its peer.
 */
public class TransmissionTask extends TimerTask
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ObservationTask.class.getName());

	/**
	 * Message to be sent
	 */
	private Message message;

	/**
	 * Max number of retries
	 */
	private int max;

	/**
	 * Current number of send calls
	 */
	private int count;

	/**
	 * Initialization of send task.
	 * 
	 * @param registry Registry
	 * @param message Message to be sent
	 * @param maxRetries Max number of retries
	 */
	public TransmissionTask(Message message, int maxRetries)
	{
		this.count = 0;
		this.message = message;
		this.max = maxRetries;
	}

	/**
	 * Sending of message to its peer.
	 * 
	 * @param message Message
	 * @throws InterfaceException
	 */
	public void send(Message message) throws InterfaceException
	{
		message.getPeer().send(message);
	}

	@Override
	public void run()
	{
		log.info("Starting send task (message-id = " + message.toString() + ")");

		// max number of retries reached
		if (count > max)
		{
			// create exception
			InterfaceException e = new TransmissionException("Message was not acknowledged by receiver", message);

			// send exception's status
			try
			{
				this.send(e.getStatus());
			}
			catch (InterfaceException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// remove this transmission
			Observations.getInstance().removeTransmission(message);

			// end execution
			return;
		}

		// set log
		if (count == 0)
			log.info("Send message (message-id = " + message.toString() + ")");
		else
			log.info("Retry message sending (attempt = " + count + ", message-id = " + message.toString() + ")");

		// send message
		try
		{
			this.send(this.message);
		}
		catch (InterfaceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// increment count
		this.count++;
	}

}
