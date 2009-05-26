/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.service.solr.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.context.provider.RuntimeContextObjectProvider;
import org.osgi.framework.BundleContext;

public class SolrListingServiceActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.cds.service.solr";

	/**
	 * Creates a new instance.
	 * 
	 * @param symbolicName
	 */
	public SolrListingServiceActivator() {
		super(SYMBOLIC_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.common.runtime.BaseBundleActivator#doStart(org.osgi.framework.BundleContext)
	 */
	@Override
	protected void doStart(final BundleContext context) throws Exception {
		// register service provider
		getServiceHelper().registerService(RuntimeContextObjectProvider.class.getName(), new SolrListingsServiceProvider(), "Gyrex", "Solr based listings service", null, null);
	}
}
