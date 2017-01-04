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

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;

public class DetachImpl extends MessageApprovedImpl implements Detach, Message
{
	private URI service = null;

	public DetachImpl()
	{
		super();

		this.setContentType(ContentType.PLAIN);
		this.setConfirmable(true);
	}

	@Override
	public String getMessageType()
	{
		return "DET";
	}

	@Override
	public void process(Datastore store, Registry registry) throws InterfaceException
	{
		super.process(store, registry);

		// check content type
		if (this.getContentType() != ContentType.PLAIN)
			throw new SyntaxException("content type must be text/plain", this);

		// check if URI exists
		try
		{
			String query = "SELECT ?s WHERE { ?s rdf:type colibri:DataService. FILTER (?s = <" + this.getService() + ">)}";

			boolean exists = store.exists(QueryBuilder.getPrefixedQuery(query));

			if (!exists)
				throw new UnknownObjectException("service is not known (" + this.getService() + ")", this);
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		// remove a peer's observation (of its own observations)
		registry.removeObservation(this.getPeer(), this.service);

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
		return this.service.toString();
	}

	public void setContent(URI content)
	{
		try
		{
			this.setContent(content.toString());
		}
		catch (SyntaxException e)
		{
			// do nothing
		}
	}

	@Override
	public void setContent(String content) throws SyntaxException
	{
		try
		{
			this.service = new URI(content);
		}
		catch (URISyntaxException e)
		{
			throw new SyntaxException("content is not a valid URI (" + content + ")", this);
		}
	}

	@Override
	public URI getService()
	{
		return this.service;
	}
}
