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

import org.eclipse.gyrex.cds.IContentDeliveryService;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.services.common.provider.BaseService;
import org.eclipse.gyrex.services.common.provider.ServiceProvider;
import org.eclipse.gyrex.services.common.status.IStatusMonitor;

public class SolrListingsServiceProvider extends ServiceProvider {

	/**
	 * Creates a new instance.
	 */
	public SolrListingsServiceProvider() {
		super(IContentDeliveryService.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.services.common.provider.ServiceProvider#createServiceInstance(java.lang.Class, org.eclipse.gyrex.context.IRuntimeContext, org.eclipse.gyrex.services.common.status.IStatusMonitor)
	 */
	@Override
	public BaseService createServiceInstance(final Class serviceType, final IRuntimeContext context, final IStatusMonitor statusMonitor) {
		if (IContentDeliveryService.class.equals(serviceType)) {
			return new SolrListingService(context, statusMonitor);
		}
		return null;
	}

}
