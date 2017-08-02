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

package at.ac.tuwien.auto.colibri.optimization.messaging.handling;

import at.ac.tuwien.auto.colibri.messaging.Peer;
import at.ac.tuwien.auto.colibri.messaging.Storage;
import at.ac.tuwien.auto.colibri.messaging.exceptions.UnmarshallingException;
import at.ac.tuwien.auto.colibri.messaging.handling.Unmarshaller;
import at.ac.tuwien.auto.colibri.messaging.types.Message;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.QueryResultImpl;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.StatusImpl;

/**
 * An unmarshaller creates a message based on a serialized message string.
 */
public class ClientUnmarshaller extends Unmarshaller
{
	public ClientUnmarshaller(Peer peer, Storage storage)
	{
		super(peer, storage);
	}

	@Override
	protected Message createMessage(String type, Peer peer) throws UnmarshallingException
	{
		Message msg = null;
		switch (type)
		{
			case "ADD":
			case "REM":
			case "REG":
			case "DRE":
			case "OBS":
			case "DET":
			case "GET":
			case "PUT":
			case "QUE":
				throw new UnmarshallingException("Message type is not supported [" + type + "]", peer);
			case "QRE":
				msg = new QueryResultImpl();
				break;
			case "STA":
				msg = new StatusImpl();
				break;
			default:
				throw new UnmarshallingException("Message type is not known [" + type + "]", peer);
		}
		return msg;
	}

}
