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

public class VariationImpl extends IndividualImpl implements Individual
{
	/**
	 * Default serial
	 */
	private static final long serialVersionUID = 1L;

	public enum Trend
	{
		Up, Down
	}

	public enum Order
	{
		Higher, Lower
	}

	private Trend trend;
	private Order order;
	private int paramIndex;

	public VariationImpl(int paramIndex, String uriTrend, String uriOrder)
	{
		// TODO Include also conditions for control variations (future work)

		this.paramIndex = paramIndex;

		if (uriOrder.endsWith("HigherValue"))
			order = Order.Higher;
		else if (uriOrder.endsWith("LowerValue"))
			order = Order.Lower;

		if (uriTrend.endsWith("UpTrend"))
			trend = Trend.Up;
		else if (uriTrend.endsWith("DownTrend"))
			trend = Trend.Down;
	}

	public Trend getTrend()
	{
		return trend;
	}

	public Order getOrder()
	{
		return order;
	}

	public int getIndicator()
	{
		if (this.getOrder().equals(Order.Higher) && this.getTrend().equals(Trend.Up))
		{
			return 1;
		}
		if (this.getOrder().equals(Order.Higher) && this.getTrend().equals(Trend.Down))
		{
			return 2;
		}
		if (this.getOrder().equals(Order.Lower) && this.getTrend().equals(Trend.Up))
		{
			return 3;
		}
		if (this.getOrder().equals(Order.Lower) && this.getTrend().equals(Trend.Down))
		{
			return 4;
		}
		return -1;
	}

	public int getParamIndex()
	{
		return paramIndex;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + this.trend.toString() + ", " + this.order.toString() + ", " + this.paramIndex + ")";
	}
}
