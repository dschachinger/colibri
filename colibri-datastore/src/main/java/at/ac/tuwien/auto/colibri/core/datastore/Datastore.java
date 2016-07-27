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

package at.ac.tuwien.auto.colibri.core.datastore;

import java.util.Iterator;

public class Datastore
{
	String ns_ontology = "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#";
	String ns_example = "http://www.auto.tuwien.ac.at/example/";
	String default_dir = "C:\\dschachinger\\Dokumente\\Eclipse\\colibri\\files";

	// public OWLDataFactory factory;
	// public OWLOntology ontology;
	// public PelletReasoner reasoner;

	public Datastore()
	{
		System.out.println("datastore constructor");
	}

	private void loadOntology()
	{
		// try
		// {
		// File file = new File(default_dir + "\\temp.owl");
		//
		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// ontology = manager.loadOntologyFromOntologyDocument(file);
		//
		// PelletReasonerFactory reasonerFactory =
		// PelletReasonerFactory.getInstance();
		// reasoner = reasonerFactory.createReasoner(ontology);
		//
		// reasoner.precomputeInferences();
		//
		// System.out.println("Ontology is consistent: " +
		// reasoner.isConsistent());
		//
		// factory = manager.getOWLDataFactory();
		//
		// OWLDataProperty p = factory.getOWLDataProperty(IRI.create(ns_ontology
		// + "orientation"));
		//
		// OWLClassExpression ex1 = factory.getOWLDataHasValue(p,
		// factory.getOWLLiteral(0));
		// // OWLClassExpression ex2 =
		// // factory.getOWLClass(IRI.create(ns_ontology +
		// // "SouthboundDelimiter"));
		// // OWLClassExpression ex3 =
		// // factory.getOWLClass(IRI.create(ns_ontology +
		// // "EastboundDelimiter"));
		//
		// System.out.println("Result set: ");
		// Set<OWLNamedIndividual> result = reasoner.getInstances(ex1,
		// true).getFlattened();
		// for (OWLNamedIndividual owlNamedIndividual : result)
		// System.out.println(owlNamedIndividual);
		// System.out.println("-END-");
		// }
		// catch (OWLOntologyCreationException e1)
		// {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		// model =
		// ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

		// Probieren: SPARQL query mit inferences

		// Mail an Entwickler: Backtrack-Error

		/*
		 * try { model.read(new FileInputStream(file), ns_example, "RDF/XML"); } catch
		 * (FileNotFoundException e1) { e1.printStackTrace(); }
		 * 
		 * PelletReasonerFactory.theInstance().create().bindSchema( model);
		 */
	}

	private void saveOntology()
	{
		// final JFileChooser fc = new JFileChooser();
		//
		// fc.setCurrentDirectory(new File(default_dir));
		//
		// // In response to a button click:
		// int returnVal = fc.showSaveDialog(Semantics.this);
		//
		// if (returnVal == JFileChooser.APPROVE_OPTION)
		// {
		// File file = fc.getSelectedFile();
		//
		// try
		// {
		// model.write(new FileOutputStream(file), "RDF/XML");
		// }
		// catch (FileNotFoundException e1)
		// {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// }
	}

	private void addStatement()
	{
		// Individual r = model.getIndividual(ns_example +
		// textFieldSubjekt.getText().trim());
		//
		// if (r == null)
		// r = model.createIndividual(ns_example +
		// textFieldSubjekt.getText().trim(), null);
		//
		// Property p =
		// model.getProperty(comboBoxP.getSelectedItem().toString() +
		// textFieldPraedikat.getText().trim());
		//
		// if (chckbxDataValue.isSelected())
		// {
		// Literal l = null;
		//
		// switch (comboBoxType.getSelectedItem().toString())
		// {
		// case "string":
		// l =
		// model.createTypedLiteral(textFieldObjekt.getText().trim());
		// break;
		// case "int":
		// l =
		// model.createTypedLiteral(Integer.parseInt(textFieldObjekt.getText().trim()));
		// break;
		// case "float":
		// l =
		// model.createTypedLiteral(Float.parseFloat(textFieldObjekt.getText().trim()));
		// break;
		// case "long":
		// l =
		// model.createTypedLiteral(Long.parseLong(textFieldObjekt.getText().trim()));
		// break;
		// }
		// r.addLiteral(p, l);
		// }
		// else
		// {
		// r.addProperty(p,
		// model.getOntResource(comboBoxObjekt.getSelectedItem().toString()
		// + textFieldObjekt.getText().trim()));
		// }
		//
		// try
		// {
		// printIterator(r.listRDFTypes(true), "Types from " +
		// r.getLocalName());
		// }
		// catch (InconsistentOntologyException ex)
		// {
		// textAreaOutput.append("\n\n" + ex.getMessage());
		// }
	}

	private void runQuery()
	{
		// QueryParser parser = QueryEngine.getParser();
		//
		// KnowledgeBase kb = reasoner.getKB();
		//
		// String qs = "...";
		//
		// Query query = parser.parse(qs, kb);
		//
		// // Query query = QueryFactory.create(textArea.getText());
		// // QueryExecution qe = QueryExecutionFactory.create(query, ontology);
		// // ResultSet results = qe.execSelect();
		//
		// QueryResult results = QueryEngine.exec(query);
		//
		// System.out.println(results.toString());
	}

