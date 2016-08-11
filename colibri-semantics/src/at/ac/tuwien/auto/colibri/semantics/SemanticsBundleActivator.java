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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import at.ac.tuwien.auto.colibri.data.AccessService;

/**
 * This class is the activator for the semantic core OSGi bundle.
 * 
 * @author dschachinger
 */
public class SemanticsBundleActivator implements BundleActivator
{
	/**
	 * Used to store context
	 */
	private static BundleContext context;

	/**
	 * Returns context of this bundle
	 * 
	 * @return
	 */
	static BundleContext getContext()
	{
		return context;
	}

	/**
	 * Service registration object for providing access service objects
	 */
	private ServiceRegistration<?> serviceRegistration;

	/**
	 * OSGi start method
	 */
	public void start(BundleContext bundleContext) throws Exception
	{
		// assign context
		SemanticsBundleActivator.context = bundleContext;

		// create and register service factory
		AccessServiceFactory serviceFactory = new AccessServiceFactory();
		serviceRegistration = context.registerService(AccessService.class.getName(), serviceFactory, null);
	}

	/**
	 * OSGi stop method
	 */
	public void stop(BundleContext bundleContext) throws Exception
	{
		// clear context
		SemanticsBundleActivator.context = null;

		// unregister service
		serviceRegistration.unregister();
	}

}
