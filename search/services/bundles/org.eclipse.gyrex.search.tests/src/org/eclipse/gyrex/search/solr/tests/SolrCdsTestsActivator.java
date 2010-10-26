/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.tests;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.persistence.storage.registry.IRepositoryRegistry;

import org.osgi.framework.BundleContext;

public class SolrCdsTestsActivator extends BaseBundleActivator {

	/** BSN */
	private static final String SYMBOLIC_NAME = "org.eclipse.gyrex.cds.solr.tests";

	private static SolrCdsTestsActivator instance;

	static SolrCdsTestsActivator getInstance() {
		final SolrCdsTestsActivator instance = SolrCdsTestsActivator.instance;
		if (instance == null) {
			throw new IllegalStateException("inactive");
		}
		return instance;
	}

	private IServiceProxy<IRepositoryRegistry> repositoryRegistryProxy;

	public SolrCdsTestsActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
		repositoryRegistryProxy = getServiceHelper().trackService(IRepositoryRegistry.class);
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}

	public IRepositoryRegistry getRepositoryRegistry() {
		final IServiceProxy<IRepositoryRegistry> proxy = repositoryRegistryProxy;
		if (proxy == null) {
			throw createBundleInactiveException();
		}
		return proxy.getService();
	}

}
