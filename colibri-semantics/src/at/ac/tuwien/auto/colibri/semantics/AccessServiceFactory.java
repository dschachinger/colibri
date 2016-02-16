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

package at.ac.tuwien.auto.colibri.semantics;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import at.ac.tuwien.auto.colibri.data.AccessService;
import at.ac.tuwien.auto.colibri.semantics.impl.AccessServiceImpl;

/**
 * This factory is used to manage the requested data access service objects.
 * 
 * @author dschachinger
 */
public class AccessServiceFactory implements ServiceFactory<Object>
{
	// number of issued access service objects
	private int usageCounter = 0;

	/**
	 * Method is invoked in order to return a new access service object
	 */
	public Object getService(Bundle bundle, ServiceRegistration<Object> registration)
	{
		// increase number of issued objects
		usageCounter++;

		// log messages
		System.out.println("Create object of AccessService for " + bundle.getSymbolicName());
		System.out.println("Number of bundles using this service: " + usageCounter);

		// create new data access service object
		AccessService service = new AccessServiceImpl();

		return service;
	}

	public void ungetService(Bundle bundle, ServiceRegistration<Object> registration, Object service)
	{
		// decrease counter
		usageCounter--;

		// log messages
		System.out.println("Release object of AccessService for " + bundle.getSymbolicName());
		System.out.println("Number of bundles using this service: " + usageCounter);
	}
}
