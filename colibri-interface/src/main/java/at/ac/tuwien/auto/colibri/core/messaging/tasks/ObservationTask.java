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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.core.messaging.types.GetImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.Observe;

/**
 * This task is periodically executed to check observed services for updates.
 */
public class ObservationTask extends TimerTask
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ObservationTask.class.getName());

	/**
	 * Original observation message
	 */
	private Observe message;

	/**
	 * Date formatter
	 */
	private SimpleDateFormat formatter;

	/**
	 * Initialization of observe task.
	 * 
	 * @param message Observation message
	 */
	public ObservationTask(Observe message)
	{
		// set variable
		this.message = message;

		// initialize formatter
		this.formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void run()
	{
		log.info("Starting observe task (service = " + message.getService().toString() + ", peer = " + message.getPeer().toString() + ", period = " + message.getPeriod() + ")");

		// creating GET message
		GetImpl get = new GetImpl();

		get.setObserve(this.message);
		get.setPeer(this.message.getPeer());

		// define parameters
		Date to = new Date();
		Date from = new Date(to.getTime() - this.message.getPeriod());

		// set content
		try
		{
			get.setContent(message.getService() + "?from=" + formatter.format(from) + "&to=" + formatter.format(to));
			QueueHandler.getInstance().getQueue(QueueType.INPUT).addInternal(get);
		}
		catch (SyntaxException e)
		{
			QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(e.getStatus());
		}
	}
}
