/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.osgi.framework.BundleContext;

/**
 *
 */
public class JettyConfigActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.http.jetty";

	private static JettyConfigActivator instance;

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static JettyConfigActivator getInstance() {
		final JettyConfigActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	private IServiceProxy<IJettyManager> jettyManagerProxy;

	/**
	 * Creates a new instance.
	 */
	public JettyConfigActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
		jettyManagerProxy = getServiceHelper().trackService(IJettyManager.class);
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
		jettyManagerProxy = null;
	}

	/**
	 * Returns the jettyManagerProxy.
	 * 
	 * @return the jettyManager
	 */
	public IJettyManager getJettyManager() {
		return jettyManagerProxy.getService();
	}
}
