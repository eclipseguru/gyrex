/*******************************************************************************
 * Copyright (c) 2010, 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.gyrex.admin.ui.internal.application.AdminApplicationConfiguration;
import org.eclipse.gyrex.admin.ui.internal.jetty.AdminServletHolder;
import org.eclipse.gyrex.admin.ui.internal.jetty.SimpleAdminLoginService;
import org.eclipse.gyrex.admin.ui.internal.servlets.AdminServletTracker;
import org.eclipse.gyrex.admin.ui.internal.servlets.PreferencesServlet;
import org.eclipse.gyrex.boot.internal.app.ServerApplication;
import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.monitoring.diagnostics.StatusTracker;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.eclipse.swt.widgets.Display;

import org.osgi.framework.BundleContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator of the admin ui bundle. Serves also images.
 */
public class AdminUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui"; //$NON-NLS-1$

	private static final String IMAGE_REGISTRY = SYMBOLIC_NAME + "#imageRegistry";
	private static final int DEFAULT_ADMIN_PORT = 3110;
	private static final Logger LOG = LoggerFactory.getLogger(AdminUiActivator.class);

	private static volatile AdminUiActivator instance;
	private static volatile Server server;

	private static final String PROPERTY_ADMIN_SECURE = "gyrex.admin.secure";
	private static final String PROPERTY_ADMIN_AUTH = "gyrex.admin.auth";

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		final URL entry = instance.getBundle().getEntry(path);
		return ImageDescriptor.createFromURL(entry);
	}

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static AdminUiActivator getInstance() {
		final AdminUiActivator activator = instance;
		if (activator == null)
			throw new IllegalStateException("inactive");
		return activator;
	}

	private ApplicationRunner adminApplicationRunner;
	private StatusTracker statusTracker;

	/**
	 * The constructor
	 */
	public AdminUiActivator() {
		super(SYMBOLIC_NAME);
	}

	private void addNonSslConnector(final Server server) {
		final SelectChannelConnector connector = new SelectChannelConnector();

		connector.setPort(DEFAULT_ADMIN_PORT);
		connector.setMaxIdleTime(30000);
		connector.setLowResourcesConnections(20000);
		connector.setLowResourcesMaxIdleTime(5000);
		connector.setForwarded(true);

		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=356988 for an issue
		// with configuring the connector

		server.addConnector(connector);
	}

	private void addSslConnector(final Server server) {

		try {

			final File keystoreFile = Platform.getStateLocation(AdminUiActivator.getInstance().getBundle()).append("jettycerts").toFile();
			if (!keystoreFile.isFile()) {
				if (!keystoreFile.getParentFile().isDirectory() && !keystoreFile.getParentFile().mkdirs())
					throw new IllegalStateException("Error creating directory for jetty ssl certificates");

				final InputStream stream = getBundle().getEntry("cert/jettycerts.jks").openStream();
				FileUtils.copyInputStreamToFile(stream, keystoreFile);
				IOUtils.closeQuietly(stream);
			}

			final SslContextFactory sslContextFactory = new SslContextFactory(keystoreFile.getCanonicalPath());
			sslContextFactory.setKeyStorePassword("changeit");
			sslContextFactory.setKeyManagerPassword("changeit");

			final SslSelectChannelConnector connector = new SslSelectChannelConnector(sslContextFactory);

			connector.setPort(DEFAULT_ADMIN_PORT);
			connector.setMaxIdleTime(30000);
			connector.setLowResourcesConnections(20000);
			connector.setLowResourcesMaxIdleTime(5000);
			connector.setForwarded(true);

			connector.setConfidentialPort(DEFAULT_ADMIN_PORT);
			connector.setConfidentialScheme(HttpSchemes.HTTPS);
			connector.setIntegralPort(DEFAULT_ADMIN_PORT);
			connector.setIntegralScheme(HttpSchemes.HTTPS);

			server.addConnector(connector);
		} catch (final Exception e) {
			throw new IllegalStateException("Error configuring jetty ssl connector for admin ui.", e);
		}

	}

	private void configureContextWithServletsAndResources(final ServletContextHandler contextHandler) throws MalformedURLException, IOException {
		// configure context base directory (required for RAP/RWT resources)
		final IPath contextBase = Platform.getStateLocation(getBundle()).append("context");
		contextHandler.setBaseResource(Resource.newResource(contextBase.toFile()));

		// configure defaults for resources served by Jetty's DefaultServlet
		if (Platform.inDevelopmentMode()) {
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.maxCachedFiles", "0");
		} else {
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "2000000");
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.maxCachedFileSize", "254000");
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.maxCachedFiles", "1000");
			contextHandler.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "true");
		}

		// initialize and start RWT application
		adminApplicationRunner = new ApplicationRunner(new AdminApplicationConfiguration(), contextHandler.getServletContext());
		adminApplicationRunner.start();

		// serve admin application directly
		contextHandler.addServlet(new AdminServletHolder(new RWTServlet()), "/admin");

		// register additional static resources references in body html
		final ServletHolder staticResources = new AdminServletHolder(new DefaultServlet());
		staticResources.setInitParameter("resourceBase", FileLocator.resolve(FileLocator.find(getBundle(), new Path("html"), null)).toExternalForm());
		contextHandler.addServlet(staticResources, "/static/*");

		// redirect to admin
		contextHandler.addServlet(new AdminServletHolder(new HttpServlet() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
				resp.sendRedirect("/admin");
			}
		}), "");

		// serve context resources (required for RAP/RWT resources)
		contextHandler.addServlet(new AdminServletHolder(new DefaultServlet()), "/*");

		// register Logback status servlet
		try {
			// note, we don't reference the class directly because the package import is optional
			final Class<?> servletClass = AdminUiActivator.getInstance().getBundle().loadClass("ch.qos.logback.classic.ViewStatusMessagesServlet");
			contextHandler.addServlet(new AdminServletHolder((Servlet) servletClass.newInstance()), "/logbackstatus");
		} catch (final ClassNotFoundException e) {
			LOG.warn("Logback status servlet not available. {}", e.getMessage(), e);
		} catch (final Exception e) {
			LOG.error("An error occurred while registering the Logback status servlet. {}", e.getMessage(), e);
		}

		// register Preferences status servlet
		contextHandler.addServlet(new AdminServletHolder(new PreferencesServlet()), "/preferences");

		// allow extension using custom servlets
		final AdminServletTracker adminServletTracker = new AdminServletTracker(getBundle().getBundleContext(), contextHandler);
		contextHandler.addBean(new AbstractLifeCycle() {
			@Override
			protected void doStart() throws Exception {
				adminServletTracker.open();
			}

			@Override
			protected void doStop() throws Exception {
				adminServletTracker.close();
			}
		});
	}

	private SecurityHandler createSecurityHandler(final Handler baseHandler, final String username, final String password) {
		final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
		final ConstraintMapping authenticationContraintMapping = new ConstraintMapping();
		final Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, AdminServletHolder.ADMIN_ROLE);
		constraint.setAuthenticate(true);
		authenticationContraintMapping.setConstraint(constraint);
		authenticationContraintMapping.setPathSpec("/*");
		securityHandler.addConstraintMapping(authenticationContraintMapping);
		securityHandler.setAuthenticator(new BasicAuthenticator());
		securityHandler.setHandler(baseHandler);
		securityHandler.setLoginService(new SimpleAdminLoginService(username, password));
		return securityHandler;
	}

	private HashSessionManager createSessionManager() {
		final HashSessionManager sessionManager = new HashSessionManager();
		sessionManager.setMaxInactiveInterval(1200);
		sessionManager.setUsingCookies(false); // allows to use RAP in multiple tabs
		return sessionManager;
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;

		statusTracker = new StatusTracker(context);
		statusTracker.open();

		// start the admin server asynchronously
		final Job jettyStartJob = new Job("Start Jetty Admin Server") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					startServer();
				} catch (final Exception e) {
					LOG.error("Failed to start Jetty Admin server.", e);
					ServerApplication.shutdown(new IllegalStateException("Unable to start Jetty admin server.", e));
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		jettyStartJob.setSystem(true);
		jettyStartJob.setPriority(Job.LONG);
		jettyStartJob.schedule();
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;

		statusTracker.close();
		statusTracker = null;

		stopServer();
	}

	public ImageRegistry getImageRegistry() {
		// ImageRegistry must be session scoped in RAP
		ImageRegistry imageRegistry = (ImageRegistry) RWT.getSessionStore().getAttribute(IMAGE_REGISTRY);
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry(Display.getCurrent());
			AdminUiImages.initializeImageRegistry(imageRegistry);
			RWT.getSessionStore().setAttribute(IMAGE_REGISTRY, imageRegistry);
		}
		return imageRegistry;
	}

	public IStatus getSystemStatus() {
		final StatusTracker tracker = statusTracker;
		if (tracker == null)
			throw createBundleInactiveException();
		return tracker.getSystemStatus();
	}

	private void startServer() {
		try {
			server = new Server();

			if (Boolean.getBoolean(PROPERTY_ADMIN_SECURE)) {
				addSslConnector(server);
			} else {
				addNonSslConnector(server);
			}

			// tweak server
			server.setSendServerVersion(false);
			server.setSendDateHeader(true); // required by some (older) browsers to support caching
			server.setGracefulShutdown(5000);

			// set thread pool
			final QueuedThreadPool threadPool = new QueuedThreadPool(5);
			threadPool.setName("jetty-server-admin");
			server.setThreadPool(threadPool);

			// create context
			final ServletContextHandler contextHandler = new ServletContextHandler();
			contextHandler.setSessionHandler(new SessionHandler(createSessionManager()));
			configureContextWithServletsAndResources(contextHandler);

			// enable security if configured
			if (Boolean.getBoolean(PROPERTY_ADMIN_SECURE)) {
				final String authenticationPhrase = System.getProperty(PROPERTY_ADMIN_AUTH);
				final String[] segments = authenticationPhrase.split("/");
				if (segments.length != 3)
					throw new IllegalArgumentException("Illegal authentication configuration. Must be three string separated by '/'");
				else if (!StringUtils.equals(segments[0], "BASIC"))
					throw new IllegalArgumentException("Illegal authentication configuration. Only method 'BASIC' is supported. Found " + segments[0]);

				server.setHandler(createSecurityHandler(contextHandler, segments[1], segments[2]));
			} else {
				server.setHandler(contextHandler);
			}
			server.start();

		} catch (final Exception e) {
			throw new IllegalStateException("Error starting jetty for admin ui", e);
		}
	}

	private void stopServer() {
		try {
			adminApplicationRunner.stop();
			adminApplicationRunner = null;

			server.stop();
			server = null;
		} catch (final Exception e) {
			throw new IllegalStateException("Error stopping jetty for admin ui", e);
		}
	}
}