	private void printRelations()
	{
		// OntResource r = model.getOntResource();

		// OWLNamedIndividual r = factory.getOWLNamedIndividual(ns_example +
		// textFieldSubjekt.getText());
		// printIterator(r.listProperties(), "properties");
		//
		// printIterator(r.listRDFTypes(true), "temp");
		// printIterator(r.listRDFTypes(false), "temp");
	}

	public void printIterator(Iterator<?> i, String header)
	{
		// System.out.println(header);
		// for (int c = 0; c < header.length(); c++)
		// System.out.print("=");
		// System.out.println();
		//
		// if (i.hasNext())
		// {
		// while (i.hasNext())
		// System.out.println(i.next());
		// }
		// else
		// System.out.println("<EMPTY>");
		//
		// System.out.println();
	}

	private void parseCsv()
	{
		// try
		// {
		// parser = CSVParser.parse(file, Charset.defaultCharset(),
		// CSVFormat.DEFAULT);
		//
		// String ns_ontology =
		// "https://raw.githubusercontent.com/dschachinger/colibri/master/res/colibri.owl#";
		// String ns_example = "http://www.auto.tuwien.ac.at/example/";
		//
		// int column1 = comboBoxParam1.getSelectedIndex();
		// int column2 = comboBoxParam2.getSelectedIndex();
		//
		// boolean first = true;
		//
		// List<CSVRecord> records = parser.getRecords();
		//
		// int count = 0;
		// progressBar.setMaximum(records.size());
		//
		// for (CSVRecord record : records)
		// {
		// progressBar.setValue(++count);
		//
		// progressBar.repaint();
		//
		// if (first)
		// {
		// first = false;
		// continue;
		// }
		//
		// Individual datavalue = model.createIndividual(ns_example +
		// "datavalue_" + count + System.currentTimeMillis(),
		// model.getResource(ns_ontology + "DataValue"));
		//
		// Resource dataservice =
		// model.getResource(comboBoxDataService.getSelectedItem().toString());
		// dataservice.addProperty(model.getProperty(ns_ontology +
		// "hasDataValue"), datavalue);
		//
		// Individual value1 = model.createIndividual(ns_example + "value_1_" +
		// count + System.currentTimeMillis(), model.getResource(ns_ontology +
		// "Value"));
		// Individual value2 = model.createIndividual(ns_example + "value_2_" +
		// count + System.currentTimeMillis(), model.getResource(ns_ontology +
		// "Value"));
		//
		// datavalue.addProperty(model.getProperty(ns_ontology + "hasValue"),
		// value1);
		// datavalue.addProperty(model.getProperty(ns_ontology + "hasValue"),
		// value2);
		//
		// value1.addProperty(model.getProperty(ns_ontology + "hasParameter"),
		// model.getIndividual(lblParam.getText()));
		// value2.addProperty(model.getProperty(ns_ontology + "hasParameter"),
		// model.getIndividual(lblParam_1.getText()));
		//
		// value1.addLiteral(model.getProperty(ns_ontology + "value"),
		// Import.this.parseLiteral(record.get(column1)));
		// value2.addLiteral(model.getProperty(ns_ontology + "value"),
		// Import.this.parseLiteral(record.get(column2)));
		//
		// }
		//
		// setVisible(false);
		// dispose();
		//
		// }
		// catch (IOException e1)
		// {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// // todo: csv auslesen und datavalues anlegen
	}

	private void parseCsvHeader()
	{
		// file = fc.getSelectedFile();
		//
		// try
		// {
		// parser = CSVParser.parse(file, Charset.defaultCharset(),
		// CSVFormat.DEFAULT);
		//
		// CSVRecord header = parser.getRecords().get(0);
		//
		// for (int i = 0; i < header.size(); i++)
		// {
		// comboBoxParam1.addItem(header.get(i));
		// comboBoxParam2.addItem(header.get(i));
		// }
		//
		// }
		// catch (IOException e1)
		// {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
	}

	// private Literal parseLiteral(String value)
	// {
	// Literal l = null;

	// try
	// {
	//
	// String v1 = value.replace(' ', 'T');
	// XSDDateTime dateTime = (XSDDateTime)
	// XSDDatatype.XSDdateTime.parse(v1);
	//
	// l = model.createTypedLiteral(dateTime);
	// }
	// catch (Exception ex)
	// {
	// try
	// {
	// l = model.createTypedLiteral(Integer.parseInt(value));
	// }
	// catch (Exception ex1)
	// {
	// try
	// {
	// l = model.createTypedLiteral(Long.parseLong(value));
	// }
	// catch (Exception ex2)
	// {
	// try
	// {
	// l = model.createTypedLiteral(Float.parseFloat(value));
	// }
	// catch (Exception ex3)
	// {
	// l = model.createTypedLiteral(value);
	// }
	// }
	// }
	// }
	// return l;
	// }

	public static void main(String[] args)
	{
		Datastore ds = new Datastore();

		ds.loadOntology();

	}

}
