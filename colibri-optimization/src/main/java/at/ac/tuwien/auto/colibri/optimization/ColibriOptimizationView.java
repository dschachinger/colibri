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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Graphical user interface for Colibri optimization.
 */
public class ColibriOptimizationView extends JFrame implements ActionListener
{
	/**
	 * Latch indicates if frame is closed
	 */
	private final CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Optimization logic
	 */
	private ColibriOptimizationController optimizer;

	private JButton initData;
	private JButton initTraining;
	private JButton initInput;
	private JButton initConstraints;
	private JButton runModel;
	private JButton runOptimize;
	private JButton start;
	private JButton stop;

	/**
	 * default serial number
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param optimizer optimizer controller
	 */
	public ColibriOptimizationView(ColibriOptimizationController optimizer)
	{
		this.optimizer = optimizer;

		this.createContents();
	}

	/**
	 * Initializes GUI
	 */
	private void createContents()
	{
		this.setTitle("Colibri optimizer");

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		BorderLayout layoutMain = new BorderLayout();
		this.setLayout(layoutMain);

		JPanel panelInitialize = new JPanel();
		JPanel panelRun = new JPanel();
		JPanel panelManage = new JPanel();

		this.add(panelManage, BorderLayout.PAGE_START);
		this.add(panelInitialize, BorderLayout.LINE_START);
		this.add(panelRun, BorderLayout.PAGE_END);

		panelInitialize.setLayout(new FlowLayout());
		panelRun.setLayout(new FlowLayout());
		panelManage.setLayout(new FlowLayout());

		this.initData = new JButton("Data");
		this.initData.addActionListener(this);
		panelInitialize.add(this.initData);

		this.initTraining = new JButton("Training");
		this.initTraining.addActionListener(this);
		panelInitialize.add(this.initTraining);

		this.initInput = new JButton("Input");
		this.initInput.addActionListener(this);
		panelInitialize.add(this.initInput);

		this.initConstraints = new JButton("Constraints");
		this.initConstraints.addActionListener(this);
		panelInitialize.add(this.initConstraints);

		this.runModel = new JButton("Model");
		this.runModel.addActionListener(this);
		panelRun.add(runModel);

		this.runOptimize = new JButton("Optimize");
		this.runOptimize.addActionListener(this);
		panelRun.add(runOptimize);

		this.start = new JButton("Start");
		this.start.addActionListener(this);
		panelManage.add(start);

		this.stop = new JButton("Stop");
		this.stop.addActionListener(this);
		panelManage.add(stop);

		this.changeEnable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			if (e.getSource() == this.initData)
			{
				optimizer.initData();
			}
			else if (e.getSource() == this.initTraining)
			{
				JOptionPane.showMessageDialog(this, "This function is currently disabled. Use MATLAB construct instead.");				
				//optimizer.initTraining();
			}
			else if (e.getSource() == this.initInput)
			{
				JOptionPane.showMessageDialog(this, "This function is currently disabled. Use MATLAB construct instead.");
				//optimizer.initInput();
			}
			else if (e.getSource() == this.initConstraints)
			{
				optimizer.initConstraints();
			}
			else if (e.getSource() == this.runModel)
			{
				optimizer.model();
			}
			else if (e.getSource() == this.runOptimize)
			{
				optimizer.optimize();
			}
			else if (e.getSource() == this.start)
			{
				if (optimizer.start())
					this.changeEnable(true);

			}
			else if (e.getSource() == this.stop)
			{
				if (optimizer.stop())
					this.changeEnable(false);
			}
		}
		finally
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void changeEnable(boolean enable)
	{
		this.initData.setEnabled(enable);
		this.initTraining.setEnabled(enable);
		this.initInput.setEnabled(enable);
		this.initConstraints.setEnabled(enable);
		this.runModel.setEnabled(enable);
		this.runOptimize.setEnabled(enable);
		this.stop.setEnabled(enable);
		this.start.setEnabled(!enable);
	}

	@Override
	public void dispose()
	{
		// deregister connector
		if (this.stop.isEnabled())
			this.optimizer.stop();

		super.dispose();

		// count down closing latch
		latch.countDown();
	}

	/**
	 * This function is used to wait for the closing of the frame.
	 */
	public void await()
	{
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}
