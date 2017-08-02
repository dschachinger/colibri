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

package at.ac.tuwien.auto.colibri.optimization.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.optimization.ColibriOptimizationController;
import at.ac.tuwien.auto.colibri.optimization.Config;

public class Ontology implements Serializable
{
	/**
	 * Default serial
	 */
	private static final long serialVersionUID = 1L;

	private List<Individual> indoorControl;
	private List<Individual> indoorParameters;
	private List<Individual> outdoorParameters;
	private List<Individual> zones;
	private List<Individual> indoorData;
	private List<Individual> outdoorData;
	private List<Individual> occupancyData;
	private List<Individual> gridData;
	private List<Individual> energyTypes;
	private List<Individual> energySuppliers;
	private List<Individual> energyConsumers;
	private List<Individual> energyProducers;
	private List<Individual> energyStorages;

	private transient ColibriOptimizationController optimizer = null;

	public Ontology()
	{

	}

	public void setOptimizer(ColibriOptimizationController optimizer)
	{
		this.optimizer = optimizer;
	}

	public List<Individual> getIndoorControl()
	{
		return indoorControl;
	}

	public List<Individual> getIndoorParameters()
	{
		return indoorParameters;
	}

	public List<Individual> getOutdoorParameters()
	{
		return outdoorParameters;
	}

	public List<Individual> getZones()
	{
		return zones;
	}

	public List<Individual> getIndoorData()
	{
		return indoorData;
	}
	
	public List<Individual> getEnergyStorages()
	{
		return energyStorages;
	}

	public List<Individual> getOutdoorData()
	{
		return outdoorData;
	}

	public List<Individual> getOccupancyData()
	{
		return occupancyData;
	}

	public List<Individual> getGridData()
	{
		return gridData;
	}

	public List<Individual> getEnergyTypes()
	{
		return energyTypes;
	}

	public List<Individual> getEnergySuppliers()
	{
		return energySuppliers;
	}

	public List<Individual> getEnergyConsumers()
	{
		return energyConsumers;
	}

	public List<Individual> getEnergyProducers()
	{
		return energyProducers;
	}

