/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
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

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;

import org.osgi.framework.BundleContext;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator of the admin ui bundle. Serves also images.
 */
public class AdminUiActivator extends BaseBundleActivator {

	/** the plug-in id */
	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui"; //$NON-NLS-1$

	/** the default port for the admin server */
	public static final int DEFAULT_ADMIN_PORT = 3110;

	/** server type for the admin server */
	public static final String TYPE_ADMIN = SYMBOLIC_NAME + ".http";

	private static final Logger LOG = LoggerFactory.getLogger(AdminUiActivator.class);
	private static volatile AdminUiActivator instance;

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
		final ImageDescriptor descriptor = ImageDescriptor.createFromURL(entry);
		return descriptor;
	}

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static AdminUiActivator getInstance() {
		final AdminUiActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	private AdminUiHttpServiceTracker adminUiHttpServiceTracker;

	/**
	 * The constructor
	 */
	public AdminUiActivator() {
		super(SYMBOLIC_NAME);
	}

	private Dictionary createAdminSettings(final BundleContext context) {
		final Dictionary<String, Object> settings = new Hashtable<String, Object>(4);
		settings.put(JettyConstants.OTHER_INFO, TYPE_ADMIN);
		settings.put(JettyConstants.HTTP_ENABLED, Boolean.TRUE);

		final int adminHttpPort = NumberUtils.toInt(context.getProperty("gyrex.admin.http.port"), 0);
		settings.put(JettyConstants.HTTP_PORT, (adminHttpPort > 0) && (adminHttpPort < 65535) ? adminHttpPort : DEFAULT_ADMIN_PORT);
		return settings;
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;

		// activate default servlet redirect
		adminUiHttpServiceTracker = new AdminUiHttpServiceTracker(context);
		adminUiHttpServiceTracker.open();

		// start the admin server
		new Job("Start Jetty Admin Server") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					JettyConfigurator.startServer("admin", createAdminSettings(context));
				} catch (final Exception e) {
					LOG.error("Failed to start Jetty Admin server.", e);
					return getStatusUtil().createError(0, "Failed to start Jetty Admin server.", e);
				}
				return Status.OK_STATUS;
			}
		}.schedule(100l);

	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;

		adminUiHttpServiceTracker.close();
		adminUiHttpServiceTracker = null;
	};

}
