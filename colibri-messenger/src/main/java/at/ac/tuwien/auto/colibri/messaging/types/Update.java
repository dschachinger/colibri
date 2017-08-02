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

public abstract class Update extends Message
{
	public Update()
	{
		super();

		this.setConfirmable(true);
		this.setApproved(true);

		try
		{
			this.setContentType(ContentType.SPARQL_UPDATE);
		}
		catch (InterfaceException e)
		{
			// to nothing
		}
	}

	public String getMessageType()
	{
		return "UPD";
	}

	@Override
	public void setContentType(ContentType contentType) throws InterfaceException
	{
		// check content type
		if (contentType != ContentType.SPARQL_UPDATE)
			throw new SyntaxException("content type must be application/sparql-update", this);

		super.setContentType(contentType);
	}
}
