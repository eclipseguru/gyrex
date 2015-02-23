/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation in Jetty
 *******************************************************************************/
package org.eclipse.gyrex.boot.internal.jmx;

import javax.management.remote.JMXServiceURL;

import org.eclipse.gyrex.boot.internal.app.ServerApplication;
import org.eclipse.gyrex.server.Platform;
import org.eclipse.gyrex.server.settings.SystemSetting;

import org.eclipse.jetty.jmx.ConnectorServer;

import org.apache.commons.lang.UnhandledException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around Jetty {@link ConnectorServer} for easier JMX access through
 * firewalls.
 */
public class JettyJmxConnector {

	private static enum Status {
		STARTING, STARTED
	}

	static synchronized void doStart() throws Exception {
		if (state != Status.STARTING)
			return;

		// using defaults from http://wiki.eclipse.org/Jetty/Tutorial/JMX#Enabling_JMXConnectorServer_for_Remote_Access
		// allow port and host override through arguments
		final String host = jmxConnectorHostSetting.get();
		final int port = jmxConnectorPortSetting.get();

		// TODO: may want to support protected access using <instance-location>/etc/jmx/... files

		LOG.info("Enabling JMX remote connections on port {} (host {}).", new Object[] { port, host });

		final JMXServiceURL url = new JMXServiceURL("rmi", host, port, String.format("/jndi/rmi://%s:%d/jmxrmi", host, port));
		connectorServer = new ConnectorServer(url, null, "org.eclipse.gyrex.jmx:name=rmiconnectorserver");
		connectorServer.start();

		state = Status.STARTED;
	}

	public static synchronized void start() throws Exception {
		if (skipJmxConnectorSetting.isTrue())
			return;

		if ((state == Status.STARTING) || (state == Status.STARTED))
			throw new IllegalStateException("already started");

		state = Status.STARTING;
		final Thread t = new Thread("JettyJMXConnectorStart") {
			@Override
			public void run() {
				try {
					doStart();
				} catch (final ClassNotFoundException | LinkageError | AssertionError e) {
					LOG.warn("Jetty JMX is not available. Please configure JMX support manually. ({})", e.getMessage());
				} catch (final Exception e) {
					ServerApplication.shutdown(new UnhandledException("An error occured while starting the embedded JMX server. Please verify the port/host configuration is correct and no other server is running. JMX can also be disabled by setting system property 'gyrex.jmxrmi.skip' to true.", e));
				}
			};
		};
		t.setDaemon(true);
		t.start();
	}

	public static synchronized void stop() throws Exception {
		state = null;
		if (connectorServer == null)
			return;

		try {
			connectorServer.stop();
		} catch (final Exception e) {
			// ignore
		} finally {
			connectorServer = null;
		}
	}

	private static final String DEFAULT_JMXRMI_HOST = "localhost";

	private static final int DEFAULT_JMXRMI_PORT = 1099;
	private static final Logger LOG = LoggerFactory.getLogger(JettyJmxConnector.class);
	private static ConnectorServer connectorServer;

	private static Status state;

	private static final SystemSetting<Boolean> skipJmxConnectorSetting = SystemSetting.newBooleanSetting("gyrex.jmxrmi.skip", "Prevents start of the built-in JMX connector for easier access through firewalls.").create();
	private static final SystemSetting<String> jmxConnectorHostSetting = SystemSetting.newStringSetting("gyrex.jmxrmi.host", "Host for accepting JMX connections.").usingDefault(DEFAULT_JMXRMI_HOST).create();
	private static final SystemSetting<Integer> jmxConnectorPortSetting = SystemSetting.newIntegerSetting("gyrex.jmxrmi.port", "Port for accepting JMX connections.").usingDefault(Platform.getInstancePort(DEFAULT_JMXRMI_PORT)).create();
}
