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

package at.ac.tuwien.auto.colibri.messaging.types;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.SyntaxException;

public abstract class Get extends Message
{
	protected Observe observe;

	protected Date to;

	protected Date from;

	protected URI service = null;

	public Get()
	{
		super();

		try
		{
			this.setContentType(ContentType.PLAIN);
		}
		catch (InterfaceException e)
		{
			// do nothing
		}

		this.setApproved(true);
	}

	public final void setObserve(Observe observe)
	{
		this.observe = observe;
	}

	public final String getMessageType()
	{
		return "GET";
	}

	public final String getContent()
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

	public final void setContent(String content) throws SyntaxException
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

	public final Date getTo()
	{
		return to;
	}

	public final Date getFrom()
	{
		return from;
	}

	public final URI getService()
	{
		return service;
	}

	public final Observe getObserve()
	{
		return observe;
	}

	@Override
	public void setContentType(ContentType contentType) throws InterfaceException
	{
		// check content type
		if (contentType != ContentType.PLAIN)
			throw new SyntaxException("content type must be text/plain", this);

		super.setContentType(contentType);
	}
}
