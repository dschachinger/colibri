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

package at.ac.tuwien.auto.colibri.core.messaging.types;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;

public class ObserveImpl extends MessageApprovedImpl implements Observe, Message
{
	private Duration duration = null;
	private Date time = null;
	private Date start = null;
	private long period = -1;
	private URI service = null;

	public ObserveImpl()
	{
		super();

		this.setConfirmable(true);
		this.setContentType(ContentType.PLAIN);
	}

	@Override
	public String getMessageType()
	{
		return "OBS";
	}

	@Override
	public void process(Datastore store, Registry registry) throws InterfaceException
	{
		super.process(store, registry);

		// check content type
		if (this.getContentType() != ContentType.PLAIN)
			throw new SyntaxException("content type must be text/plain", this);

		// check if URI exists
		boolean exists = false;
		try
		{
			String query = "SELECT ?s WHERE { ?s rdf:type colibri:DataService. FILTER (?s = <" + this.getService().toString() + ">)}";

			exists = store.exists(QueryBuilder.getPrefixedQuery(query));
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		if (!exists)
			throw new UnknownObjectException("service is not known (" + this.getService() + ")", this);

		// define start and period
		this.start = null;
		this.period = -1;

		if (this.time != null)
		{
			// set period
			period = 1000 * 60 * 60 * 24;

			// set date
			this.start = new Date();

			// formatter
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			// cut off time of day
			try
			{
				start = formatter.parse(formatter.format(start));
			}
			catch (ParseException e)
			{
				throw new ProcessingException("error while parsing date (" + e.getMessage() + ")", this);
			}

			// add relative start time
			start = new Date(start.getTime() + time.getTime());

			// add 24 hours if necessary
			if (start.getTime() < new Date().getTime())
				start = new Date(start.getTime() + period);
		}
		else if (this.duration != null)
		{
			// set date
			this.start = new Date();

			// set period
			this.period = this.duration.getTimeInMillis(start);
		}

		// register observation
		registry.addObservation(this);

		// create status
		StatusImpl result = new StatusImpl();
		result.setReference(this);
		result.setPeer(this.getPeer());

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(result);
	}

	@Override
	public String getContent()
	{
		String content = this.service.toString();

		if (time != null)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			content += "?freq=" + formatter.format(time);
		}
		else if (duration != null)
		{
			content += "?freq=" + duration.toString();
		}

		return content;
	}

	@Override
	public void setContent(String content) throws SyntaxException
	{
		// split content
		int index = content.indexOf("?");

		// check URI
		String uri = content;
		if (index >= 0)
			uri = content.substring(0, index);

		try
		{
			this.service = new URI(uri);
		}
		catch (URISyntaxException e)
		{
			throw new SyntaxException("URI is not valid (" + uri + ")", this);
		}

		if (index >= 0)
		{
			// check parameter
			String freq = content.substring(index + 1);

			index = freq.indexOf("=");
			if (index == -1)
				throw new SyntaxException("parameter is not well-formed (" + freq + ")", this);

			String value = freq.substring(index + 1);

			if (value.startsWith("-P"))
			{
				throw new SyntaxException("negative duration value is not allowed (" + value + ")", this);
			}
			else if (value.toUpperCase().startsWith("P"))
			{
				try
				{
					this.duration = DatatypeFactory.newInstance().newDuration(value);
				}
				catch (DatatypeConfigurationException e)
				{
					throw new SyntaxException("duration parameter value cannot be parsed (" + value + ")", this);
				}
			}
			else
			{
				try
				{
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss'Z'");
					formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
					this.time = formatter.parse(value);
				}
				catch (ParseException e)
				{
					throw new SyntaxException("time parameter value cannot be parsed (" + value + ")", this);
				}
			}
		}
	}

	@Override
	public URI getService()
	{
		return this.service;
	}

	@Override
	public Date getStart()
	{
		return this.start;
	}

	@Override
	public long getPeriod()
	{
		return this.period;
	}

	@Override
	public boolean isPeriodic()
	{
		if (this.period == -1)
			return false;
		return true;
	}
}
