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

package at.ac.tuwien.auto.colibri.core.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class ColibriCoreActivator implements BundleActivator, ServiceListener
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ColibriCoreActivator.class.getName());

	/**
	 * Colibri core (main object)
	 */
	private ColibriCore core = null;

	/**
	 * OSGi bundle context
	 */
	private BundleContext context = null;

	@Override
	public void start(BundleContext bundleContext) throws IOException, ServletException
	{
		log.info("Starting Colibri core bundle");

		this.context = bundleContext;

		this.core = new ColibriCore();
		this.core.start();

		// bundleContext.registerService(ObjectBroker.class.getName(),
		// ObjectBrokerImpl.getInstance(), null);
		//
		// bundleContext.registerService(InterceptorBroker.class.getName(),
		// InterceptorBrokerImpl.getInstance(), null);
		//
		// ServiceReference serviceReference =
		// bundleContext.getServiceReference(MdnsResolver.class.getName());
		// if (serviceReference == null)
		// {
		// log.severe("Could not find mDNS-SD Service!");
		// }
		// else
		// {
		// resolver = (MdnsResolver) bundleContext.getService(serviceReference);
		// core.setMdnsResolver(resolver);
		// }

		this.context.addServiceListener(this);
	}

	@Override
	public void stop(BundleContext bundleContext)
	{
		log.info("Stopping Colibri core bundle");

		this.core.stop();
	}

	@Override
	public void serviceChanged(ServiceEvent event)
	{
		// String[] objectClass = (String[]) event.getServiceReference().getProperty("objectClass");
		//
		// if (event.getType() == ServiceEvent.REGISTERED)
		// {
		// if (objectClass[0].equals(MdnsResolver.class.getName()))
		// {
		//
		// synchronized (this)
		// {
		// log.info(">>>>>>>>>> Mdnssd detected.");
		// resolver = (MdnsResolver) context.getService(event.getServiceReference());
		// core.setMdnsResolver(resolver);
		// }
		// }
		// }
	}
}
