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
package org.eclipse.gyrex.search.internal.solr.facets;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManagerMetrics;
import org.eclipse.gyrex.monitoring.metrics.MetricSet;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.search.solr.facets.BaseSolrFacetManager;

/**
 * {@link MetricSet} for {@link BaseFacetManager}
 */
public class FacetManagerMetrics extends BaseModelManagerMetrics {

	/**
	 * Creates a new instance.
	 * 
	 * @param metricsId
	 * @param context
	 * @param repository
	 */
	public FacetManagerMetrics(final String id, final IRuntimeContext context, final SolrServerRepository repository) {
		super(id, BaseSolrFacetManager.class, context, repository, new ThroughputMetric(id + ".facets.write"), new ThroughputMetric(id + ".facets.read"));
	}

}
