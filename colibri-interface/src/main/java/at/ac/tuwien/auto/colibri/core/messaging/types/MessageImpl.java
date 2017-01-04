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

import java.util.Date;

import at.ac.tuwien.auto.colibri.core.messaging.Datastore;
import at.ac.tuwien.auto.colibri.core.messaging.Peer;
import at.ac.tuwien.auto.colibri.core.messaging.Registry;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.core.messaging.exceptions.SyntaxException;

public abstract class MessageImpl implements Message
{
	private boolean confirmable = false;

	private Peer peer = null;

	private String messageId = null;
	private Date expires = null;
	private Date date = null;
	private Message reference = null;
	private ContentType contentType = null;

	private String content = null;

	public MessageImpl()
	{
		this(Long.toString(System.nanoTime()));
	}

	public MessageImpl(String messageId)
	{
		this.messageId = messageId;
		this.date = new Date();
		this.content = "";
	}

	public abstract void process(Datastore store, Registry registry) throws InterfaceException;

	@Override
	public String toString()
	{
		return this.messageId.toString();
	}

	@Override
	public boolean isConfirmable()
	{
		return this.confirmable;
	}

	@Override
	public void setConfirmable(boolean confirmable)
	{
		this.confirmable = confirmable;
	}

	@Override
	public Peer getPeer()
	{
		return this.peer;
	}

	@Override
	public void setPeer(Peer peer)
	{
		this.peer = peer;
	}

	@Override
	public String getMessageId()
	{
		return this.messageId;
	}

	@Override
	public void setMessageId(String messageId)
	{
		this.messageId = messageId;
	}

	@Override
	public Message getReference()
	{
		return this.reference;
	}

	@Override
	public void setReference(Message reference)
	{
		this.reference = reference;
	}

	@Override
	public ContentType getContentType()
	{
		return this.contentType;
	}

	@Override
	public void setContentType(ContentType contentType)
	{
		this.contentType = contentType;
	}

	@Override
	public Date getDate()
	{
		return this.date;
	}

	@Override
	public void setDate(Date date)
	{
		this.date = date;
	}

	@Override
	public Date getExpires()
	{
		return this.expires;
	}

	@Override
	public void setExpires(Date expires)
	{
		this.expires = expires;
	}

	@Override
	public String getContent()
	{
		return this.content;
	}

	@Override
	public void setContent(String content) throws SyntaxException
	{
		this.content = content;
	}
}
