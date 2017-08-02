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

package at.ac.tuwien.auto.colibri.core.datastore.reasoner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class ReasonerRunnable implements Runnable
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ReasonerRunnable.class.getName());

	private List<TaskListener> listeners = Collections.synchronizedList(new ArrayList<TaskListener>());
	
	private Model model = null;
	private ReasonerLevel reasoningLevel = ReasonerLevel.REASONING_FULL;
	
	public ReasonerRunnable(Model model, ReasonerLevel reasonerLevel) {
		this.model = model;
	}

	@Override
	public void run()
	{
		log.info("Reasoner thread startet");
		try {
			doReasoning();
		} catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e) {
			e.printStackTrace();
		}
		log.info("Reasoner thread finished");
	}

	/**
	 * Sets up the reasoner and does the reasoning. The inferred ontology is written
	 * to an OWL file.
	 */
	private void doReasoning() throws OWLOntologyStorageException, IOException, OWLOntologyCreationException
	{
		Reasoner r = new Reasoner(model);
		Model m = r.doReasoning(reasoningLevel, false);
		notifyListeners(m);
	}

	/**
	 * Add new listener that should be notified when reasoning is finished
	 *
	 * @param listener TaskListener to notify
	 */
	public void addListener(TaskListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove listener that should be notified when reasoning is finished
	 *
	 * @param listener TaskListener to remove
	 */
	public void removeListener(TaskListener listener)
	{
		listeners.remove(listener);
	}

	private final void notifyListeners(Model m)
	{
		synchronized (listeners)
		{
			for (TaskListener listener : listeners)
			{
				listener.threadFinished(this, m);
			}
		}
	}
}
