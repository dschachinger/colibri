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

package at.ac.tuwien.auto.colibri.messaging;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;

/**
 * The registry manages all registered peers, the observations as well as all transmissions.
 */
public class Registry
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(Registry.class.getName());

	/**
	 * Registered connectors
	 */
	private Map<Peer, URI> connectors;

	/**
	 * Static instance of the registry
	 */
	private static Registry instance = null;

	/**
	 * Singleton getter
	 * 
	 * @return Registry instance
	 */
	public static Registry getInstance()
	{
		if (instance == null)
			instance = new Registry();
		return instance;
	}

	/**
	 * Initialization
	 */
	private Registry()
	{
		log.info("Starting registry");

		// initialize collections
		this.connectors = new HashMap<Peer, URI>();
	}

	/**
	 * Registers a connector.
	 * 
	 * @param peer Peer that needs to be registered
	 * @param connector URI of peer's connector
	 * @throws ProcessingException
	 */
	public synchronized void addConnector(Peer peer, URI connector) throws ProcessingException
	{
		// check availability
		if (connectors.containsKey(peer))
			throw new ProcessingException("peer already added to connector registry", peer);

		// add connector
		connectors.put(peer, connector);

		// set log
		log.info("Connector added (peer = " + peer.toString() + ")");
	}

	/**
	 * Removes a connector registration.
	 * 
	 * @param peer Removed peer
	 */
	public synchronized void removeConnector(Peer peer)
	{
		// remove peer's connector
		this.connectors.remove(peer);

		// set log
		log.info("Connector removed (peer = " + peer.toString() + ")");
	}

	/**
	 * Retrieves the connector URI of a registered peer.
	 * 
	 * @param peer Registered peer
	 * @return URI of connector
	 * @throws ProcessingException
	 */
	public synchronized URI getConnector(Peer peer)
	{
		// return URI
		return connectors.get(peer);
	}

	/**
	 * Returns the peer that is registered on the given URI
	 * 
	 * @param uri Registered URI
	 * @return peer object
	 */
	public synchronized Peer getPeer(URI uri)
	{
		for (Peer p : connectors.keySet())
		{
			if (connectors.get(p).equals(uri))
				return p;
		}
		return null;
	}

	/**
	 * Returns a list of all connected peers.
	 * 
	 * @return Peer set
	 */
	public synchronized Set<Peer> getPeers()
	{
		return this.connectors.keySet();
	}
}
