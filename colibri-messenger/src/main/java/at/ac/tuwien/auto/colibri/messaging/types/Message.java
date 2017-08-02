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

import java.util.Date;
import java.util.Random;

import at.ac.tuwien.auto.colibri.messaging.Peer;
import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;

public abstract class Message
{
	public enum ContentType
	{
		PLAIN("text/plain"), TURTLE("application/x-turtle"), RDF_XML("application/rdf+xml"), SPARQL_QUERY("application/sparql-query"), SPARQL_UPDATE("application/sparql-update"), SPARQL_RESULT_JSON("application/sparql-result+json"), SPARQL_RESULT_XML("application/sparql-result+xml");

		private final String contentType;

		private ContentType(final String contentType)
		{
			this.contentType = contentType;
		}

		@Override
		public String toString()
		{
			return this.contentType;
		}

		public static ContentType getContentType(String contentType)
		{
			for (ContentType v : values())
			{
				if (v.toString().equals(contentType))
					return v;
			}
			return null;
		}
	}

	private Peer peer = null;

	private boolean confirmable = false;
	private boolean approved = false;

	private String messageId = null;
	private Date expires = null;
	private Date date = null;
	private Message reference = null;
	private ContentType contentType = null;

	private String content = null;

	public Message()
	{
		this(Long.toString(System.nanoTime() + new Random().nextInt()));
	}

	public Message(String messageId)
	{
		this.messageId = messageId;
		this.date = new Date();
		this.content = "";
	}

	@Override
	public String toString()
	{
		return this.messageId.toString();
	}

	public abstract String getMessageType();

	public boolean isApproved()
	{
		return this.approved;
	}

	protected void setApproved(boolean approved)
	{
		this.approved = approved;
	}

	public boolean isConfirmable()
	{
		return this.confirmable;
	}

	public void setConfirmable(boolean confirmable)
	{
		this.confirmable = confirmable;
	}

	public Peer getPeer()
	{
		return this.peer;
	}

	public void setPeer(Peer peer)
	{
		this.peer = peer;
	}

	public String getMessageId()
	{
		return this.messageId;
	}

	public void setMessageId(String messageId)
	{
		this.messageId = messageId;
	}

	public Message getReference()
	{
		return this.reference;
	}

	public void setReference(Message reference)
	{
		this.reference = reference;
	}

	public ContentType getContentType()
	{
		return this.contentType;
	}

	public void setContentType(ContentType contentType) throws InterfaceException
	{
		this.contentType = contentType;
	}

	public Date getDate()
	{
		return this.date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public Date getExpires()
	{
		return this.expires;
	}

	public void setExpires(Date expires)
	{
		this.expires = expires;
	}

	public String getContent() throws InterfaceException
	{
		return this.content;
	}

	public void setContent(String content) throws InterfaceException
	{
		this.content = content;
	}
}
