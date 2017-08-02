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

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.messaging.Registry;
import at.ac.tuwien.auto.colibri.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.PermissionException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.UnknownObjectException;
import at.ac.tuwien.auto.colibri.messaging.types.Deregister;

public class DeregisterImpl extends Deregister implements Processible
{
	public void process(Datastore store) throws InterfaceException
	{
		// get peer's URI
		URI uri = Registry.getInstance().getConnector(this.getPeer());

		// check permission
		if (!this.getConnector().toString().toUpperCase().equals(uri.toString().toUpperCase()))
			throw new PermissionException("peer is only allowed to delete itself", this);

		// check if URI exists
		boolean exists = true;
		try
		{
			String query = QueryBuilder.getPrefixedQuery("SELECT ?t WHERE { ?t rdf:type colibri:TechnologyConnector. FILTER (?t = <" + this.getConnector().toString() + ">)}");
			exists = store.exists(query);
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		if (!exists)
			throw new UnknownObjectException("connector is not known (" + this.getConnector().toString() + ")", this);

		// delete connector (only connector, no linked elements)
		try
		{
			// do not delete the connector, but update the resources when connector is newly
			// registered

			// String delete = "DELETE ?s ?p ?o WHERE { ?s ?p ?o. FILTER (?s = <" +
			// this.connector.toString() + ">)}";
			// store.update(QueryBuilder.getPrefixedQuery(delete));
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

		// delete connector and its observations from registry
		// observations on still existing services of the deleted connector remain
		Registry.getInstance().removeConnector(this.getPeer());

		// create status
		StatusImpl result = new StatusImpl();
		result.setReference(this);
		result.setPeer(this.getPeer());

		// send status
		QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(result);
	}

}
