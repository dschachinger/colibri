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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import at.ac.tuwien.auto.colibri.commons.Configuration;

/**
 * Configuration file for Colibri optimization
 */
public class Config extends Configuration
{
	/**
	 * Singleton member variable
	 */
	private static Config configuration = null;

	/**
	 * Singleton method
	 * 
	 * @return instance of configuration
	 */
	public static Config getInstance()
	{
		if (configuration == null)
		{
			configuration = new Config();
		}
		return configuration;
	}

	/**
	 * WebSocket endpoint
	 */
	public String endpoint = "";

	/**
	 * Optimization horizon in hours
	 */
	public int optimizationHours = 0;

	/**
	 * Optimization step size in minutes
	 */
	public int intervalMinutes = 0;

	/**
	 * Percentage of optimization horizon used for historic values
	 */
	public float pastTimesPercentage = 0;

	/**
	 * Path for saving and loading files in MATLAB
	 */
	public String matlabSavePath = "";

	/**
	 * Number of time-dependent features
	 */
	public int matlabTimeInputs = 0;

	/**
	 * Step width for interval states
	 */
	public float intervalStateSteps = 0;

	/**
	 * Initial size of hidden layer
	 */
	public int initialHiddenSize = 0;

	/**
	 * Iterations for training neural networks
	 */
	public int trainingIterations = 0;

	/**
	 * Train hidden size
	 */
	public int trainHiddenSize = 0;

	/**
	 * Train performance function
	 */
	public int trainPerformanceFct = 0;

	/**
	 * Train feature selection
	 */
	public int trainFeatures = 0;

	/**
	 * Train magnitude of values
	 */
	public int trainMagnitude = 0;

	/**
	 * Train division of input vectors
	 */
	public int trainDivision = 0;
	
	/**
	 * Used trainings algorithm
	 */
	public String trainAlgorithm = "";

	/**
	 * Training division
	 */
	public float trainDefaultDivision = 0;
	
	/**
	 * Number of no improvements that are accepted
	 */
	public int trainNoImproveAccepted = 0;

	/**
	 * Train network type
	 */
	public int trainType = 0;
	
	/**
	 * Minimum delay elements
	 */
	public int minDelays = 0;
	
	/**
	 * GA population size
	 */
	public int populationSize = 0;
	
	/**
	 * Number of past schedules for initialization of population
	 */
	public int initialSchedules = 0;
	
	/**
	 * Activation of global optimization
	 */
	public boolean globalOptimization = false;
	
	/**
	 * Parameter for tournament selection
	 */
	public int tournamentSize = 0;
	
	/**
	 * Used selection algorithm (1=roulette, 2=tournament)
	 */
	public int selectionAlgorithm = 0;
	
	/**
	 * Number of retries for checking the validity of a solution
	 */
	public int maxValidityCount = 0;
	
	@Override
	public String getResource()
	{
		return "/optimization.properties";
	}

	/**
	 * Private constructor
	 */
	protected Config()
	{
		this.load();
	}

	/**
	 * Writes content to a file in the given directory.
	 * 
	 * @param directory Directory
	 * @param file Filename
	 * @param content File content
	 */
	public void write(String directory, String file, String content) throws IOException
	{
		File dir = new File(directory);

		dir.mkdirs();

		File f = new File(directory + file);
		f.createNewFile();

		FileWriter fw = new FileWriter(f);
		fw.write(content);
		fw.close();
	}
}
