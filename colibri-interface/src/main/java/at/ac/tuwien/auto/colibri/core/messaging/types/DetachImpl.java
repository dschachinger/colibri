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

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.Observations;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.messaging.Registry;
import at.ac.tuwien.auto.colibri.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.messaging.types.Detach;

public class DetachImpl extends Detach implements Processible
{
	public void process(Datastore store) throws InterfaceException
	{
		// check content type
		if (this.getContentType() != ContentType.PLAIN)
			throw new SyntaxException("content type must be text/plain", this);

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

		// remove a peer's observation (of its own observations)
		Observations.getInstance().removeObservation(this.getPeer(), this.getService());

		try
		{
			// remove observation of semantic core
			if (Observations.getInstance().getObservations(this.getService(), true).isEmpty())
			{
				DetachImpl det = new DetachImpl();
				det.setService(this.getService());
				det.setPeer(Registry.getInstance().getPeer(new URI(connectorUri)));

				// send status
				QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(det);
			}

		}
		catch (URISyntaxException e)
		{
			throw new ProcessingException(e.getMessage(), this);
		}

		// create status
		StatusImpl result = new StatusImpl();
		result.setReference(this);
		result.setPeer(this.getPeer());

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(result);

	}
}
