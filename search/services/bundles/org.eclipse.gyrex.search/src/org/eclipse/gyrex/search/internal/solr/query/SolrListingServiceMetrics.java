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

import org.eclipse.gyrex.monitoring.metrics.MetricSet;
import org.eclipse.gyrex.monitoring.metrics.StatusMetric;

/**
 * 
 */
public class SolrListingServiceMetrics extends MetricSet {

	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 * @param metrics
	 */
	protected SolrListingServiceMetrics(final String id) {
		super(id, new StatusMetric(id.concat(".status"), "ok", "created"));
	}

}
