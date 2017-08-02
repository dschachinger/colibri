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

import java.rmi.activation.UnknownObjectException;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.types.Query;

public class QueryImpl extends Query implements Processible
{
	public void process(Datastore store) throws InterfaceException
	{
		try
		{
			// handle ask queries
			if (this.getContent().toUpperCase().contains("ASK"))
				throw new UnknownObjectException("ask queries are not yet supported.");

			// run query
			ResultSet rs = store.select(QueryBuilder.getPrefixedQuery(this.getContent()));

			// get variables
			List<String> vars = rs.getResultVars();

			// create content
			String content = "{ \n \"head\": { \"vars\": [";

			// add head section
			for (int i = 0; i < vars.size(); i++)
			{
				String var = vars.get(i);
				if (i > 0)
					content += ",";
				content += " \"" + var + "\"";
			}
			content += " ] },\n";
			content += "\"results\": { \n \"bindings\": [ \n";

			// add results
			boolean first = true;
			while (rs.hasNext())
			{
				QuerySolution s = rs.nextSolution();

				if (!first)
					content += ",";

				first = false;

				content += "{";

				for (int i = 0; i < vars.size(); i++)
				{
					String var = vars.get(i);

					RDFNode node = s.get(var);

					if (i > 0)
						content += ",\n   ";

					content += "\"" + var + "\" : { \"type\" : ";

					if (node != null)
					{
						if (node.isURIResource())
						{
							content += "\"uri\", \"value\": \"" + node.toString() + "\"} ";
						}
						else if (node.isLiteral())
						{
							Literal l = node.asLiteral();

							content += "\"literal\", \"value\": ";
							content += "\"" + l.getValue().toString() + "\"";

							if (l.getDatatype() != null)
							{
								content += ", \"datatype\": \"" + l.getDatatype().getURI() + "\"";
							}

							content += " } ";
						}
					}
					else
					{
						content += "\"literal\",\"value\": \"\" } ";
					}
				}

				content += "}\n";
			}

			content += "] \n } \n }";

			// set result message
			QueryResultImpl result = new QueryResultImpl();

			result.setContent(content);
			result.setReference(this);
			result.setPeer(this.getPeer());

			// send result message
			QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(result);
		}
		catch (Exception e)
		{
			// data store exception
			throw new DatastoreException(e, this);
		}
	}
}
