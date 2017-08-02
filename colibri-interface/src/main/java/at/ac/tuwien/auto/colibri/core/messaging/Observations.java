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

package at.ac.tuwien.auto.colibri.core.messaging;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.core.messaging.queue.MessageQueue.QueueType;
import at.ac.tuwien.auto.colibri.core.messaging.queue.QueueHandler;
import at.ac.tuwien.auto.colibri.core.messaging.tasks.Observation;
import at.ac.tuwien.auto.colibri.core.messaging.tasks.Transmission;
import at.ac.tuwien.auto.colibri.core.messaging.types.DeregisterImpl;
import at.ac.tuwien.auto.colibri.core.messaging.types.DetachImpl;
import at.ac.tuwien.auto.colibri.messaging.Config;
import at.ac.tuwien.auto.colibri.messaging.Peer;
import at.ac.tuwien.auto.colibri.messaging.Registry;
import at.ac.tuwien.auto.colibri.messaging.Storage;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.types.Message;
import at.ac.tuwien.auto.colibri.messaging.types.Observe;

/**
 * The registry manages all registered peers, the observations as well as all transmissions.
 */
public class Observations
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(Registry.class.getName());

	/**
	 * Registered periodic and non-periodic observations
	 */
	private List<Observation> observations;

	/**
	 * Registered, unconfirmed transmissions
	 */
	private Map<Message, Transmission> confirmables;

	public Storage storage = null;

	// /**
	// * Ontology
	// */
	// private Model ontology;

	private static Observations instance = null;

	public static Observations getInstance()
	{
		if (instance == null)
			instance = new Observations();
		return instance;
	}

	/**
	 * Initialization
	 */
	private Observations()
	{
		log.info("Starting registry");

		// initialize collections
		this.observations = new ArrayList<Observation>();
		this.confirmables = new HashMap<Message, Transmission>();
		this.storage = new Storage();
	}

//	/**
//	 * Gets TBox of Colibri ontology
//	 * 
//	 * @return ontology
//	 */
//	private Model getOntology()
//	{
//		if (this.ontology == null)
//		{
//			this.ontology = ModelFactory.createOntologyModel();
//			this.ontology.read(Config.getInstance().ontology);
//		}
//		return this.ontology;
//	}

	/**
	 * Adds an observation to the collection.
	 * 
	 * @param message Observe message
	 * @throws ProcessingException
	 */
	public synchronized void addObservation(Observe message) throws ProcessingException
	{
		// check if peer is registered
		if (Registry.getInstance().getConnector(message.getPeer()) == null)
			throw new ProcessingException("peer not yet registered as connector", message.getPeer());

		// check if service is already observed
		if (getObservation(message.getPeer(), message.getService()) != null)
			throw new ProcessingException("service already observed by this peer", message.getPeer());

		// add observation
		this.observations.add(new Observation(message));

		// set log
		log.info("Observation added (peer = " + message.getPeer().toString() + ", service = " + message.getService().toString() + ")");
	}

	/**
	 * Retrieves an open observation for a peer and a service.
	 * 
	 * @param peer Observing peer
	 * @param service Observed service
	 * @return Observation object
	 */
	private synchronized Observation getObservation(Peer peer, URI service)
	{
		for (Observation o : this.observations)
		{
			if (o.getObserve().getPeer().equals(peer) && o.getObserve().getService().toString().toUpperCase().equals(service.toString().toUpperCase()))
				return o;
		}
		return null;
	}

	/**
	 * Retrieves all observations of a given service.
	 * 
	 * @param service Observed service
	 * @return List of observation objects
	 */
	private synchronized List<Observation> getObservations(URI service)
	{
		return getObservations(service, true);
	}

	/**
	 * Retrieves some observations of a given service.
	 * 
	 * @param service Observed service
	 * @param includePeriodic Specifies if the result set contains all observations or only
	 *        non-periodic observations
	 * @return List of a service's relevant observations
	 */
	public synchronized List<Observation> getObservations(URI service, boolean includePeriodic)
	{
		List<Observation> list = new ArrayList<Observation>();

		for (Observation o : this.observations)
		{
			if (o.getObserve().getService().toString().toUpperCase().equals(service.toString().toUpperCase()))
			{
				if (!(o.isPeriodic() && !includePeriodic))
					list.add(o);
			}
		}
		return list;
	}

