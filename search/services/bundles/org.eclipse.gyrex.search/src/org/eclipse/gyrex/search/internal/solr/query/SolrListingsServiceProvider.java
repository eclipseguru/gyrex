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
package org.eclipse.cloudfree.listings.service.solr.internal;


import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.services.common.provider.BaseService;
import org.eclipse.cloudfree.services.common.provider.ServiceProvider;
import org.eclipse.cloudfree.services.common.status.IStatusMonitor;
import org.eclipse.cloudfree.services.listings.IListingService;

public class SolrListingsServiceProvider extends ServiceProvider {

	/**
	 * Creates a new instance.
	 */
	SolrListingsServiceProvider() {
		super(IListingService.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.services.common.provider.ServiceProvider#createServiceInstance(java.lang.Class, org.eclipse.cloudfree.common.context.IContext, org.eclipse.cloudfree.services.common.status.IStatusMonitor)
	 */
	@Override
	public BaseService createServiceInstance(final Class serviceType, final IContext context, final IStatusMonitor statusMonitor) {
		if (IListingService.class.equals(serviceType)) {
			return new SolrListingService(context, statusMonitor);
		}
		return null;
	}

}
