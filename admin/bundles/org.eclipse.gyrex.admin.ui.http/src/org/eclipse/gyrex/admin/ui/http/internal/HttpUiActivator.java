/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import java.net.URL;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.http.application.manager.IApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;

import org.eclipse.jface.resource.ImageDescriptor;

import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class HttpUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.http";

	private static volatile HttpUiActivator instance;

	public static ApplicationManager getAppManager() {
		return (ApplicationManager) getInstance().getService(IApplicationManager.class);
	}

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
	public static HttpUiActivator getInstance() {
		final HttpUiActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	/**
	 * Creates a new instance.
	 */
	public HttpUiActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}
}
