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

package at.ac.tuwien.auto.colibri.connector.knx;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import at.ac.tuwien.auto.colibri.connector.ConnectionType;
import at.ac.tuwien.auto.colibri.connector.Connector;
import at.ac.tuwien.auto.colibri.connector.knx.datapoint.DPST_1_1;
import at.ac.tuwien.auto.colibri.data.AccessService;
import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

public class KNXConnector implements Connector
{
	/**
	 * Access interface
	 */
	private AccessService access;

	/**
	 * Own IP address
	 */
	private String address;

	/**
	 * Generated IP address
	 */
	private String ip;

	/**
	 * Hostname of KNX IP router
	 */
	private String hostname;

	/**
	 * Port of IP router
	 */
	private int port;

	/**
	 * Calimero process communicator
	 */
	private ProcessCommunicator pc;

	/**
	 * Calimero IP link
	 */
	private KNXNetworkLinkIP nl;

	/**
	 * List of registered listeners per group address
	 */
	private final Hashtable<Integer, ArrayList<KNXListener>> listeners = new Hashtable<Integer, ArrayList<KNXListener>>();

	/**
	 * Connection flag
	 */
	private boolean connected = false;

	/**
	 * Test datapoint
	 */
	private DPST_1_1 test;

	/**
	 * Constructor for initializing values
	 * 
	 * @param access
	 * @param address
	 * @param hostname
	 * @param port
	 */
	public KNXConnector(AccessService access, String address, String hostname, int port)
	{
		this.access = access;
		this.address = address;
		this.port = port;
		this.hostname = hostname;

		// TODO replace this by dynamic creation of available datapoints (config
		// file, ...)
		try
		{
			test = new DPST_1_1(new GroupAddress("1.0.0"), this);
		}
		catch (KNXFormatException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns process communicator
	 * 
	 * @return
	 */
	public ProcessCommunicator getProcessCommunicator()
	{
		return pc;
	}

	@Override
	public void closeConnection()
	{
		test = null;

		// TODO close connection to data store

	}

	@Override
	public void openConnection()
	{
		// open connection to data store
		access.open(address);

		// open connection to KNX bus
		try
		{
			synchronized (this)
			{
				System.out.println("Connecting KNX tunnel - Tunnel, " + address + ", " + hostname + ", " + port);

				if ("auto".equals(address))
				{
					// detecting IP address if auto detection is configured

					System.out.println("auto detetecting local IP.");
					String detectedLocalIP = "";
					int curSimilarity = 0;
					InetAddress routerAddress = InetAddress.getByName(hostname);

					try
					{
						Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

						while (networkInterfaces.hasMoreElements())
						{
							NetworkInterface ni = networkInterfaces.nextElement();
							Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
							while (inetAddresses.hasMoreElements())
							{
								InetAddress inetAddress = inetAddresses.nextElement();
								String hostAddress = inetAddress.getHostAddress();

								int i = 0;
								for (i = 0; i < Math.min(routerAddress.getHostAddress().length(), hostAddress.length()); i++)
								{
									if (routerAddress.getHostAddress().charAt(i) != hostAddress.charAt(i))
									{
										break;
									}
								}

								if (i >= curSimilarity)
								{
									curSimilarity = i;
									detectedLocalIP = hostAddress;
								}

							}
						}
					}
					catch (SocketException e)
					{
						e.printStackTrace();
					}
					ip = detectedLocalIP;
					System.out.println("detectedLocalIP: " + ip + " with similarity " + curSimilarity);
				}
				else
				{
					// set address manually

					ip = address;
				}

				// create network link
				nl = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNELING, new InetSocketAddress(InetAddress.getByName(ip), 0), new InetSocketAddress(InetAddress.getByName(hostname), port), false, new TPSettings(false));
				System.out.println("KNX address: " + nl.getKNXMedium().getDeviceAddress());
				pc = new ProcessCommunicatorImpl(nl);

				// update flag
				connected = true;

				// add link listener
				nl.addLinkListener(new KNXLinkListener());

				System.out.println("KNX connection established.");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public boolean testConnection(ConnectionType type)
	{
		// TODO test connection

		test.write(true);

		return true;
	}

	/**
	 * Adds listeners from datapoint instances to the hashtable
	 * 
	 * @param observation
	 * @param knxListener
	 */
	public void addListener(GroupAddress observation, KNXListener knxListener)
	{
		synchronized (listeners)
		{
			// add list of listeners to new address
			if (!listeners.containsKey(observation.getRawAddress()))
			{
				listeners.put(observation.getRawAddress(), new ArrayList<KNXListener>());
			}

			// log message
			System.out.println("Adding watchdog for address " + observation.getRawAddress());

			// TODO use log library

			// add listener
			listeners.get(observation.getRawAddress()).add(knxListener);
		}
	}

	/**
	 * Writes a value to the KNX bus
	 * 
	 * @param group KNX group address
	 * @param value new boolean value
	 */
	public void write(GroupAddress group, boolean value)
	{
		try
		{
			// check connection
			if (!connected)
			{
				return;
			}

			// log
			System.out.println("Writing " + value + " on " + group);

			// write value
			pc.write(group, value);
		}
		catch (KNXException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Represents a listener to the KNX bus
	 * 
	 * @author dschachinger
	 *
	 */
	private class KNXLinkListener implements NetworkLinkListener
	{
		@Override
		public void indication(FrameEvent e)
		{
			CEMILData data = (CEMILData) e.getFrame();

			GroupAddress target = (GroupAddress) data.getDestination();
			System.out.println("Received frame for " + target + " from " + data.getSource());

			synchronized (listeners)
			{
				// notify registered listeners
				if (listeners.containsKey(target.getRawAddress()))
				{
					for (KNXListener listener : listeners.get(target.getRawAddress()))
					{
						listener.notify(data.getPayload());
					}
				}
			}
		}

		@Override
		public void linkClosed(CloseEvent e)
		{
			// TODO implement
		}

		@Override
		public void confirmation(FrameEvent e)
		{
			// TODO implement
		}
	}
}
