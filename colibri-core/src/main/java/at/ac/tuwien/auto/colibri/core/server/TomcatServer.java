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

package at.ac.tuwien.auto.colibri.core.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanFilter;

public class TomcatServer
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(TomcatServer.class.getName());

	/**
	 * Tomcat instance
	 */
	private Tomcat tomcat = null;

	/**
	 * Thread for the Tomcat execution
	 */
	private Thread execution = null;

	/**
	 * Initialization of Tomcat server.
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	public TomcatServer() throws IOException, ServletException
	{
		log.info("Initializing Tomcat server");

		// create temporary directories
		Path tempPathTomcat = Files.createTempDirectory("tomcat-basedir");
		Path tempPathWebapp = Files.createTempDirectory("tomcat-webapp");

		// load configuration
		Properties properties = this.loadProperties();

		// set properties
		System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
		TomcatURLStreamHandlerFactory.disable();

		// initialize Tomcat
		tomcat = new Tomcat();
		tomcat.setBaseDir(tempPathTomcat.toString());
		tomcat.setPort(Integer.valueOf(properties.getProperty("port", "8080")));
		tomcat.getHost().setAppBase(tempPathTomcat.toString());
		tomcat.getHost().setAutoDeploy(true);
		tomcat.getHost().setDeployOnStartup(true);
		tomcat.enableNaming();

		// configure Web application
		this.initWebapp(tempPathWebapp.toString(), properties);

		// copy files
		this.copyFiles(tempPathWebapp.toString(), properties, "app");
		this.copyFiles(tempPathWebapp.toString() + "/WEB-INF/classes", properties, "classes");
		this.copyFiles(tempPathWebapp.toString() + "/WEB-INF/lib", properties, "lib");

		// // define new location for WEB-INF/classes folder
		// WebResourceRoot resources = new StandardRoot(context);
		// WebResourceSet resourceSet;
		//
		// File dir = new File(tempPathWebapp.toString() + "/WEB-INF/classes");
		//
		// if (dir.exists())
		// {
		// resourceSet = new DirResourceSet(resources, "/WEB-INF/classes", dir.getAbsolutePath(),
		// "/");
		// log.info("Loading WEB-INF resources (" + dir.getAbsolutePath());
		// }
		// else
		// {
		// resourceSet = new EmptyResourceSet(resources);
		// }
		// resources.addPreResources(resourceSet);
		//
		// context.setResources(resources);

		// // set database connection programmatically
		// ContextResource resource = new ContextResource();
		// resource.setName("jdbc/Colibri");
		// resource.setAuth("Container");
		// resource.setType("javax.sql.DataSource");
		// resource.setScope("Shareable");
		// resource.setProperty("driverClassName", "org.postgresql.Driver");
		// resource.setProperty("url", "jdbc:postgresql://localhost:5432/Colibri");
		// tomcat.getServer().getGlobalNamingResources().addResource(resource);
		// context.getNamingResources().addResource(resource);
	}

	/**
	 * Load properties from config file.
	 * 
	 * @throws IOException
	 */
	private Properties loadProperties() throws IOException
	{
		Properties properties = new Properties();
		URL url = this.getClass().getResource("/core.properties");
		BufferedInputStream stream = new BufferedInputStream(url.openStream());
		properties.load(stream);
		stream.close();

		return properties;
	}

	/**
	 * Add Web content to standard context.
	 * 
	 * @param path Path of Web application folder
	 * @param properties Configuration properties
	 * @throws ServletException
	 * @throws IOException
	 */
	private void initWebapp(String path, Properties properties) throws ServletException, IOException
	{
		File content = new File(path);
		log.info("Configuring Web app (" + content.getAbsolutePath() + ")");

		// copy config files to webapp
		this.copyFile("/context.xml", content.toString() + "/META-INF");
		this.copyFile("/web.xml", content.toString() + "/WEB-INF");

		// getting standard context
		StandardContext context = (StandardContext) tomcat.addWebapp("", content.getAbsolutePath());
		context.setParentClassLoader(TomcatServer.class.getClassLoader());

		// disable clearing RMI references when shutting down
		// (https://issues.onehippo.com/browse/HSTTWO-3737)
		context.setClearReferencesRmiTargets(false);

		// disable TLD scanning by default.
		if (System.getProperty(Constants.SKIP_JARS_PROPERTY) == null && System.getProperty(Constants.SKIP_JARS_PROPERTY) == null)
		{
			log.info("Disabling TLD scanning");
			StandardJarScanFilter jarScanFilter = (StandardJarScanFilter) context.getJarScanner().getJarScanFilter();
			jarScanFilter.setTldSkip("*");
			jarScanFilter.setDefaultPluggabilityScan(false);
			jarScanFilter.setDefaultTldScan(false);
		}
	}

	/**
	 * Copying files that are specified in the configuration file to given path.
	 * 
	 * @param properties Configuration properties
	 * @param path Path of WEB-INF folder
	 * @param variable Prefix of configuration variable
	 * @throws IOException
	 */
	private void copyFiles(String path, Properties properties, String variable) throws IOException
	{
		int fileCount = 1;
		while (properties.containsKey(variable + fileCount))
		{
			String f = properties.getProperty(variable + fileCount);
			this.copyFile(f, path.toString());
			fileCount++;
		}
	}

	/**
	 * Copies the given resource (srcFile) to the destination folder of the file system
	 * (destFolder).
	 * 
	 * @param srcFile Path to the resource
	 * @param destFolder File system folder
	 * @throws IOException
	 */
	private void copyFile(String srcFile, String destFolder) throws IOException
	{
		// get URL of resource
		URL url = this.getClass().getResource(srcFile);

		// create streams
		InputStream is = url.openStream();
		FileOutputStream os = new FileOutputStream(this.getFile(destFolder, srcFile));

		// write files
		int read;
		byte[] buffer = new byte[1024];
		while ((read = is.read(buffer)) != -1)
			os.write(buffer, 0, read);

		// close streams
		os.close();
		is.close();
	}

	/**
	 * Returns a file handle on a given file in a given folder.
	 * 
	 * @param folder Folder where the file will be located
	 * @param file File path including relative directories
	 * @return
	 */
	private File getFile(String folder, String file)
	{
		file = folder + "/" + file;

		// is there a directory in the path
		if (file.lastIndexOf('/') >= 0)
		{
			// directory part
			String temp = file.substring(0, file.lastIndexOf('/'));

			// make directories
			File dir = new File(temp);
			dir.mkdirs();
		}

		// return file handle
		return new File(file);
	}

	/**
	 * Starts embedded Tomcat server.
	 */
	public void start()
	{
		log.info("Starting Tomcat server");

		// create thread
		this.execution = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					// start Tomcat and keep waiting
					tomcat.start();
					tomcat.getServer().await();
				}
				catch (LifecycleException e)
				{
					log.severe(e.getMessage());
				}
			}
		});

		// execute thread
		this.execution.start();
	}

	/**
	 * Stops embedded Tomcat server.
	 */
	public void stop()
	{
		log.info("Stopping Tomcat server");

		try
		{
			// Tomcat server was initialized
			if (this.tomcat != null)
			{
				// stop server (thread will be stopped)
				this.tomcat.stop();

				// destroy object
				this.tomcat.destroy();
			}
		}
		catch (LifecycleException e)
		{
			log.info("Tomcat already stopped (" + e.getMessage() + ")");
		}
	}
}
