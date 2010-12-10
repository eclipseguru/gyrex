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
package org.eclipse.gyrex.cds.solr.internal.documents;

import org.eclipse.gyrex.monitoring.metrics.MetricSet;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;

public class SolrDocumentManagerMetrics extends MetricSet {

	private final ThroughputMetric docsPublishedMetric;
	private final ThroughputMetric docsRetrievedByIdMetric;

	public SolrDocumentManagerMetrics(final String id, final String description) {
		super(id, description, new ThroughputMetric(id + ".docs.published"), new ThroughputMetric(id + ".docs.retrieved.byId"));
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
