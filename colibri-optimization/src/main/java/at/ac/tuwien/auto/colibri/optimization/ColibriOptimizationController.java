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

package at.ac.tuwien.auto.colibri.optimization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWCharArray;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

import at.ac.tuwien.auto.colibri.messaging.exceptions.InterfaceException;
import at.ac.tuwien.auto.colibri.messaging.exceptions.ProcessingException;
import at.ac.tuwien.auto.colibri.messaging.types.Deregister;
import at.ac.tuwien.auto.colibri.messaging.types.Message;
import at.ac.tuwien.auto.colibri.messaging.types.Message.ContentType;
import at.ac.tuwien.auto.colibri.messaging.types.Query;
import at.ac.tuwien.auto.colibri.messaging.types.Status.Code;
import at.ac.tuwien.auto.colibri.optimization.connection.ClientSocketEndpoint;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.DeregisterImpl;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.QueryImpl;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.QueryResultImpl;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.RegisterImpl;
import at.ac.tuwien.auto.colibri.optimization.messaging.types.StatusImpl;
import at.ac.tuwien.auto.colibri.optimization.model.ControlServiceImpl;
import at.ac.tuwien.auto.colibri.optimization.model.Individual;
import at.ac.tuwien.auto.colibri.optimization.model.Ontology;
import at.ac.tuwien.auto.colibri.optimization.model.VariationImpl;
import matlaboptimizer.MatlabOptimizer;

/**
 * The optimization control logic is located in this class.
 */
public class ColibriOptimizationController
{
	/**
	 * Logging instance
	 */
	private static final Logger log = Logger.getLogger(ColibriOptimizationController.class.getName());

	/**
	 * Socket endpoint
	 */
	private ClientSocketEndpoint endpoint = null;

	/**
	 * Date formatter
	 */
	private SimpleDateFormat formatter = null;

	/**
	 * Ontology's individuals
	 */
	private Ontology ontology = null;

	/**
	 * Number of time delay states for models
	 */
	private int delays = 1;

	/**
	 * Number of future steps
	 */
	private int future = 1;

	/**
	 * Constructor
	 * 
	 * @param endpoint client socket endpoint
	 */
	public ColibriOptimizationController(ClientSocketEndpoint endpoint)
	{
		this.endpoint = endpoint;

		this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public boolean start()
	{
		// set start message
		log.info("Start optimization controller");

		try
		{
			// create registration
			RegisterImpl reg = new RegisterImpl();
			reg.setContentType(ContentType.RDF_XML);
			reg.setContent(this.load("/message_register.xml"));

			// send registration
			this.endpoint.send(reg);
			this.checkStatus();

			// get individuals
			File file = new File(Config.getInstance().matlabSavePath + "\\ontology.ser");

			if (file.exists())
			{
				FileInputStream fileIn = new FileInputStream(file);
				ObjectInputStream in = new ObjectInputStream(fileIn);

				this.ontology = (Ontology) in.readObject();
				this.ontology.setOptimizer(this);

				in.close();
				fileIn.close();
			}
			else
			{
				this.ontology = new Ontology();
				this.ontology.setOptimizer(this);
				this.ontology.query();

				FileOutputStream fileOut = new FileOutputStream(file);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(this.ontology);
				out.close();
				fileOut.close();
			}

			// get basic configuration
			int d = Math.round(Config.getInstance().pastTimesPercentage * Config.getInstance().optimizationHours * 60 / Config.getInstance().intervalMinutes);

			if (d > Config.getInstance().minDelays)
				this.delays = d;
			else
				this.delays = Config.getInstance().minDelays;

			this.future = Config.getInstance().optimizationHours * 60 / Config.getInstance().intervalMinutes;

			return true;
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());

			return false;
		}
	}

	public boolean stop()
	{
		// set stop message
		log.info("Stop optimization controller");

		try
		{
			// create deregistration
			Deregister dre = new DeregisterImpl();
			dre.setContent("http://www.colibri-samples.org/optimizer");

			// send deregistration
			this.endpoint.send(dre);
			this.checkStatus();

			return true;
		}
		catch (InterfaceException e)
		{
			log.severe(e.getMessage());

			return false;
		}
	}

