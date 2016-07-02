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
 * 
 * This file is part of the Colibri project.
 *************************************************************************************************/

package at.ac.tuwien.auto.colibri.connector.knx.datapoint;

import at.ac.tuwien.auto.colibri.connector.knx.KNXConnector;
import at.ac.tuwien.auto.colibri.connector.knx.KNXListener;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.exception.KNXException;

/**
 * Implements the switch datapoint DPST 1.001
 * 
 * @author dschachinger
 */
public class DPST_1_1
{
	private String identifier;
	private boolean value;
	private GroupAddress group;
	private KNXConnector connector;

	/**
	 * Constructor for establishing a listener and initializing values
	 * 
	 * @param group KNX group address
	 * @param connector KNX connector object
	 */
	public DPST_1_1(GroupAddress group, KNXConnector connector)
	{
		this.identifier = Integer.toString(group.getRawAddress());
		this.group = group;
		this.connector = connector;

		// add listener
		if (connector != null && group != null)
		{
			connector.addListener(group, new KNXListener()
			{
				// implement notify method
				@Override
				public void notify(byte[] apdu)
				{
					try
					{
						// read value from bus
						DPTXlatorBoolean x = new DPTXlatorBoolean(DPTXlatorBoolean.DPT_SWITCH);
						x.setData(apdu, 0);
						
						// log message
						System.out.println("Switch for " + DPST_1_1.this.identifier + " now " + x.getValueBoolean());

						// set value to boolean flag
						setValue(x.getValueBoolean());
					}
					catch (KNXException e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Getter for value
	 * 
	 * @return current value
	 */
	public boolean isValue()
	{
		return value;
	}

	/**
	 * Setter for boolean value
	 * 
	 * @param value new value
	 */
	public void setValue(boolean value)
	{
		this.value = value;
	}

	/**
	 * Writes a value to the KNX bus
	 */
	public void write(boolean val)
	{
		connector.write(group, val);
	}
}