//	/**
//	 * Retrieves all observations of a given peer.
//	 * 
//	 * @param peer Observing peer
//	 * @return List of observation objects
//	 */
//	private synchronized List<Observation> getObservations(Peer peer)
//	{
//		List<Observation> list = new ArrayList<Observation>();
//
//		for (Observation o : this.observations)
//		{
//			if (o.getObserve().getPeer().equals(peer))
//				list.add(o);
//		}
//		return list;
//	}

	/**
	 * Removes an observation
	 * 
	 * @param peer Observing peer
	 * @param service Observed service URI
	 */
	public synchronized void removeObservation(Peer peer, URI service)
	{
		// get observation
		Observation o = this.getObservation(peer, service);

		// remove observation
		if (o != null)
		{
			// cancel task
			o.cancel();

			// remove from list
			this.observations.remove(o);
		}

		// set log
		log.info("Observation removed (peer = " + peer.toString() + ", service = " + service.toString() + ")");
	}

	/**
	 * Removes all observations for a given service.
	 * 
	 * @param service Observed service
	 * @return List of removed observe messages
	 */
	public synchronized List<Observe> removeObservations(URI service)
	{
		List<Observe> messages = new ArrayList<Observe>();

		// get all relevant observations
		for (Observation o : this.getObservations(service))
		{
			// cancel observation
			o.cancel();

			// remove from list
			observations.remove(o);

			// add to return list
			messages.add(o.getObserve());
		}

		// set log
		log.info("Observations removed (service = " + service.toString() + ")");

		return messages;
	}

	/**
	 * Creates a transmission and adds confirmable transmissions to a watch list.
	 * 
	 * @param message Message that should be transmitted
	 */
	public synchronized void addTransmission(Message message)
	{
		// create the transmission
		Transmission t = new Transmission(message, Config.getInstance().interval, Config.getInstance().retries);

		// add transmission to the list
		if (message.isConfirmable())
			this.confirmables.put(message, t);
	}

	// /**
	// * Get all unconfirmed transmissions per peer.
	// *
	// * @param peer Peer object
	// * @return List of transmissions
	// */
	// private synchronized List<Transmission> getTransmissions(Peer peer)
	// {
	// List<Transmission> transmissions = new ArrayList<Transmission>();
	//
	// for (Transmission t : this.confirmables.values())
	// {
	// if (t.getMessage().getPeer().equals(peer))
	// transmissions.add(t);
	// }
	//
	// return transmissions;
	// }

	/**
	 * Removes a transmission from the list of unconfirmed transmissions.
	 * 
	 * @param message Message of transmission
	 */
	public synchronized void removeTransmission(Message message)
	{
		// TODO undo the things that are done by this message (e.g. observation does not work)

		if (message == null)
			return;

		Transmission t = this.confirmables.remove(message);

		if (t != null)
		{
			t.cancel();
			log.info("Transmission removed (message-id = " + message.toString() + ")");
		}
	}

	/**
	 * Cleans up the registry.
	 */
	public synchronized void clean()
	{
		log.info("Starting registry cleanup");

		// clean observations
		for (Observation o : this.observations)
		{
			// cancel observation
			o.cancel();

			// create detach message
			DetachImpl detach = new DetachImpl();
			detach.setConfirmable(false);
			detach.setPeer(o.getObserve().getPeer());
			detach.setContent(o.getObserve().getService());
			detach.setReference(o.getObserve());

			// send message
			QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(detach);
		}
		this.observations.clear();

		// clean peers
		List<Peer> cleanPeers = new ArrayList<Peer>();

		for (Peer p : Registry.getInstance().getPeers())
		{
			// create deregister message
			DeregisterImpl deregister = new DeregisterImpl();
			deregister.setPeer(p);
			deregister.setConfirmable(false);
			deregister.setContent(Registry.getInstance().getConnector(p));

			// send message
			QueueHandler.getInstance().getQueue(QueueType.OUTPUT).addInternal(deregister);

			cleanPeers.add(p);
		}

		for (Peer p : cleanPeers)
			Registry.getInstance().removeConnector(p);

		// clean unconfirmed transmissions
		for (Transmission r : this.confirmables.values())
		{
			// cancel transmission
			r.cancel();
		}
		this.confirmables.clear();

		log.info("Registry cleanup finished");
	}
}