	public void initTraining()
	{
		MatlabOptimizer m = null;
		MWStructArray s = null;
		MWCharArray p = null;

		try
		{
			// set start message
			log.info("Initialize training data started");

			// set parameters and run MATLAB
			s = this.getTraining();
			p = new MWCharArray(Config.getInstance().matlabSavePath);

			m = new MatlabOptimizer();
			m.inittraining(1, p, s);

			// set start message
			log.info("Initialize training data finished");
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		finally
		{
			MWCharArray.disposeArray(p);
			MWStructArray.disposeArray(s);

			if (m != null)
				m.dispose();
		}
	}

	public void initInput()
	{
		MatlabOptimizer m = null;
		MWStructArray s = null;
		MWCharArray p = null;

		try
		{
			// set start message
			log.info("Initialize input started");

			// set parameters and run MATLAB
			s = this.getInput();
			p = new MWCharArray(Config.getInstance().matlabSavePath);

			m = new MatlabOptimizer();
			m.initinput(1, p, s);

			// set start message
			log.info("Initialize input finished");
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		finally
		{
			MWCharArray.disposeArray(p);
			MWStructArray.disposeArray(s);

			if (m != null)
				m.dispose();
		}
	}

	public void initData()
	{
		MatlabOptimizer m = null;
		MWStructArray s = null;
		MWCharArray p = null;

		try
		{
			// set start message
			log.info("Initialize static data started");

			// set parameters and run MATLAB
			s = this.getData();
			p = new MWCharArray(Config.getInstance().matlabSavePath);

			m = new MatlabOptimizer();
			m.initdata(1, p, s);

			// set start message
			log.info("Initialize static data finished");
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		finally
		{
			MWCharArray.disposeArray(p);
			MWStructArray.disposeArray(s);

			if (m != null)
				m.dispose();
		}
	}

	public void initConstraints()
	{
		MatlabOptimizer m = null;
		MWStructArray s = null;
		MWCharArray p = null;

		try
		{
			// set start message
			log.info("Initialize constraints started");

			// set parameters and run MATLAB
			s = this.getConstraints();
			p = new MWCharArray(Config.getInstance().matlabSavePath);

			m = new MatlabOptimizer();
			m.initconstraints(1, p, s);

			// set start message
			log.info("Initialize constraints finished");
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		finally
		{
			MWCharArray.disposeArray(p);
			MWStructArray.disposeArray(s);

			if (m != null)
				m.dispose();
		}
	}

	public void model()
	{
		MatlabOptimizer m = null;
		MWCharArray p = null;

		try
		{
			// set start message
			log.info("Modeling started");

			// set parameters and run MATLAB
			p = new MWCharArray(Config.getInstance().matlabSavePath);

			m = new MatlabOptimizer();
			m.model(1, p);

			// set start message
			log.info("Modeling finished");
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		finally
		{
			MWCharArray.disposeArray(p);

			if (m != null)
				m.dispose();
		}
	}

	public void optimize()
	{
		MatlabOptimizer m = null;
		MWCharArray p = null;
		MWNumericArray schedule = null;
		Object[] result = null;

		try
		{
			// set start message
			log.info("Optimization run started");

			// set parameters and run MATLAB
			p = new MWCharArray(Config.getInstance().matlabSavePath);

			m = new MatlabOptimizer();
			result = m.optimize(1, p);

			schedule = (MWNumericArray) result[0];

			// create schedule result as CSV
			String content = "";

			// header
			content += "\"Time\";";
			for (int i = 0; i < this.ontology.getEnergySuppliers().size(); i++)
				content += "\"" + this.ontology.getEnergySuppliers().get(i).getUri() + "\";";

			for (int i = 0; i < this.ontology.getEnergyConsumers().size(); i++)
				content += "\"" + this.ontology.getEnergyConsumers().get(i).getUri() + "\";";
			content = content.substring(0, content.length() - 1) + "\n";

			// run through all time slots
			for (int j = 1; j <= schedule.getDimensions()[1]; j++)
			{
				content += "\"" + formatter.format(new Date(1000 * schedule.getLong(new int[] { 1, j }))) + "\";";

				// part of energy per supplier
				for (int i = 1; i <= this.ontology.getEnergySuppliers().size(); i++)
					content += "\"" + schedule.getDouble(new int[] { i + 1, j }) + "\";";

				// states of control services
				for (int i = 0; i < this.ontology.getIndoorControl().size(); i++)
				{
					ControlServiceImpl s = (ControlServiceImpl) this.ontology.getIndoorControl().get(i);
					double state = s.convertToBas(new int[] { schedule.getInt(new int[] { 2 + i + this.ontology.getEnergySuppliers().size(), j }) })[0];
					content += "\"" + Double.toString(state) + "\";";
				}

				// states of energy storage devices
				for (int i = this.ontology.getEnergySuppliers().size() + this.ontology.getIndoorControl().size() + 2; i <= schedule.getDimensions()[0]; i++)
				{
					content += "\"" + schedule.getInt(new int[] { i, j }) + "\";";
				}
				content = content.substring(0, content.length() - 1) + "\n";
			}

			// write CSV file
			Config.getInstance().write(Config.getInstance().matlabSavePath, "\\schedule.csv", content);

			// set start message
			log.info("Optimization run finished");
		}
		catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		finally
		{
			MWCharArray.disposeArray(p);
			MWArray.disposeArray(result);
			MWNumericArray.disposeArray(schedule);

			if (m != null)
				m.dispose();
		}
	}

	private MWStructArray getTraining() throws InterfaceException
	{
		// TODO Read training data from ontology and set values of the struct.
		// Currently, this task is done manually in MATLAB.

		// TODO Map states from BAS to integer states of MATLAB

		String[] variables = new String[] {
				"train_start",
				"train_occupancy",
				"train_influences",
				"train_states",
				"target_comfort",
				"target_occupancy",
				"target_demand",
				"target_production"
		};

		MWStructArray temp = new MWStructArray(1, 1, variables);

		temp.set("train_start", 1, formatter.format(new Date()));

		temp.set("train_occupancy", 1, new float[this.ontology.getOccupancyData().size()][this.delays + this.future]);
		temp.set("train_influences", 1, new float[this.ontology.getOutdoorData().size()][this.delays + this.future]);
		temp.set("train_states", 1, new float[this.ontology.getEnergyConsumers().size()][this.delays + this.future]);
		temp.set("target_comfort", 1, new float[this.ontology.getZones().size()][this.ontology.getIndoorParameters().size()][this.delays + this.future]);
		temp.set("target_occupancy", 1, new float[this.ontology.getOccupancyData().size()][this.delays + this.future]);
		temp.set("target_demand", 1, new float[this.ontology.getEnergyTypes().size()][this.delays + this.future]);
		temp.set("target_production", 1, new float[this.ontology.getEnergySuppliers().size()][this.delays + this.future]);

		return temp;
	}

	private MWStructArray getInput() throws InterfaceException
	{
		// TODO Read input data from ontology and set values of the struct.
		// Currently, this task is done manually in MATLAB.

		// TODO Map states from BAS to integer states of MATLAB

		String[] variables = new String[] {
				"current_start",
				"future_priority",
				"future_prices",
				"future_influences",
				"history_occupancy",
				"history_influences",
				"history_states",
				"initial_schedule",
				"targets_comfort",
				"targets_occupancy",
				"targets_demand",
				"targets_production"
		};

		MWStructArray temp = new MWStructArray(1, 1, variables);

		temp.set("current_start", 1, formatter.format(new Date()));

		temp.set("future_priority", 1, new float[this.ontology.getIndoorParameters().size()][this.future]);
		temp.set("future_prices", 1, new float[this.ontology.getEnergySuppliers().size()][this.future]);
		temp.set("future_influences", 1, new float[this.ontology.getOutdoorData().size()][this.future]);
		temp.set("history_occupancy", 1, new float[this.ontology.getOccupancyData().size()][this.delays]);
		temp.set("history_influences", 1, new float[this.ontology.getOutdoorData().size()][this.delays]);
		temp.set("history_states", 1, new float[this.ontology.getEnergyConsumers().size()][this.delays]);
		temp.set("initial_schedule", 1, new float[Config.getInstance().initialSchedules][this.future]);
		temp.set("targets_comfort", 1, new float[this.ontology.getZones().size()][this.ontology.getIndoorParameters().size()][this.delays]);
		temp.set("targets_occupancy", 1, new float[this.ontology.getZones().size()][this.delays]);
		temp.set("targets_demand", 1, new float[this.ontology.getEnergyTypes().size()][this.delays]);
		temp.set("targets_production", 1, new float[this.ontology.getEnergySuppliers().size()][this.delays]);

		return temp;
	}

	private MWStructArray getConstraints() throws InterfaceException
	{
		// without states for supplier units s
		int blocksize = this.ontology.getEnergySuppliers().size() * 2 + this.ontology.getEnergyConsumers().size();

		int[] lowerBounds = new int[this.future * blocksize];
		int[] upperBounds = new int[this.future * blocksize];

		// create bounds for each time step
		for (int t = 0; t < this.future; t++)
		{
			// create offset
			int offset = t * blocksize;

			// create bounds for demand parts of energy suppliers (0 - 100)
			for (int i = 0; i < this.ontology.getEnergySuppliers().size(); i++)
			{
				lowerBounds[i + offset] = 0;
				upperBounds[i + offset] = 100;
			}

			// update offset
			offset += this.ontology.getEnergySuppliers().size();

			// create bounds for control services
			for (int i = 0; i < this.ontology.getIndoorControl().size(); i++)
			{
				ControlServiceImpl c = (ControlServiceImpl) this.ontology.getIndoorControl().get(i);

				int high = 0;
				if (c.getStates().size() > 0)
					high = c.getStates().size() - 1;

				lowerBounds[i + offset] = 0;
				upperBounds[i + offset] = high;
			}

			// create bounds for demand of energy storage resources (loading of storage)
			for (int i = this.ontology.getIndoorControl().size(); i < this.ontology.getEnergyConsumers().size(); i++)
			{
				lowerBounds[i + offset] = 0;
				upperBounds[i + offset] = 1;
			}

			// update offset
			offset += this.ontology.getEnergyConsumers().size();

			// create bounds for activated supply of suppliers
			for (int i = 0; i < this.ontology.getEnergySuppliers().size(); i++)
			{
				lowerBounds[i + offset] = 0;
				upperBounds[i + offset] = 1;
			}
		}

		// define integer variables
		int[] integerVars = new int[this.future * blocksize];

		for (int i = 0; i < integerVars.length; i++)
			integerVars[i] = i + 1;

		// create linear inequality constraints

		// create linear equality constraints
		List<double[]> linearAeq = new ArrayList<double[]>();
		List<Double> linearBeq = new ArrayList<Double>();

		List<double[]> linearA = new ArrayList<double[]>();
		List<Double> linearB = new ArrayList<Double>();

		// 1) sum over demand ratios must be 100 for all energy types g

		// load suppliers per energy type
		Object[][] listSupplierType = this.getResult("SELECT ?supply ?type "
				+ " WHERE "
				+ " { "
				+ " 	?supply rdf:type colibri:EnergyService. "
				+ " 	?supply colibri:hasEnergyType ?type. "
				+ " } ");
		int[][] arrayTypeSupply = this.getMatrix(listSupplierType, this.ontology.getEnergyTypes(), this.ontology.getEnergySuppliers(), 1, 0);

		for (int t = 0; t < this.future; t++)
		{
			// create offset
			int offset = t * blocksize;

			for (int g = 0; g < this.ontology.getEnergyTypes().size(); g++)
			{
				double[] A = new double[this.future * blocksize];
				double b = 100;
				boolean anySupply = false;

				for (int i = 0; i < this.ontology.getEnergySuppliers().size(); i++)
				{
					if (arrayTypeSupply[g][i] == 1)
					{
						anySupply = true;
						A[i + offset] = 1;
					}
				}

				if (anySupply)
				{
					linearAeq.add(A);
					linearBeq.add(b);
				}
			}
		}

		// 2) flow conservation in storage devices, output <= input

		// TODO load charging rate, discharging rate, and loss of power per storage device
		double a = 2.0;
		double u = 2.0;
		double w = 0.01;

		double[] A = new double[this.future * blocksize];
		double b = -w;

		for (int t = 0; t < this.future; t++)
		{
			// create offset
			int offset = t * blocksize;

			for (int y = 0; y < this.ontology.getEnergySuppliers().size(); y++)
			{
				for (int storage = 0; storage < this.ontology.getEnergyStorages().size(); storage++)
				{
					if (this.ontology.getEnergyStorages().get(storage).getUri().equals(this.ontology.getEnergySuppliers().get(y).getUri()))
					{
						double[] temp = new double[this.future * blocksize];

						System.arraycopy(A, 0, temp, 0, A.length);

						A = temp;

						// Ax <= b (=> multiply by -1)
						A[offset + this.ontology.getEnergySuppliers().size() + this.ontology.getIndoorControl().size() + storage] = -a;
						A[offset + this.ontology.getEnergySuppliers().size() + this.ontology.getEnergyConsumers().size() + y] = u;

						linearA.add(A);
						linearB.add(b);
					}
				}
			}
		}

		// 3) load and supply not at the same time (maybe obsolete)

		for (int t = 0; t < this.future; t++)
		{
			// create offset
			int offset = t * blocksize;

			for (int y = 0; y < this.ontology.getEnergySuppliers().size(); y++)
			{
				for (int storage = 0; storage < this.ontology.getEnergyStorages().size(); storage++)
				{
					if (this.ontology.getEnergyStorages().get(storage).getUri().equals(this.ontology.getEnergySuppliers().get(y).getUri()))
					{
						double[] temp = new double[this.future * blocksize];

						// Ax <= b
						temp[offset + this.ontology.getEnergySuppliers().size() + this.ontology.getIndoorControl().size() + storage] = 1;
						temp[offset + this.ontology.getEnergySuppliers().size() + this.ontology.getEnergyConsumers().size() + y] = 1;

						linearA.add(temp);
						linearB.add(1.0);
					}
				}
			}
		}

		// 4) get comfort threshold conditions

		double[][] nonlinear_comfort_b = new double[this.ontology.getZones().size()][this.ontology.getIndoorParameters().size()];
		int[][] nonlinear_comfort_op = new int[this.ontology.getZones().size()][this.ontology.getIndoorParameters().size()];

		Object[][] conditions = this.getResult("SELECT ?zone ?type ?symbolData ?symbolConstant ?formula ?value ?unit "
				+ "WHERE {"
				+ "?condition rdf:type colibri:Condition."
				+ "?condition colibri:hasVariable ?var1."
				+ "?condition colibri:hasVariable ?var2."
				+ "?var1 colibri:hasData ?data."
				+ "?var1 colibri:symbol ?symbolData."
				+ "?data colibri:covers ?zone."
				+ "?data colibri:hasDataConfiguration ?config."
				+ "?config colibri:hasParameter ?param."
				+ "?param rdf:type ?type."
				+ "?type rdfs:subClassOf colibri:EnvironmentalParameter."
				+ "?condition colibri:formula ?formula."
				+ "?var2 colibri:hasConstant ?constant."
				+ "?var2 colibri:symbol ?symbolConstant."
				+ "?constant rdf:type colibri:Constant."
				+ "?constant colibri:value ?value."
				+ "?constant colibri:hasUnit ?unit."
				+ "}");

		for (int i = 0; i < this.ontology.getZones().size(); i++)
		{
			Individual zone = this.ontology.getZones().get(i);

			for (int j = 0; j < this.ontology.getIndoorParameters().size(); j++)
			{
				Individual parameter = this.ontology.getIndoorParameters().get(j);

				for (Object[] row : conditions)
				{
					if (zone.getUri().equals(row[0]) && parameter.getUri().equals(row[1]))
					{
						// get formula and symbols
						String formula = row[4].toString();
						String symbolData = row[2].toString();
						String symbolConstant = row[3].toString();

						// invert formula
						if (formula.indexOf(symbolData) > formula.indexOf(symbolConstant))
						{
							if (formula.contains("<"))
								formula = formula.replace('<', '>');
							else if (formula.contains("<"))
								formula = formula.replace('>', '<');
						}

						// set operation
						if (formula.contains("<="))
							nonlinear_comfort_op[i][j] = 1;
						else if (formula.contains("=="))
							nonlinear_comfort_op[i][j] = 2;
						else if (formula.contains(">="))
							nonlinear_comfort_op[i][j] = 3;

						// set value
						nonlinear_comfort_b[i][j] = Double.parseDouble(row[5].toString());

						// TODO convert value to unit of data service's parameter
					}
				}
			}
		}

		// create resulting MATLAB structure
		String[] variables = new String[] {
				"number_vars",
				"integer_vars",
				"lower_bounds",
				"upper_bounds",
				"linear_A",
				"linear_b",
				"linear_Aeq",
				"linear_beq",
				"nonlinear_production_b",
				"nonlinear_comfort_op",
				"nonlinear_comfort_b"
		};

		MWStructArray temp = new MWStructArray(1, 1, variables);

		temp.set("number_vars", 1, this.future * blocksize);
		temp.set("lower_bounds", 1, lowerBounds);
		temp.set("upper_bounds", 1, upperBounds);
		temp.set("integer_vars", 1, integerVars);

		if (linearAeq.size() == 0)
			temp.set("linear_Aeq", 1, new double[][] {});
		else
			temp.set("linear_Aeq", 1, linearAeq.toArray());

		if (linearBeq.size() == 0)
			temp.set("linear_beq", 1, new double[] {});
		else
			temp.set("linear_beq", 1, linearBeq.toArray());

		if (linearA.size() == 0)
			temp.set("linear_A", 1, new double[][] {});
		else
			temp.set("linear_A", 1, linearA.toArray());

		if (linearB.size() == 0)
			temp.set("linear_b", 1, new double[] {});
		else
			temp.set("linear_b", 1, linearB.toArray());

		temp.set("nonlinear_comfort_op", 1, nonlinear_comfort_op);
		temp.set("nonlinear_comfort_b", 1, nonlinear_comfort_b);
		temp.set("nonlinear_production_b", 1, new double[] {});

		return temp;
	}

	private MWStructArray getData() throws Exception
	{
		// which control service covers which parameter types and zones
		Object[][] listZoneTypeControl = this.getResult("SELECT ?zone ?type ?control "
				+ " WHERE "
				+ " { "
				+ " 	?zone rdf:type colibri:Zone. "
				+ " 	?control colibri:covers ?zone. "
				+ " 	?control colibri:controlsParameter ?param. "
				+ " 	?param rdf:type ?type. "
				+ " 	?type rdfs:subClassOf colibri:EnvironmentalParameter. "
				+ " } ");

		// which data service covers which parameter types and zones
		Object[][] listZoneTypeData = this.getResult("SELECT ?zone ?type ?data "
				+ " WHERE "
				+ " { "
				+ " 	?zone rdf:type colibri:Zone. "
				+ " 	?data colibri:covers ?zone. "
				+ "   	?data rdf:type colibri:BuildingData. "
				+ " 	?data colibri:monitorsParameter ?param. "
				+ " 	?param rdf:type ?type. "
				+ " 	?type rdfs:subClassOf colibri:EnvironmentalParameter. "
				+ " } ");

		// which supplier provides which energy types
		Object[][] listSupplierType = this.getResult("SELECT ?supply ?type "
				+ " WHERE "
				+ " { "
				+ " 	?supply rdf:type colibri:EnergyService. "
				+ " 	?supply colibri:hasEnergyType ?type. "
				+ " } ");

		// which consumer (control service, storage) consumes which energy types
		Object[][] listConsumerType = this.getResult("SELECT ?consumer ?type "
				+ " WHERE {"
				+ "	 { "
				+ " 	?consumer rdf:type colibri:ControlService. "
				+ " 	?consumer colibri:hasEnergyType ?type. "
				+ "	 } "
				+ "  UNION "
				+ "  { "
				+ " 	?consumer rdf:type colibri:EnergyStorageResource. "
				+ " 	?consumer colibri:provides ?es."
				+ " 	?es rdf:type colibri:EnergyService."
				+ " 	?es colibri:hasEnergyType ?type. "
				+ "	 }"
				+ " }");

		// which indoor data depends on which outdoor data
		Object[][] listBuildingDataOutdoor = this.getResult("SELECT distinct ?s_indoor ?s_outdoor "
				+ " WHERE "
				+ " { "
				+ " 	?s_indoor rdf:type colibri:BuildingData. "
				+ " 	?s_outdoor rdf:type colibri:WebData. "
				+ "     ?s_indoor colibri:dependsOn ?s_outdoor. "
				+ " } ");

		// which energy demand depends on which outdoor data
		Object[][] listTypeOutdoor = this.getResult("SELECT distinct ?energytype ?outdoor "
				+ " WHERE "
				+ " { "
				+ " 	?supply rdf:type colibri:EnergyService. "
				+ " 	?supply colibri:hasEnergyType ?energytype. "
				+ " 	?supply colibri:dependsOn ?outdoor."
				+ " 	?outdoor rdf:type colibri:WebData. "
				+ " 	?outdoor colibri:monitorsParameter ?p."
				+ " 	?p rdf:type ?t."
				+ " 	?t rdfs:subClassOf+ colibri:EnvironmentalParameter."
				+ " } ");

		// which energy service of a producer depends on which outdoor data
		Object[][] listProducerOutdoor = this.getResult("SELECT distinct ?producer ?outdoor "
				+ " WHERE "
				+ " { "
				+ " 	?resource rdf:type colibri:EnergyProducerResource."
				+ " 	?resource colibri:provides ?producer."
				+ " 	?producer rdf:type colibri:EnergyService. "
				+ " 	?producer colibri:hasData ?data. "
				+ " 	?data colibri:monitorsParameter ?param."
				+ " 	?param rdf:type colibri:EnergyParameter."
				+ "		?data colibri:dependsOn ?outdoor. "
				+ " 	?outdoor rdf:type colibri:WebData. "
				+ " 	?outdoor colibri:monitorsParameter ?paramOutdoor."
				+ " 	?paramOutdoor rdf:type ?type."
				+ " 	?type rdfs:subClassOf+ colibri:EnvironmentalParameter."
				+ " } ");

		// which zone has an occupancy measurement
		Object[][] listOccupiedZones = this.getResult("SELECT ?zone ?occupancy "
				+ " WHERE "
				+ " { "
				+ " 	?zone rdf:type colibri:Zone. "
				+ " 	?occupancy colibri:covers ?zone. "
				+ "   	?occupancy rdf:type colibri:BuildingData. "
				+ " 	?occupancy colibri:monitorsParameter ?param. "
				+ " 	?param rdf:type colibri:OccupancyParameter. "
				+ " } ");

		// create integer arrays
		int[][][] arrayZoneTypeControl = this.getMatrix(listZoneTypeControl, this.ontology.getZones(), this.ontology.getIndoorParameters(), this.ontology.getIndoorControl(), 0, 1, 2);
		int[][][] arrayZoneTypeData = this.getMatrix(listZoneTypeData, this.ontology.getZones(), this.ontology.getIndoorParameters(), this.ontology.getIndoorData(), 0, 1, 2);
		int[][] arrayTypeSupply = this.getMatrix(listSupplierType, this.ontology.getEnergyTypes(), this.ontology.getEnergySuppliers(), 1, 0);
		int[][] arrayTypeConsumer = this.getMatrix(listConsumerType, this.ontology.getEnergyTypes(), this.ontology.getEnergyConsumers(), 1, 0);
		int[][] arrayIndoorOutdoor = this.getMatrix(listBuildingDataOutdoor, this.ontology.getIndoorData(), this.ontology.getOutdoorData(), 0, 1);
		int[][] arraySupplyOutdoor = this.getMatrix(listProducerOutdoor, this.ontology.getEnergySuppliers(), this.ontology.getOutdoorData(), 0, 1);
		int[][] arrayTypeOutdoor = this.getMatrix(listTypeOutdoor, this.ontology.getEnergyTypes(), this.ontology.getOutdoorData(), 0, 1);
		int[][] arrayOccupancyOutdoor = this.getMatrix(listBuildingDataOutdoor, this.ontology.getOccupancyData(), this.ontology.getOutdoorData(), 0, 1);
		int[][] arrayMonitorIndicator = new int[this.ontology.getZones().size()][this.ontology.getIndoorParameters().size()];
		int[] arraySupplyProduction = new int[this.ontology.getEnergySuppliers().size()];
		int[] arraySupplyStorage = new int[this.ontology.getEnergySuppliers().size()];
		int[][] arrayZoneOccupancy = this.getMatrix(listOccupiedZones, this.ontology.getZones(), this.ontology.getOccupancyData(), 0, 1);
		
		for (int i = 0; i < this.ontology.getEnergySuppliers().size(); i++)
		{
			Individual ii = this.ontology.getEnergySuppliers().get(i);
			arraySupplyStorage[i] = 0;

			for (int j = 0; j < this.ontology.getEnergyStorages().size(); j++)
			{
				Individual ij = this.ontology.getEnergyStorages().get(j);

				if (ii.getUri().equals(ij.getUri()))
				{
					arraySupplyStorage[i] = 1;
					break;
				}
			}
		}

		for (int i = 0; i < this.ontology.getEnergySuppliers().size(); i++)
		{
			Individual ii = this.ontology.getEnergySuppliers().get(i);
			arraySupplyProduction[i] = 0;

			for (int j = 0; j < this.ontology.getEnergyProducers().size(); j++)
			{
				Individual ij = this.ontology.getEnergyProducers().get(j);

				if (ii.getUri().equals(ij.getUri()))
				{
					arraySupplyProduction[i] = 1;
					break;
				}
			}
		}

		for (int i = 0; i < this.ontology.getZones().size(); i++)
		{
			for (int j = 0; j < this.ontology.getIndoorParameters().size(); j++)
			{
				boolean monitor = false;
				boolean control = false;

				for (int z = 0; z < arrayZoneTypeControl[i][j].length; z++)
				{
					if (arrayZoneTypeControl[i][j][z] == 1)
					{
						control = true;
						break;
					}
				}

				for (int z = 0; z < arrayZoneTypeData[i][j].length; z++)
				{
					if (arrayZoneTypeData[i][j][z] == 1)
					{
						monitor = true;
						break;
					}
				}

				arrayMonitorIndicator[i][j] = 0;
				if (monitor && control)
					arrayMonitorIndicator[i][j] = 1;
			}
		}

		
		List<Integer[][]> arrayControlParameterVariation = new ArrayList<Integer[][]>();
		int maxLength = 0;
		
		for (int i = 0; i < this.ontology.getIndoorControl().size(); i++)
		{
			List<Integer[]> controls = new ArrayList<Integer[]>();			
			ControlServiceImpl c = (ControlServiceImpl) this.ontology.getIndoorControl().get(i);
			
			for(int j = 0; j < this.ontology.getIndoorParameters().size(); j++)
			{
				List<Integer> parameters = new ArrayList<Integer>();
				
				for (VariationImpl v : c.getVariations())
				{
					if (v.getParamIndex() == j)
						parameters.add(v.getIndicator());
				}
				
				Integer[] parametersArr = new Integer[parameters.size()];				
				controls.add(parameters.toArray(parametersArr));
				
				if (parametersArr.length > maxLength)
					maxLength = parametersArr.length;
			}
			Integer[][] controlsArr = new Integer[controls.size()][maxLength];
			arrayControlParameterVariation.add(controls.toArray(controlsArr));
		}

		// run static constraints

		// each supply unit can provide only one energy type
		for (int s = 0; s < this.ontology.getEnergySuppliers().size(); s++)
		{
			int count = 0;
			for (int t = 0; t < this.ontology.getEnergyTypes().size(); t++)
			{
				if (arrayTypeSupply[t][s] == 1)
					count++;
			}

			if (count > 1)
				throw new Exception("Supplier can only provide one type of energy");
		}

		// create MATLAB structure
		String[] variables = new String[] {
				"history_steps",
				"future_steps",
				"step_interval",
				"objective_weight",
				"parameters",
				"zones",
				"grids",
				"suppliers",
				"consumers",
				"outdoor",
				"monitor_indicator",
				"supply_indicator",
				"supply_production",
				"supply_storage",
				"zone_occupancy",
				"grid_consumer",
				"zone_parameter_control",
				"zone_parameter_monitor",
				"indoor_outdoor",
				"grid_outdoor",
				"supply_outdoor",
				"occupancy_outdoor",
				"input_time",
				"input_occupancy",
				"input_influences",
				"input_states",
				"input_end",
				"control_parameter_variation",
				"hidden_size",
				"train_iterations",
				"train_hiddensize",
				"train_performancefct",
				"train_features",
				"train_magnitude",
				"train_division",
				"train_algorithm",
				"train_division_default",
				"train_noimprove_default",
				"train_type",
				"population_size",
				"global_optimization",
				"tournament_size",
				"selection_algorithm",
				"validity_count"
		};

		MWStructArray temp = new MWStructArray(1, 1, variables);

		temp.set("history_steps", 1, delays);
		temp.set("future_steps", 1, future);
		temp.set("step_interval", 1, Config.getInstance().intervalMinutes);

		temp.set("objective_weight", 1, 0.7);

		temp.set("parameters", 1, this.ontology.getIndoorParameters().size());
		temp.set("zones", 1, this.ontology.getZones().size());
		temp.set("grids", 1, this.ontology.getEnergyTypes().size());
		temp.set("suppliers", 1, this.ontology.getEnergySuppliers().size());
		temp.set("consumers", 1, this.ontology.getEnergyConsumers().size());
		temp.set("outdoor", 1, this.ontology.getOutdoorParameters().size());

		temp.set("monitor_indicator", 1, arrayMonitorIndicator);
		temp.set("supply_indicator", 1, arrayTypeSupply);

		temp.set("supply_production", 1, arraySupplyProduction);
		temp.set("supply_storage", 1, arraySupplyStorage);
		temp.set("zone_occupancy", 1, arrayZoneOccupancy);
		temp.set("grid_consumer", 1, arrayTypeConsumer);

		temp.set("zone_parameter_control", 1, arrayZoneTypeControl);
		temp.set("zone_parameter_monitor", 1, arrayZoneTypeData);

		temp.set("indoor_outdoor", 1, arrayIndoorOutdoor);
		temp.set("grid_outdoor", 1, arrayTypeOutdoor);
		temp.set("supply_outdoor", 1, arraySupplyOutdoor);
		temp.set("occupancy_outdoor", 1, arrayOccupancyOutdoor);

		temp.set("control_parameter_variation", 1, arrayControlParameterVariation.toArray());
		temp.set("hidden_size", 1, Config.getInstance().initialHiddenSize);
		temp.set("train_iterations", 1, Config.getInstance().trainingIterations);
		temp.set("train_hiddensize", 1, Config.getInstance().trainHiddenSize);
		temp.set("train_performancefct", 1, Config.getInstance().trainPerformanceFct);
		temp.set("train_features", 1, Config.getInstance().trainFeatures);
		temp.set("train_magnitude", 1, Config.getInstance().trainMagnitude);
		temp.set("train_division", 1, Config.getInstance().trainDivision);
		temp.set("train_algorithm", 1, Config.getInstance().trainAlgorithm);
		temp.set("train_division_default", 1, Config.getInstance().trainDefaultDivision);
		temp.set("train_noimprove_default", 1, Config.getInstance().trainNoImproveAccepted);
		temp.set("train_type", 1, Config.getInstance().trainType);
		temp.set("population_size", 1, Config.getInstance().populationSize);
		temp.set("global_optimization", 1, Config.getInstance().globalOptimization);
		temp.set("tournament_size", 1, Config.getInstance().tournamentSize);
		temp.set("selection_algorithm", 1, Config.getInstance().selectionAlgorithm);
		temp.set("validity_count", 1, Config.getInstance().maxValidityCount);

		int i = 1;
		temp.set("input_time", 1, i);

		i += Config.getInstance().matlabTimeInputs;
		temp.set("input_occupancy", 1, i);

		i += this.ontology.getOccupancyData().size();
		temp.set("input_influences", 1, i);

		i += this.ontology.getOutdoorData().size();
		temp.set("input_states", 1, i);

		i += this.ontology.getEnergyConsumers().size() - 1;
		temp.set("input_end", 1, i);

		return temp;
	}

	public Object[][] getResult(String query) throws InterfaceException
	{
		Query q = new QueryImpl();
		q.setContent(query);
		this.endpoint.send(q);
		this.await();

		Message msg = this.endpoint.poll();

		if (msg instanceof QueryResultImpl)
		{
			QueryResultImpl message = (QueryResultImpl) msg;
			return message.getResults();
		}
		else if (msg instanceof StatusImpl)
		{
			StatusImpl message = (StatusImpl) msg;

			if (message.getCode() != Code.OK)
				throw new ProcessingException(message.getText(), message);
		}
		throw new ProcessingException("This message is not supported in this function", msg);
	}

	private int[][][] getMatrix(Object[][] r, List<Individual> listX, List<Individual> listY, List<Individual> listZ, int indexX, int indexY, int indexZ)
	{
		int[][][] matrix = new int[listX.size()][listY.size()][listZ.size()];

		for (Individual x : listX)
		{
			matrix[x.getIndex()] = getMatrix(r, listY, listZ, indexY, indexZ, x.getUri(), indexX);
		}

		return matrix;
	}

	private int[][] getMatrix(Object[][] r, List<Individual> listX, List<Individual> listY, int indexX, int indexY)
	{
		return this.getMatrix(r, listX, listY, indexX, indexY, null, 0);
	}

	private int[][] getMatrix(Object[][] r, List<Individual> listX, List<Individual> listY, int indexX, int indexY, String a, int indexA)
	{
		int[][] matrix = new int[listX.size()][listY.size()];

		for (Individual x : listX)
		{
			for (Individual y : listY)
			{
				matrix[x.getIndex()][y.getIndex()] = 0;

				for (int i = 0; i < r.length; i++)
				{
					if (x.getUri().equals(r[i][indexX].toString()))
					{
						if (y.getUri().equals(r[i][indexY].toString()))
						{
							if (a == null || a.equals(r[i][indexA].toString()))
							{
								matrix[x.getIndex()][y.getIndex()] = 1;
								break;
							}
						}
					}
				}
			}
		}
		return matrix;
	}

	// private int[] getMatrix(Object[][] r, List<Individual> listX, int indexX)
	// {
	// int[] matrix = new int[listX.size()];
	//
	// for (Individual x : listX)
	// {
	// matrix[x.getIndex()] = 0;
	//
	// for (int i = 0; i < r.length; i++)
	// {
	// if (x.getUri().equals(r[i][indexX].toString()))
	// {
	// matrix[x.getIndex()] = 1;
	// break;
	// }
	// }
	// }
	// return matrix;
	// }

	private void checkStatus() throws InterfaceException
	{
		this.await();

		Message msg = this.endpoint.poll();

		if (msg instanceof StatusImpl)
		{
			StatusImpl s = (StatusImpl) msg;
			if (s.getCode() != Code.OK)
				throw new ProcessingException(s.getContent(), s);
		}
		else
		{
			throw new ProcessingException("Status message was excepted", msg);
		}
	}

	private String load(String resource) throws IOException
	{
		URL url = this.getClass().getResource(resource);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

		StringBuilder out = new StringBuilder();
		String line;

		while ((line = br.readLine()) != null)
			out.append(line);

		String content = out.toString();
		br.close();

		return content;
	}

	private void await()
	{
		try
		{
			while (this.endpoint.isEmpty())
			{
				// notify parent that executing is waiting
				synchronized (this)
				{
					this.notify();
				}

				// wait for new messages
				synchronized (this.endpoint)
				{
					this.endpoint.wait();
				}
			}
		}
		catch (InterruptedException e)
		{
			log.severe(e.getMessage());
		}
	}
}
