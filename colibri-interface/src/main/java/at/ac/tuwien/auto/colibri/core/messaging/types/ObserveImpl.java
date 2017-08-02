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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.Observations;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.core.messaging.tasks.Observation;
import at.ac.tuwien.auto.colibri.messaging.Peer;
import at.ac.tuwien.auto.colibri.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.messaging.Registry;
import at.ac.tuwien.auto.colibri.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.messaging.types.Observe;

public class ObserveImpl extends Observe implements Processible
{
	@Override
	public void process(Datastore store) throws InterfaceException
	{
		// check if URI exists
		String connectorUri = null;
		try
		{
			String query = "SELECT ?s ?t WHERE { ?s rdf:type colibri:DataService. ?s colibri:hasTechnologyConnector ?t. FILTER (?s = <" + this.getService().toString() + ">)}";

			ResultSet r = store.select(QueryBuilder.getPrefixedQuery(query));

			if (r.hasNext())
			{
				QuerySolution s = r.nextSolution();
				connectorUri = s.get("t").toString();
			}
			else
			{
				throw new Exception("No technology connector for the observed service was found");
			}

			if (r.hasNext())
			{
				throw new Exception("More than one technology connector for the observed service was found");
			}
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		if (connectorUri == null || connectorUri.isEmpty())
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

		// send permanent observation to host of the service
		try
		{
			// get service's host
			Peer host = Registry.getInstance().getPeer(new URI(connectorUri));

			// check if observe should be sent to host
			boolean empty = true;
			boolean hostOnly = true;
			for (Observation o : Observations.getInstance().getObservations(this.getService(), true))
			{
				empty = false;

				if (o.getObserve().getPeer() != host)
					hostOnly = false;
			}

			// send observe
			if (empty || hostOnly)
			{
				ObserveImpl obs = new ObserveImpl();

				obs.setPeer(host);
				obs.setContent(this.getService().toString());
				obs.setContentType(this.getContentType());

				// do not create observe cycle
				if (obs.getPeer() != this.getPeer())
					QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(obs);
			}
		}
		catch (Exception e)
		{
			throw new ProcessingException(e.getMessage(), this);
		}

		// register observation
		Observations.getInstance().addObservation(this);

		// create status
		StatusImpl result = new StatusImpl();
		result.setReference(this);
		result.setPeer(this.getPeer());

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(result);

	}
}
