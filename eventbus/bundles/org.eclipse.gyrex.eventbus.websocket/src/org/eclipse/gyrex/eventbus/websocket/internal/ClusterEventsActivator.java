/**
 * Copyright (c) 2014 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Mihm - initial API and implementation
 */
package org.eclipse.gyrex.eventbus.websocket.internal;

import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.common.services.IServiceProxy;

import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class ClusterEventsActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.eventbus.websocket"; //$NON-NLS-1$

	private static volatile ClusterEventsActivator instance;

	public static ClusterEventsActivator getInstance() {
		final ClusterEventsActivator activator = instance;
		if (activator == null)
			throw new IllegalStateException("inactive");
		return activator;
	}

	private IServiceProxy<ICloudManager> cloudManagerProxy;

	public ClusterEventsActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
		cloudManagerProxy = getServiceHelper().trackService(ICloudManager.class);
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}

	public ICloudManager getCloudManager() {
		return cloudManagerProxy.getProxy();
	}

}
