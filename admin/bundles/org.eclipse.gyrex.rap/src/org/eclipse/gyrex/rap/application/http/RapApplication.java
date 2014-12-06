/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.rap.application.http;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.http.application.Application;
import org.eclipse.gyrex.http.application.context.IResourceProvider;
import org.eclipse.gyrex.rap.application.RapApplicationConfiguration;
import org.eclipse.gyrex.rap.internal.RapActivator;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.engine.RWTServlet;

import org.osgi.framework.Bundle;

/**
 * A Gyrex RAP application.
 * <p>
 * This class may be subclassed by clients providing a custom RAP application
 * type.
 * </p>
 *
 * @see RapApplicationProvider
 */
public class RapApplication extends Application {

	private static final String ALIAS_RWT_SERVLET = "/";
	private static final String ALIAS_RWT_RESOURCES = ALIAS_RWT_SERVLET + ApplicationRunner.RESOURCES;

	private final RapApplicationConfiguration applicationConfiguration;
	private ApplicationRunner applicationRunner;

	/**
	 * Creates a new application instance.
	 *
	 * @param id
	 *            the application id
	 * @param context
	 *            the context
	 * @param applicationConfiguration
	 *            the RAP application configuration
	 */
	protected RapApplication(final String id, final IRuntimeContext context, final RapApplicationConfiguration applicationConfiguration) {
		super(id, context);
		checkArgument(applicationConfiguration != null, "application configuration must not be null");
		this.applicationConfiguration = applicationConfiguration;
	}

	@Override
	protected void doDestroy() {
		getApplicationContext().unregister(ALIAS_RWT_RESOURCES);
		getApplicationContext().unregister(ALIAS_RWT_SERVLET);
		if (applicationRunner != null) {
			applicationRunner.stop();
			applicationRunner = null;
		}
	}

	/**
	 * Registers the servlets and resources required for RAP.
	 */
	@Override
	protected void doInit() throws IllegalStateException, Exception {
		// initialize resource root location
		final IPath contextBase = getResourceRootLocation();

		// set directory for resources
		getApplicationContext().getServletContext().setAttribute(ApplicationConfiguration.RESOURCE_ROOT_LOCATION, contextBase.toString());

		// initialize and start RWT application
		applicationRunner = new ApplicationRunner(getApplicationConfiguration(), getApplicationContext().getServletContext());
		applicationRunner.start();

		// register RWTServlet for the entry point
		getApplicationContext().registerServlet(ALIAS_RWT_SERVLET, new RWTServlet(), null);

		// serve context resources (required for RAP/RWT resources)
		getApplicationContext().registerResources(ALIAS_RWT_RESOURCES, ApplicationRunner.RESOURCES, new IResourceProvider() {

			@Override
			public URL getResource(final String path) throws MalformedURLException {
				final String canonicalPath = URIUtil.canonicalPath(path);
				if (canonicalPath == null)
					return null;

				// resolve from context path
				return contextBase.append(canonicalPath).toFile().toURI().toURL();
			}

			@Override
			public Set<String> getResourcePaths(final String path) {
				// directory listing not supported
				return null;
			}
		});
	}

	/**
	 * Returns the application configuration.
	 *
	 * @return the application configuration
	 */
	protected RapApplicationConfiguration getApplicationConfiguration() {
		return applicationConfiguration;
	}

	private Bundle getBundle() {
		return RapActivator.getInstance().getBundle();
	}

	private IPath getResourceRootLocation() {
		final String resourceRootLocation = getApplicationContext().getInitProperties().get(ApplicationConfiguration.RESOURCE_ROOT_LOCATION);
		if ((resourceRootLocation != null) && (resourceRootLocation.length() > 0))
			// use the one specified as application property
			return new Path(resourceRootLocation);

		// fallback to default
		return Platform.getStateLocation(getBundle()).append("contexts").append(getId()).append(getContext().getContextPath().toString().replace('/', '_'));
	}
}
