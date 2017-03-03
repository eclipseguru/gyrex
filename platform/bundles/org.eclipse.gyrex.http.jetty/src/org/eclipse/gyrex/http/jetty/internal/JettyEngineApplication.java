/*******************************************************************************
 * Copyright (c) 2011, 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.http.jetty.internal;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
import org.eclipse.gyrex.http.internal.application.gateway.IHttpGateway;
import org.eclipse.gyrex.http.jetty.admin.ChannelDescriptor;
import org.eclipse.gyrex.http.jetty.admin.ICertificate;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;
import org.eclipse.gyrex.http.jetty.internal.app.JettyGateway;
import org.eclipse.gyrex.http.jetty.internal.connectors.CertificateSslContextFactory;
import org.eclipse.gyrex.monitoring.metrics.MetricSet;
import org.eclipse.gyrex.preferences.CloudScope;
import org.eclipse.gyrex.server.Platform;
import org.eclipse.gyrex.server.settings.SystemSetting;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NegotiatingServerConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyEngineApplication implements IApplication {

	private static final Logger LOG = LoggerFactory.getLogger(JettyEngineApplication.class);

	// OSGi Http Service suggest these properties for setting the default ports
	private static final String ORG_OSGI_SERVICE_HTTP_PORT = "org.osgi.service.http.port"; //$NON-NLS-1$

	//	private static final String ORG_OSGI_SERVICE_HTTP_PORT_SECURE = "org.osgi.service.http.port.secure"; //$NON-NLS-1$
	private static final SystemSetting<Integer> httpServicePort = SystemSetting.newIntegerSetting(ORG_OSGI_SERVICE_HTTP_PORT, "Port for the OSGi HttpService to listen on (will only be used if no channels are configured).").usingDefault(Platform.getInstancePort(8080)).create();

	/** Exit object indicating error termination */
	private static final Integer EXIT_ERROR = Integer.valueOf(1);

	private static final AtomicReference<CountDownLatch> stopSignalRef = new AtomicReference<CountDownLatch>(null);
	private static final AtomicReference<Throwable> jettyErrorRef = new AtomicReference<Throwable>();

	private static Map<MetricSet, ServiceRegistration<MetricSet>> metricsRegistrations = new ConcurrentHashMap<>();

	/**
	 * Force a shutdown of the ZooKeeper gate.
	 */
	public static void forceShutdown() {
		final CountDownLatch stopSignal = stopSignalRef.get();
		if (stopSignal != null) {
			stopSignal.countDown();
		}
	}

	public static ServiceRegistration<MetricSet> registerMetrics(final MetricSet metricSet) {
		final ServiceRegistration<MetricSet> metricsRegistration = HttpJettyActivator.getInstance().getServiceHelper().registerService(MetricSet.class, metricSet, "Eclipse Gyrex", metricSet.getDescription(), null, null);
		final ServiceRegistration<MetricSet> oldRegistration = metricsRegistrations.put(metricSet, metricsRegistration);
		if ((null != oldRegistration) && (oldRegistration != metricsRegistration)) {
			try {
				oldRegistration.unregister();
			} catch (final IllegalStateException e) {
				// ignore
			}
		}
		return metricsRegistration;
	}

	public static void unregisterMetrics(final MetricSet metrics) {
		final ServiceRegistration<MetricSet> registration = metricsRegistrations.remove(metrics);
		if (null != registration) {
			try {
				registration.unregister();
			} catch (final IllegalStateException e) {
				// ignore
			}
		}
	}

	private void configureServer(final Server server) {
		if (JettyDebug.engine) {
			LOG.debug("Configuring server {}", server);
		}

		// collect node properties for filtering
		final Map<String, Object> nodeProperties = getNodeProperties();

		// create channels
		final IJettyManager jettyManager = HttpJettyActivator.getInstance().getJettyManager();
		final Collection<ChannelDescriptor> channels = jettyManager.getChannels();
		if (!channels.isEmpty()) {
			for (final ChannelDescriptor channel : channels) {
				createConnector(server, channel, jettyManager, nodeProperties);
			}
		} else {
			// start a default channel in development mode
			// or if OSGi property is explicitly set (bug 358859)
			if (Platform.inDevelopmentMode()) {
				final int port = httpServicePort.get();
				LOG.info("No channels configured. Enabling default channel on port {} in development mode.", port);
				final ChannelDescriptor defaultChannel = new ChannelDescriptor();
				defaultChannel.setId("default");
				defaultChannel.setPort(port);
				createConnector(server, defaultChannel, jettyManager, nodeProperties);
			} else if (httpServicePort.isSet()) {
				final int port = httpServicePort.get();
				LOG.info("No channels configured. Enabling channel on port {} configured via system property '{}'.", port, ORG_OSGI_SERVICE_HTTP_PORT);
				final ChannelDescriptor defaultChannel = new ChannelDescriptor();
				defaultChannel.setId("default");
				defaultChannel.setPort(port);
				createConnector(server, defaultChannel, jettyManager, nodeProperties);
			}
		}

		// tweak server
		server.setStopAtShutdown(true);
		server.setStopTimeout(5000);

		// set thread pool
		// TODO: (Jetty9?) final QueuedThreadPool threadPool = new QueuedThreadPool();
		// TODO: (Jetty9?) threadPool.setName("jetty-server");
		// TODO: (Jetty9?) server.setThreadPool(threadPool);
	}

	private void createConnector(final Server server, final ChannelDescriptor channel, final IJettyManager jettyManager, final Map<String, Object> nodeProperties) {
		if ((channel.getPort() <= 0) || (channel.getPort() > 65535)) {
			if (JettyDebug.engine) {
				LOG.debug("Ignoring disabled channel {}", channel);
			}
			return;
		}

		try {
			final String filter = channel.getNodeFilter();
			if (filter != null) {
				final Filter nodeFilter = FrameworkUtil.createFilter(filter);
				if (!nodeFilter.matches(nodeProperties)) {
					if (JettyDebug.engine) {
						LOG.debug("Ignoring channel {} which has a node filter that does not match this node.", channel);
					}
					return;
				}
			}

			if (JettyDebug.engine) {
				LOG.debug("Configuring channel {}", channel);
			}

			SslContextFactory sslFactory = null;
			if (channel.isSecure()) {
				final ICertificate certificate = jettyManager.getCertificate(channel.getCertificateId());
				sslFactory = new CertificateSslContextFactory(certificate);
			}

			final HttpConfiguration httpConfig = new HttpConfiguration();
			if (null != channel.getSecureChannelId()) {
				final ChannelDescriptor secureChannel = jettyManager.getChannel(channel.getSecureChannelId());
				if (secureChannel != null) {
					httpConfig.setSecurePort(secureChannel.getPort());
					httpConfig.setSecureScheme(HttpScheme.HTTPS.asString());
				}
			}

			final ServerConnector connector = createJettyConnector(server, sslFactory, httpConfig);
			connector.setPort(channel.getPort());
			connector.setIdleTimeout(200000);

			server.addConnector(connector);
		} catch (final Exception e) {
			LOG.warn("Error configuring channel {}. Please check the channel configuration. {}", channel.getId(), ExceptionUtils.getRootCauseMessage(e));
		}
	}

	private ServerConnector createJettyConnector(final Server server, final SslContextFactory sslFactory, final HttpConfiguration httpConfig) {
		final HttpConnectionFactory http1 = new HttpConnectionFactory(httpConfig);
		if (sslFactory != null) {
			try {
				// use HTTP/2 if ALPN is available via -Xbootclassloader
				NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
				final HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(httpConfig);
				final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();

				// default to 1.1
				alpn.setDefaultProtocol(http1.getProtocol());

				// sort TLS ciphers to prefer HTTP/2
				sslFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
				sslFactory.setUseCipherSuitesOrder(true);

				LOG.info("Using HTTP/2 with fallback to HTTP/1.1 on port {}.", httpConfig.getSecurePort());
				return new ServerConnector(server, sslFactory, alpn, http2, http1);
			} catch (AssertionError | LinkageError | ClassNotFoundException | IllegalStateException e) {
				if (JettyDebug.engine) {
					LOG.debug("Jetty HTTP/2 environment not available: {}", ExceptionUtils.getRootCauseMessage(e), e);
				}
				LOG.warn("Jetty HTTP/2 compatible environment not available. To use HTTP/2 in Jetty you need to add the ALPN boot Jar in the boot classpath (see https://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-starting).");
			} catch (final Exception e) {
				LOG.error("Error loading the Jetty HTTP/2 implementation. {}", ExceptionUtils.getRootCauseMessage(e), e);
			}
		}

		LOG.info("Using HTTP/1.1 only implementation.");
		return new ServerConnector(server, sslFactory, http1);
	}

	private Map<String, Object> getNodeProperties() {
		final INodeEnvironment nodeEnvironment = HttpJettyActivator.getInstance().getNodeEnvironment();
		final Map<String, Object> nodeProperties = new HashMap<String, Object>(2);
		nodeProperties.put("id", nodeEnvironment.getNodeId());
		final Set<String> tags = nodeEnvironment.getTags();
		if (!tags.isEmpty()) {
			nodeProperties.put("tag", tags.toArray(new String[tags.size()]));
		}
		return nodeProperties;
	}

	boolean isActive() {
		final CountDownLatch stopSignal = stopSignalRef.get();
		return (stopSignal != null) && (stopSignal.getCount() > 0);
	}

	void signalStopped(final Throwable jettyError) {
		if (JettyDebug.engine) {
			LOG.debug("Received stop signal for Jetty engine.");
		}
		final CountDownLatch signal = stopSignalRef.get();
		if (null != signal) {
			jettyErrorRef.set(jettyError);
			signal.countDown();
		}
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		if (JettyDebug.engine) {
			LOG.debug("Starting Jetty engine.");
		}

		// set stop signal
		final CountDownLatch stopSignal = new CountDownLatch(1);
		if (!stopSignalRef.compareAndSet(null, stopSignal))
			throw new IllegalStateException("Jetty engine already running!");

		try {
			// FIXME timing issue with "ON_CLOUD_CONNECT" and ZooKeeperBasedPreferences
			// there is a bit of a timing issue here; we need to wait a bit in order
			// for the PlatformPreferences to be available
			int timeout = 5000;
			while (timeout > 0) {
				try {
					CloudScope.INSTANCE.getNode(HttpJettyActivator.SYMBOLIC_NAME);
					break;
				} catch (final IllegalStateException e) {
					if (JettyDebug.engine) {
						LOG.debug("Platform preferences not available. Jetty start will be delayed.");
						try {
							timeout -= 500;
							Thread.sleep(500);
						} catch (final Exception e1) {
							// interrupted
							Thread.currentThread().interrupt();
						}
					}
				}
			}

			// initialize (but do not start) the Jetty server
			final Server server = new Server();

			// enable Jetty JMX support
			final MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
			server.addBean(mbContainer);

			// register Jetty loggers as MBeans
			mbContainer.beanAdded(server, Log.getRootLogger());

			// create gateway
			JettyGateway gateway = new JettyGateway(server);

			// tweak server config
			configureServer(server);

			// start the server
			server.start();

			// don't expose too detailed version info
			// (must be set after server started)
			HttpGenerator.setJettyVersion("Jetty");

			if (JettyDebug.engine) {
				LOG.debug("Jetty server started!");
				LOG.debug(server.dump());
			}

			// activate HTTP gateway
			final ServiceRegistration<IHttpGateway> gatewayServiceRegistration = HttpJettyActivator.getInstance().getServiceHelper().registerService(IHttpGateway.class, gateway, "Eclipse Gyrex", "Jetty based HTTP gateway.", null, null);

			if (JettyDebug.engine) {
				LOG.debug("Jetty HTTP gateway registered!");
				LOG.debug(server.dump());
			}

			// signal running
			context.applicationRunning();

			// wait for stop
			try {
				stopSignal.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			// remove gateway
			gatewayServiceRegistration.unregister();

			// remove metrics
			while (!metricsRegistrations.isEmpty()) {
				for (final MetricSet metric : metricsRegistrations.keySet()) {
					unregisterMetrics(metric);
				}
			}

			// shutdown Jetty
			try {
				server.stop();
			} catch (final Exception e) {
				if (JettyDebug.debug) {
					LOG.warn("Error while stopping Jetty. {}", new Object[] { ExceptionUtils.getRootCauseMessage(e), e });
				} else {
					LOG.warn("Error while stopping Jetty. {}", ExceptionUtils.getRootCauseMessage(e));
				}
			}

			// destroy gateway
			if (null != gateway) {
				gateway.close();
				gateway = null;
			}

			if (JettyDebug.engine) {
				LOG.debug("Jetty engine shutdown complete.");
			}

			// exit
			final Throwable error = jettyErrorRef.getAndSet(null);
			return error == null ? IApplication.EXIT_OK : EXIT_ERROR;
		} catch (final Exception e) {
			// shutdown the whole server when Jetty does not come up
			LOG.error("Unable to start Jetty. Please check the log files. System will be shutdown.", e);
//			ServerApplication.signalShutdown(new Exception("Could not start the Jetty server. " + ExceptionUtils.getRootCauseMessage(e), e));
			return EXIT_ERROR;
		} finally {
			// done, now reset signal to allow further starts
			stopSignalRef.compareAndSet(stopSignal, null);
		}
	}

	@Override
	public void stop() {
		signalStopped(null);
	}

}
