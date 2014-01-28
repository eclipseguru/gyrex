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
package org.eclipse.gyrex.admin.ui.cloud.internal;

import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.common.services.IServiceProxy;

import org.eclipse.jface.resource.ImageDescriptor;

import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CloudUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.cloud"; //$NON-NLS-1$

	private static CloudUiActivator instance;

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return ImageDescriptor.createFromURL(getInstance().getBundle().getEntry(path));
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CloudUiActivator getInstance() {
		final CloudUiActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	private IServiceProxy<ICloudManager> cloudManagerProxy;
	private IServiceProxy<INodeEnvironment> nodeEnvironmentProxy;

	/**
	 * The constructor
	 */
	public CloudUiActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		cloudManagerProxy = getServiceHelper().trackService(ICloudManager.class);
		nodeEnvironmentProxy = getServiceHelper().trackService(INodeEnvironment.class);
		instance = this;
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}

	public ICloudManager getCloudManager() {
		return cloudManagerProxy.getProxy();
	}

	public INodeEnvironment getNodeEnvironment() {
		return nodeEnvironmentProxy.getProxy();
	}
}
