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

package at.ac.tuwien.auto.colibri.generator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * This class is used to load properties from configuration files
 */
public class Generator
{
	public Generator()
	{

	}

	public int generateBuilding(int floors, int apartments) throws Exception
	{
		String owlTemplate = readFile("/apartment.txt");
		String owlFull = "";
		String owlHaus = "<owl:NamedIndividual rdf:about=\"http://www.auto.tuwien.ac.at/example/haus\"> " +
				"<rdf:type rdf:resource=\"&colibri;Building\"/>" +
				"<colibri:name rdf:datatype=\"&xsd;string\">Haus A1 - Rosa Zukunft</colibri:name>";

		for (int i = 1; i <= floors; i++)
		{
			String urlFloor = "http://www.auto.tuwien.ac.at/example/floor_" + i;
			String owlFloor = "<owl:NamedIndividual rdf:about=\"" + urlFloor + "\">" +
					"<rdf:type rdf:resource=\"&colibri;Floor\"/>" +
					"<colibri:name rdf:datatype=\"&xsd;string\">" + i + ". Stock</colibri:name>";

			for (int j = 1; j <= apartments; j++)
			{
				String urlApartment = "http://www.auto.tuwien.ac.at/example/apartment_" + Integer.toString(i) + "_" + Integer.toString(j);
				String owlApartment = owlTemplate.replace("[XXX]", Integer.toString(i) + "_" + Integer.toString(j));
				
				owlFull += "\n" + owlApartment;
				owlFloor += "<colibri:contains rdf:resource=\"" + urlApartment + "\"/>";
			}

			owlHaus += " <colibri:contains rdf:resource=\"" + urlFloor + "\"/>";
			owlFloor += "</owl:NamedIndividual>";
			owlFull += "\n" + owlFloor;
		}

		owlHaus += "</owl:NamedIndividual>";
		owlFull += "\n" + owlHaus;

		String owlGenerated = readFile("/generated.owl");
		owlGenerated = owlGenerated.replace("[XXX]", owlFull);

		FileOutputStream fo = new FileOutputStream("generated_" + floors + "_" + apartments + ".owl", false);
		OutputStreamWriter os = new OutputStreamWriter(fo);
		os.write(owlGenerated);
		os.flush();
		os.close();
		fo.close();

		return this.readTripleCount(owlGenerated);
	}

	private String readFile(String filename) throws IOException
	{
		URL url = this.getClass().getResource(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

		String line = br.readLine();

		String template = "";
		while (line != null)
		{
			template += line + "\n";
			line = br.readLine();
		}

		br.close();

		return template;
	}

	private int readTripleCount(String owl) throws Exception
	{
		InputStream is = new ByteArrayInputStream(owl.getBytes(StandardCharsets.UTF_8));

		Model model = ModelFactory.createDefaultModel();
		model.read(is, null, "RDF/XML");

		is.close();

		return model.getGraph().size();
	}

	public static void main(String[] args) throws Exception
	{
		Generator g = new Generator();

		for (int i = 0; i <= 10; i++)
		{
			for (int j = 1; j <= 10; j++)
			{
				int triples = g.generateBuilding(i, j);

				System.out.println("size\t" + i + "\t" + j + "\t" + triples);
			}
		}
	}

}
