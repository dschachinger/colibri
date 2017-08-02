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

package at.ac.tuwien.auto.colibri.core.messaging.tasks;

import at.ac.tuwien.auto.colibri.messaging.types.Observe;

/**
 * An observation hosts the observe (OBS) message and the timer for periodic execution.
 */
public class Observation extends Executable
{
	/**
	 * Observe message
	 */
	private Observe message;

	/**
	 * Constructor for initialization.
	 * 
	 * @param message Observe message
	 * @param start First time at which the task should be executed
	 * @param period Time in milliseconds between successive task executions
	 */
	public Observation(Observe message)
	{
		super(message.getStart(), message.getPeriod());

		this.message = message;

		// start timer if observation is periodic
		if (this.message.isPeriodic())
			this.init(new ObservationTask(this.message));
	}

	/**
	 * Shows if the observation is periodically executed.
	 * 
	 * @return True if observation is periodic
	 */
	public boolean isPeriodic()
	{
		return this.message.isPeriodic();
	}

	/**
	 * Returns observe message.
	 * 
	 * @return Observe message
	 */
	public Observe getObserve()
	{
		return this.message;
	}
}
