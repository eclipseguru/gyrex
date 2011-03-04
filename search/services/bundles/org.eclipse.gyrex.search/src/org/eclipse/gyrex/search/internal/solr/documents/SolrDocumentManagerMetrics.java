/*******************************************************************************
 * Copyright (c) 2008, 2010 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.search.internal.solr.documents;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManagerMetrics;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.search.solr.documents.BaseSolrDocumentManager;

public class SolrDocumentManagerMetrics extends BaseModelManagerMetrics {

	private final ThroughputMetric docsPublishedMetric;
	private final ThroughputMetric docsRetrievedByIdMetric;

	public SolrDocumentManagerMetrics(final String id, final IRuntimeContext context, final SolrServerRepository repository) {
		super(id, BaseSolrDocumentManager.class, context, repository, new ThroughputMetric(id + ".docs.published"), new ThroughputMetric(id + ".docs.retrieved.byId"));
		docsPublishedMetric = getMetric(0, ThroughputMetric.class);
		docsRetrievedByIdMetric = getMetric(1, ThroughputMetric.class);
	}

	/**
	 * Returns the docsPublishedMetric.
	 * 
	 * @return the docsPublishedMetric
	 */
	public ThroughputMetric getDocsPublishedMetric() {
		return docsPublishedMetric;
	}

	/**
	 * Returns the docsRetrievedByIdMetric.
	 * 
	 * @return the docsRetrievedByIdMetric
	 */
	public ThroughputMetric getDocsRetrievedByIdMetric() {
		return docsRetrievedByIdMetric;
	}
}
