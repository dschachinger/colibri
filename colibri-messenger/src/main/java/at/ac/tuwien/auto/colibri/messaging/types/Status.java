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

import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.SyntaxException;

public class Status extends Message
{
	public enum Code
	{
		OK(200), STRUCTURE(300), CONTENT_SYNTAX(400), CONTENT_SEMANTIC(500), CONNECTION(600), INTERNAL_PROCESSING(700), ACCESS(800);

		private final int code;

		private Code(final int code)
		{
			this.code = code;
		}

		public int getCode()
		{
			return code;
		}

		public static Code findCode(int code)
		{
			for (Code c : Code.values())
			{
				if (c.getCode() == code)
					return c;
			}
			return null;
		}

		@Override
		public String toString()
		{
			return Integer.toString(code);
		}
	}

	private Code code = null;

	private String text = null;

	public Status()
	{
		this(Code.OK, "OK");
	}

	public Status(Code code, String text)
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

		this.code = code;
		this.text = text;
	}

	public final String getMessageType()
	{
		return "STA";
	}

	public final Code getCode()
	{
		return code;
	}

	public final String getText()
	{
		return this.text;
	}

	public final String getContent()
	{
		return (this.code.toString() + "  " + this.text).trim();
	}

	public final void setContent(String content) throws SyntaxException
	{
		// split content
		int index = content.indexOf(" ");

		String codeStr = content;
		if (index >= 0)
			codeStr = content.substring(0, index);

		// get code
		Code code = null;
		try
		{
			code = Code.findCode(Integer.parseInt(codeStr));
		}
		catch (NumberFormatException e)
		{
			throw new SyntaxException("error parsing status message content (" + e.getMessage() + ")", this);
		}

		if (code == null)
			throw new SyntaxException("status code not found (" + codeStr + ")", this);

		// set code
		this.code = code;

		// set text
		if (index >= 0)
		{
			this.text = content.substring(index + 1).trim();
		}
		else
		{
			this.text = "";
		}
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
