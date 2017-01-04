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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

public class ReasonerRunnable implements Runnable
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(ReasonerRunnable.class.getName());

	private List<TaskListener> listeners = Collections.synchronizedList(new ArrayList<TaskListener>());

	private OWLOntology ontology = null;
	private PelletReasoner reasoner = null;
	private OWLOntologyManager manager = null;

	private String ontologyPath = null;
	private String outputPath = null;

	@Override
	public void run()
	{
		if (ontologyPath == null || outputPath == null)
			return;

		File file = new File(ontologyPath);
		manager = OWLManager.createOWLOntologyManager();
		try
		{
			ontology = manager.loadOntologyFromOntologyDocument(file);
			doReasoning();
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
		}
		catch (OWLOntologyStorageException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the reasoner and does the reasoning. The inferred ontology is written
	 * to an OWL file.
	 */
	private void doReasoning() throws OWLOntologyStorageException, IOException, OWLOntologyCreationException
	{
		/* Setup reasoner */
		PelletReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
		reasoner = reasonerFactory.createReasoner(ontology);
		log.info("Start precompute ...");
		reasoner.precomputeInferences();

		/* Generate inferred ontology */
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<>();
		gens.add(new InferredSubClassAxiomGenerator());
		gens.add(new InferredClassAssertionAxiomGenerator());
		// gens.add( new InferredDisjointClassesAxiomGenerator());
		gens.add(new InferredEquivalentClassAxiomGenerator());
		gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
		gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
		gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
		// gens.add( new InferredObjectPropertyCharacteristicAxiomGenerator());
		gens.add(new InferredPropertyAssertionGenerator());
		gens.add(new InferredSubDataPropertyAxiomGenerator());
		gens.add(new InferredSubObjectPropertyAxiomGenerator());

		OWLOntology inferredOntology = manager.createOntology();
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner, gens);
		generator.fillOntology(manager, inferredOntology);

		/* Add imports to inferred ontology */
		Set<OWLImportsDeclaration> importDecs = ontology.getImportsDeclarations();
		for (OWLImportsDeclaration i : importDecs)
		{
			manager.applyChange(new AddImport(inferredOntology, new OWLImportsDeclarationImpl(i.getIRI())));
		}

		/* Write inferred ontology to file */
		OWLOntologyFormat format = manager.getOntologyFormat(ontology);
		FileOutputStream outstrm = new FileOutputStream(outputPath);
		manager.saveOntology(inferredOntology, format, outstrm);
		outstrm.close();
		log.info("Inferred ontology has been written to file");

		notifyListeners();
	}

	/**
	 * Sets the path of the OWL file containing the ontology for reasoning
	 *
	 * @param ontologyPath Path of the OWL file
	 */
	public void setOntologyFile(String ontologyPath)
	{
		this.ontologyPath = ontologyPath;
	}

	/**
	 * Sets the path of the OWL file to which the inferred ontology is written
	 *
	 * @param outputPath Path of the OWL file
	 */
	public void setOutputFile(String outputPath)
	{
		this.outputPath = outputPath;
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

	private final void notifyListeners()
	{
		synchronized (listeners)
		{
			for (TaskListener listener : listeners)
			{
				listener.threadFinished(this);
			}
		}
	}
}