	public void query() throws Exception
	{
		// get all environmental parameters, which are linked to an indoor data or control service
		indoorParameters = this.query("select distinct ?t where { "
				+ "{?p rdf:type ?t."
				+ "?t rdfs:subClassOf colibri:EnvironmentalParameter."
				+ "?s colibri:monitorsParameter ?p."
				+ "?s rdf:type colibri:BuildingData. }"
				+ "UNION"
				+ "{?p rdf:type ?t."
				+ "?t rdfs:subClassOf colibri:EnvironmentalParameter."
				+ "?s colibri:controlsParameter ?p.}"
				+ "}");

		// get all modeled environmental parameters, which are linked to an outdoor data service
		outdoorParameters = this.query("select distinct ?t where { "
				+ "?p rdf:type ?t."
				+ "?t rdfs:subClassOf colibri:EnvironmentalParameter."
				+ "?s colibri:monitorsParameter ?p."
				+ "?s rdf:type colibri:WebData. }");

		// get all modeled zones
		zones = this.query("select ?z where {?z rdf:type colibri:Zone} ");

		// get states for control services
		Object[][] controlStates = this.optimizer.getResult("SELECT ?control ?state1 ?value1 ?value2 ?state2"
				+ " WHERE "
				+ " { "
				+ " 	?control rdf:type colibri:ControlService."
				+ " 	?control colibri:hasState ?state1."
				+ " 	OPTIONAL "
				+ "		{"
				+ "			{"
				+ "				?state1 colibri:value ?value1."
				+ " 			?state1 colibri:value ?value2."
				+ "			}"
				+ " 		UNION"
				+ " 	 	{"
				+ "				?state1 colibri:min ?value1."
				+ " 			?state1 colibri:max ?value2."
				+ "			}"
				+ "		}"
				+ " 	OPTIONAL "
				+ "		{"
				+ "			?control colibri:hasState ?state2. "
				+ "			?state1 colibri:isLowerThan ?state2. "
				+ "		}"
				+ " }"
				+ " ORDER BY ?control");

		// get variations for control services
		Object[][] controlVariations = this.optimizer.getResult("SELECT ?c ?t ?trend ?order "
				+ " WHERE "
				+ " { "
				+ " 	?c rdf:type colibri:ControlService."
				+ "		?c colibri:hasControlVariation ?v."
				+ "		?v colibri:hasParameter ?p."
				+ "		?p rdf:type ?t."
				+ "		?t rdfs:subClassOf colibri:EnvironmentalParameter."
				+ "		?v colibri:hasTrend ?trend."
				+ "		?v colibri:hasOrder ?order."
				+ " }");

		// get control services (indoor)
		indoorControl = new ArrayList<Individual>();

		for (Individual i : this.query("select ?z where {?z rdf:type colibri:ControlService} "))
		{
			ControlServiceImpl c = new ControlServiceImpl();
			c.setUri(i.getUri());
			c.setIndex(i.getIndex());
			indoorControl.add(c);

			for (int j = 0; j < controlStates.length; j++)
			{
				Object[] values = controlStates[j];

				if (i.getUri().equals(values[0]))
				{
					String higherUri = null;
					if (values[4] != null)
						higherUri = values[4].toString();

					c.addState(values[1].toString(), values[2], values[3], higherUri);
				}
			}

			for (int j = 0; j < controlVariations.length; j++)
			{
				Object[] values = controlVariations[j];

				if (i.getUri().equals(values[0]))
				{
					int index = -1;

					for (Individual ind : indoorParameters)
					{
						if (ind.getUri().equals(values[1].toString()))
						{
							index = ind.getIndex();
							break;
						}
					}

					if (index == -1)
						throw new Exception("Indoor parameter was not found (" + values[1].toString() + ")");

					c.addVariation(index, values[2].toString(), values[3].toString());
				}
			}

			c.generateHash();
		}

		// get all modeled data services for environmental parameters (indoor)
		indoorData = this.query("select ?z where {"
				+ " 	?z rdf:type colibri:BuildingData."
				+ "		?z colibri:monitorsParameter ?p."
				+ " 	?p rdf:type ?t."
				+ "		?t rdfs:subClassOf colibri:EnvironmentalParameter. "
				+ " }");

		// get all modeled data services for environmental parameters (outdoor), e.g. weather
		// services
		outdoorData = this.query("select ?z where {"
				+ " 	?z rdf:type colibri:WebData."
				+ "		?z colibri:monitorsParameter ?p."
				+ " 	?p rdf:type ?t."
				+ "		?t rdfs:subClassOf colibri:EnvironmentalParameter. "
				+ " }");

		// get all modeled data services (outdoor), e.g. weather services
		gridData = this.query("select ?z where {?z rdf:type colibri:GridData} ");

		// get all monitored or used energy types (assumption: there is only one grid per energy type)
		energyTypes = this.query("select distinct ?p where {"
				+ "	{"
				+ "		?p rdf:type colibri:EnergyType."
				+ "		?s rdf:type colibri:DataService."
				+ "		?s colibri:hasEnergyType ?p."
				+ "	}"
				+ "	UNION"
				+ "	{"
				+ "		?p rdf:type colibri:EnergyType."
				+ "		?s rdf:type colibri:EnergyService."
				+ "		?s colibri:hasEnergyType ?p."
				+ "	}"
				+ "	UNION"
				+ "	{"
				+ "		?p rdf:type colibri:EnergyType."
				+ "		?s rdf:type colibri:ControlService."
				+ "		?s colibri:hasEnergyType ?p."
				+ "	}"
				+ "} ");

		// get all energy providers
		energySuppliers = this.query("select ?z where {?z rdf:type colibri:EnergyService. ?z colibri:hasEnergyType ?e.} ");

		// get all energy consumers (control services and storage devices)
		energyConsumers = this.query("select ?z where {"
				+ " { ?z rdf:type colibri:ControlService. ?z colibri:hasEnergyType ?e.}"
				+ "UNION "
				+ " { ?z rdf:type colibri:EnergyStorageResource.}}");

		// get all occupancy services
		occupancyData = this.query("select ?z where {"
				+ " 	?z rdf:type colibri:BuildingData."
				+ "		?z colibri:monitorsParameter ?p."
				+ " 	?p rdf:type colibri:OccupancyParameter."
				+ "}");

		// get all energy storage supply devices
		energyStorages = this.query("select ?z where {"
				+ " ?s rdf:type colibri:EnergyStorageResource."
				+ " ?s colibri:provides ?z. "
				+ " ?z rdf:type colibri:EnergyService. "
				+ " ?z colibri:hasEnergyType ?e.}");
		
		// get all suppliers that produce energy
		energyProducers = this.query("SELECT distinct ?producer "
				+ " WHERE "
				+ " { "
				+ " 	?resource rdf:type colibri:EnergyProducerResource."
				+ " 	?resource colibri:provides ?producer."
				+ " 	?producer rdf:type colibri:EnergyService. "
				+ " 	?producer colibri:hasData ?data. "
				+ " 	?data colibri:monitorsParameter ?param."
				+ " 	?param rdf:type colibri:EnergyParameter."
				+ " } ");

		// serialize individuals
		Config.getInstance().write(Config.getInstance().matlabSavePath, "\\individuals.txt", this.serialize());
	}

	private List<Individual> query(String query) throws InterfaceException
	{
		List<Individual> elements = new ArrayList<Individual>();

		Object[][] result = this.optimizer.getResult(query);

		for (int i = 0; i < result.length; i++)
		{
			IndividualImpl e = new IndividualImpl();
			e.setIndex(i);
			e.setUri(result[i][0].toString());
			elements.add(e);
		}

		return elements;
	}

	private String serialize()
	{
		String content = "";

		content += this.serialize("Zones", zones) + "\n\n";
		content += this.serialize("Environmental parameters (indoor)", indoorParameters) + "\n\n";
		content += this.serialize("Environmental parameters (outdoor)", outdoorParameters) + "\n\n";
		content += this.serialize("Data services (indoor)", indoorData) + "\n\n";
		content += this.serialize("Data services (outdoor)", outdoorData) + "\n\n";
		content += this.serialize("Data services (occupancy)", occupancyData) + "\n\n";
		content += this.serialize("Data services (grid)", gridData) + "\n\n";
		content += this.serialize("Control services (indoor)", indoorControl) + "\n\n";
		content += this.serialize("Energy types", energyTypes) + "\n\n";
		content += this.serialize("Energy suppliers", energySuppliers) + "\n\n";
		content += this.serialize("Energy consumers", energyConsumers) + "\n\n";
		content += this.serialize("Energy producers (local)", energyProducers) + "\n\n";
		content += this.serialize("Energy storages (local)", energyStorages) + "\n\n";

		return content;
	}

	private String serialize(String name, List<Individual> list)
	{
		String str = name + "\n";

		for (Individual i : list)
			str += "\n" + i.toString();

		return str;
	}
}
