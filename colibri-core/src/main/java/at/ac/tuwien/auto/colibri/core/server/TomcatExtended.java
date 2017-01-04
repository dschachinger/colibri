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

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;

/**
 * This class can be used instead of the standard Tomcat class in TomcatServer to avoid the log
 * message "No global web.xml found".
 */
public class TomcatExtended extends Tomcat
{
	@Override
	public Context addWebapp(Host host, String contextPath, String docBase, ContextConfig config)
	{
		Context ctx = new StandardContext();
		String pathToGlobalWebXml = docBase + "/WEB-INF/web.xml";

		ctx.setPath(contextPath);
		ctx.setDocBase(docBase);
		ctx.addLifecycleListener(new DefaultWebXmlListener());
		ctx.addLifecycleListener(config);

		if (new File(pathToGlobalWebXml).exists())
		{
			config.setDefaultWebXml(pathToGlobalWebXml);
		}
		else
		{
			config.setDefaultWebXml(noDefaultWebXmlPath());
		}

		ctx.setConfigFile(getWebappConfigFile(docBase, contextPath));

		if (host == null)
		{
			getHost().addChild(ctx);
		}
		else
		{
			host.addChild(ctx);
		}

		return ctx;
	}
}
