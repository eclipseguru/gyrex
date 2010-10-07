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
package org.eclipse.gyrex.cds.solr.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.context.provider.RuntimeContextObjectProvider;
import org.osgi.framework.BundleContext;

public class ListingsSolrModelActivator extends BaseBundleActivator {

	/** <code>"org.eclipse.gyrex.cds.model.solr"</code> */
	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.cds.model.solr";

	private static final AtomicReference<ListingsSolrModelActivator> instance = new AtomicReference<ListingsSolrModelActivator>();

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static ListingsSolrModelActivator getInstance() {
		final ListingsSolrModelActivator modelActivator = instance.get();
		if (null == modelActivator) {
			throw new IllegalStateException("inactive");
		}
		return modelActivator;
	}

	/**
	 * Creates a new instance.
	 */
	public ListingsSolrModelActivator() {
		super(SYMBOLIC_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.common.runtime.BaseBundleActivator#doStart(org.osgi.framework.BundleContext)
	 */
	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance.set(this);
		getServiceHelper().registerService(RuntimeContextObjectProvider.class.getName(), new SolrListingsModelProvider(), "Eclipse Gyrex", "Gyrex Solr based listing model provider", null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.common.runtime.BaseBundleActivator#doStop(org.osgi.framework.BundleContext)
	 */
	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance.set(null);
	}
}
