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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.auto.colibri.optimization.Config;

/**
 * This class represents a control service individual of the ontology.
 */
public class ControlServiceImpl extends IndividualImpl implements Individual
{
	/**
	 * Default serial
	 */
	private static final long serialVersionUID = 1L;

	private StateImpl root = null;

	private List<VariationImpl> variations = null;

	private HashMap<Integer, StateImpl> mapIndex = null;
	private HashMap<Double, StateImpl> mapValue = null;

	public ControlServiceImpl()
	{
		this.variations = new ArrayList<VariationImpl>();
	}

	public List<VariationImpl> getVariations()
	{
		return this.variations;
	}

	public List<StateImpl> getStates()
	{
		List<StateImpl> states = new ArrayList<StateImpl>();

		StateImpl temp = root;
		int i = 0;

		while (temp != null)
		{
			temp.setIndex(i);
			states.add(temp);
			temp = temp.getHigher();
			i++;
		}

		return states;
	}

	public int[] convertToMatlab(double[] states) throws Exception
	{
		int[] ret = new int[states.length];

		// convert states
		for (int i = 0; i < states.length; i++)
		{
			// direct
			if (mapValue.containsKey(states[i]))
				ret[i] = mapValue.get(states[i]).getIndex();

			// indirect (best neighbor)
			else
			{
				float step = Config.getInstance().intervalStateSteps;
				int base = (int) Math.round(states[i] / step);
				ret[i] = mapValue.get((double) (base * step)).getIndex();
			}
		}

		return ret;
	}

	public void generateHash() throws Exception
	{
		mapValue = new HashMap<Double, StateImpl>();
		mapIndex = new HashMap<Integer, StateImpl>();
		for (StateImpl s : this.getStates())
		{
			if (mapValue.containsKey(s.getValue()))
				throw new Exception("State values in control service are not unique");
			mapValue.put(s.getValue(), s);

			if (mapIndex.containsKey(s.getIndex()))
				throw new Exception("State values in control service are not unique");
			mapIndex.put(s.getIndex(), s);
		}
	}

	public double[] convertToBas(int[] states)
	{
		double[] ret = new double[states.length];

		// convert states
		for (int i = 0; i < states.length; i++)
		{
			ret[i] = mapIndex.get(states[i]).getValue();
		}

		return ret;
	}

	@Override
	public String toString()
	{
		String ret = super.toString();

		for (VariationImpl s : this.getVariations())
		{
			s.setIndex(this.getVariations().indexOf(s));
			ret += "\n   " + s.toString();
		}

		ret += "\n";

		for (StateImpl s : this.getStates())
			ret += "\n   " + s.toString();

		return ret;
	}

	public void addVariation(int paramIndex, String uriTrend, String uriOrder)
	{
		this.variations.add(new VariationImpl(paramIndex, uriTrend, uriOrder));
	}

	public void addState(String uriState, Object min, Object max, String uriHigherState)
	{
		StateImpl state = null;

		StateImpl temp = root;
		while (temp != null)
		{
			if (temp.getUri().equals(uriState))
			{
				state = temp;
				break;
			}
			temp = temp.getHigher();
		}

		double minD = Double.parseDouble(min.toString());
		double maxD = Double.parseDouble(max.toString());

		float step = Config.getInstance().intervalStateSteps;

		if (minD > maxD)
		{
			double tmp = minD;
			minD = maxD;
			maxD = tmp;
		}

		StateImpl lower = null;
		StateImpl higher = null;

		if (state == null)
		{
			state = new StateImpl();
			state.setUri(uriState);
		}
		else
		{
			lower = state.getLower();
			higher = state.getHigher();
		}

		// insert new state
		if (lower == null && higher == null)
		{
			temp = root;
			StateImpl end = root;

			while (temp != null)
			{
				if (uriHigherState != null && temp.getUri().equals(uriHigherState))
					break;

				end = temp;
				temp = temp.getHigher();
			}

			if (temp != null)
			{
				if (temp.getLower() != null)
				{
					state.setLower(temp.getLower());
					temp.getLower().setHigher(state);
				}
				temp.setLower(state);
				state.setHigher(higher);

				if (root == temp)
				{
					root = state;
				}
			}
			else if (end != null)
			{
				end.setHigher(state);
				state.setLower(end);
			}
			else
			{
				root = state;
			}

			lower = state.getLower();
			higher = state.getHigher();
		}

		if (minD == maxD)
		{
			state.setValue(minD);
		}
		else
		{

			while (minD <= maxD)
			{
				StateImpl sInt = new StateImpl();
				sInt.setUri(uriState);
				sInt.setValue(minD);
				sInt.setLower(lower);
				sInt.setHigher(higher);

				if (lower != null)
					lower.setHigher(sInt);
				else
					root = sInt;
				lower = sInt;
				minD += step;
			}
		}
	}
}
