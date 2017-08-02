package at.ac.tuwien.auto.colibri.core.datastore.reasoner;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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

public class Reasoner {
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(Reasoner.class.getName());
	
	private OWLOntology ontology = null;
	private PelletReasoner reasoner = null;
	private OWLOntologyManager manager = null;
	
	public static final int REASONING_FULL = 1;
	public static final int REASONING_WITHOUT_SWRL = 2;
	public static final int REASONING_MINIMAL = 3;
	
	public Reasoner (Model jenaModel) throws RuntimeException {
		try (PipedInputStream in = new PipedInputStream(); PipedOutputStream out = new PipedOutputStream(in)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						jenaModel.write(out, "RDF/XML", null);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument(in);
		} catch (IOException | OWLOntologyCreationException e) {
			throw new RuntimeException("Creating Reasoner failed");
		}
	}
	
	public Reasoner (OWLOntology ontology) {
		this.ontology = ontology;
		manager = OWLManager.createOWLOntologyManager();
	}
	
	/**
	 * Sets up the reasoner and does the reasoning. The inferred ontology is written
	 * to an OWL file.
	 */
	public Model doReasoning(ReasonerLevel level, boolean addImports) throws OWLOntologyStorageException, IOException, OWLOntologyCreationException
	{	
		log.info("Start reasoning with level " + level);

		/* Setup reasoner */
		if (level == ReasonerLevel.REASONING_FULL)
			PelletOptions.DL_SAFE_RULES = true;
		else
			PelletOptions.DL_SAFE_RULES = false;
		
		PelletReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();	
		reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences();

		/* Generate inferred ontology */
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<>();

		gens.add(new InferredClassAssertionAxiomGenerator());
		if (level != ReasonerLevel.REASONING_MINIMAL) {
			// gens.add( new InferredDisjointClassesAxiomGenerator());
			gens.add(new InferredEquivalentClassAxiomGenerator());
			gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
			gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
			gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
			gens.add(new InferredSubClassAxiomGenerator());
			gens.add(new InferredSubDataPropertyAxiomGenerator());
			gens.add(new InferredSubObjectPropertyAxiomGenerator());
			gens.add(new InferredPropertyAssertionGenerator());
			// gens.add( new InferredObjectPropertyCharacteristicAxiomGenerator());
		}

		OWLOntology inferredOntology = manager.createOntology();
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner, gens);
		generator.fillOntology(manager, inferredOntology);
		
		if (addImports) {
			Set<OWLImportsDeclaration> importDecs = ontology.getImportsDeclarations();
			for (OWLImportsDeclaration i : importDecs)
			{
				manager.applyChange(new AddImport(inferredOntology, new OWLImportsDeclarationImpl(i.getIRI())));
			}
		}

		log.info("Reasoning finished");
		
		Model m = convertOWLOntologyToJenaModel(inferredOntology);
		
		return m;
	}
	
	private Model convertOWLOntologyToJenaModel(OWLOntology ont) {
		Model model; 
		
		log.info("Convert OWLOntology back to Jena model");
		
		try (PipedInputStream in = new PipedInputStream(); PipedOutputStream out = new PipedOutputStream(in)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						manager.saveOntology(ont, new RDFXMLOntologyFormat(), out);
						out.close();
					} catch (OWLOntologyStorageException | IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			model = ModelFactory.createDefaultModel(); 
			model.read(in, null, "RDF/XML");
		} catch (IOException e) {
			throw new RuntimeException("Creating Reasoner failed");
		}
		
		log.info("Jena model successfully created");
		
		return model;
	}
}
