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

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import at.ac.tuwien.auto.colibri.core.messaging.Config;
import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.QueryBuilder;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.DatastoreException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.ObjectExistsException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;
import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;

public class GetImpl extends MessageApprovedImpl implements Get, Message
{
	private Observe observe;

	private Date to;

	private Date from;

	private URI service = null;

	public GetImpl()
	{
		super();

		this.setContentType(ContentType.PLAIN);
	}

	public void setObserve(Observe observe)
	{
		this.observe = observe;
	}

	@Override
	public String getMessageType()
	{
		return "GET";
	}

	@Override
	public void process(Datastore store, Registry registry) throws InterfaceException
	{
		super.process(store, registry);

		// check content type
		if (this.getContentType() != ContentType.PLAIN)
			throw new SyntaxException("content type must be text/plain", this);

		try
		{
			// check if service URI exists in ontology
			boolean exists = false;
			try
			{
				String query = QueryBuilder.getPrefixedQuery("SELECT ?s WHERE { ?s ?p ?o. FILTER (?s = <" + this.getService().toString() + ">)}");
				exists = store.exists(query);
			}
			catch (Exception e)
			{
				throw new DatastoreException(e, this);
			}

			if (!exists)
				throw new ObjectExistsException("URI does not exist (" + this.getService().toString() + ")", this);

			// create query
			String query = "SELECT ?d ?x ?x_v ?x_p ?y ?y_v ?y_p "
					+ "WHERE { "
					+ "?s rdf:type colibri:DataService. "
					+ "?s colibri:hasDataValue ?d. "
					+ "?d colibri:hasValue ?x. "
					+ "?d colibri:hasValue ?y. "
					+ "?x colibri:hasParameter ?x_p. "
					+ "?y colibri:hasParameter ?y_p. "
					+ "?x_p rdf:type colibri:TimeParameter. "
					+ "?x colibri:value ?x_v. "
					+ "?y colibri:value ?y_v. "
					+ "FILTER (?s = <" + this.getService() + ">) "
					+ "FILTER (?x != ?y) ";

			// add filter
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			if (to != null && from == null)
			{
				query += "FILTER (?x_v <= \"" + formatter.format(to) + "\"^^xsd:dateTime) ";
			}
			else if (from != null && to == null)
			{
				query += "FILTER (?x_v >= \"" + formatter.format(from) + "\"^^xsd:dateTime) ";
			}
			else if (to != null && from != null)
			{
				query += "FILTER (?x_v >= \"" + formatter.format(from) + "\"^^xsd:dateTime && ?x_v <= \"" + formatter.format(to) + "\"^^xsd:dateTime)";
			}
			query += "} ";

			if (to == null && from == null)
			{
				query += "ORDER BY DESC(xsd:dateTime(?x_v)) LIMIT 1";
			}

			// query results
			ResultSet rs = store.select(QueryBuilder.getPrefixedQuery(query));

			// map result set to RDF model
			Model m = ModelFactory.createDefaultModel();

			String ns = Config.getInstance().namespace;

			// create resources
			Resource tDataValue = m.createResource(ns + "DataValue");
			Resource tValue = m.createResource(ns + "Value");
			Property pValue = m.createProperty(ns + "value");
			Property pHasParameter = m.createProperty(ns + "hasParameter");
			Property pHasValue = m.createProperty(ns + "hasValue");

			boolean empty = !rs.hasNext();

			// process results
			while (rs.hasNext())
			{
				QuerySolution s = rs.nextSolution();

				// data value
				Resource dv = m.createResource(s.get("d").toString());

				// values
				Resource x = m.createResource(s.get("x").toString());
				Resource y = m.createResource(s.get("y").toString());

				// add statements
				m.add(dv, RDF.type, tDataValue);

				m.add(x, RDF.type, tValue);
				m.add(y, RDF.type, tValue);

				m.add(dv, pHasValue, x);
				m.add(dv, pHasValue, y);

				m.add(x, pValue, s.get("x_v").asLiteral());
				m.add(y, pValue, s.get("y_v").asLiteral());

				m.add(x, pHasParameter, m.createResource(s.get("x_p").toString()));
				m.add(y, pHasParameter, m.createResource(s.get("y_p").toString()));
			}

			if (!empty || this.observe == null)
			{
				// write model
				StringWriter sw = new StringWriter();
				m.write(sw, "RDF/XML");
				String content = sw.toString();
				sw.close();

				// create put message
				PutImpl put = new PutImpl();

				put.setContent(content);
				put.setPeer(this.getPeer());
				put.setContentType(ContentType.RDF_XML);

				// set reference message
				if (this.getObserve() != null)
					put.setReference(this.getObserve());
				else
					put.setReference(this);

				// send put message
				QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(put);
			}
		}
		catch (Exception e)
		{
			throw new DatastoreException(e, this);
		}

	}

	@Override
	public String getContent()
	{
		String content = this.service.toString();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		if (to != null)
		{
			content += "?to=" + formatter.format(to);
		}
		if (from != null)
		{
			if (to == null)
				content += "?from=" + formatter.format(from);
			else
				content += "&from=" + formatter.format(from);

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
			String params = content.substring(index + 1);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			StringTokenizer st = new StringTokenizer(params, "&");

			while (st.hasMoreTokens())
			{
				String param = st.nextToken();

				index = param.indexOf("=");

				if (index == -1)
					throw new SyntaxException("parameter is not well-formed (" + param + ")", this);

				String identifier = param.substring(0, index).trim();
				String value = param.substring(index + 1).trim();

				try
				{
					Date d = formatter.parse(value);

					if (identifier.toUpperCase().equals("TO"))
						this.to = d;
					else if (identifier.toUpperCase().equals("FROM"))
						this.from = d;
				}
				catch (ParseException e)
				{
					throw new SyntaxException("date parameter value cannot be parsed (" + value + ")", this);
				}

			}
		}
	}

	@Override
	public Date getTo()
	{
		return to;
	}

	@Override
	public Date getFrom()
	{
		return from;
	}

	@Override
	public URI getService()
	{
		return service;
	}

	@Override
	public Observe getObserve()
	{
		return observe;
	}
}
