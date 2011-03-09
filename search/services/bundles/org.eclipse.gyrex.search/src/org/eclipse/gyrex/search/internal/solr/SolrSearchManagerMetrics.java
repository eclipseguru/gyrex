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
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 *******************************************************************************/
package org.eclipse.gyrex.search.internal.solr;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManagerMetrics;
import org.eclipse.gyrex.monitoring.metrics.StatusMetric;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.search.solr.BaseSolrSearchManager;

public class SolrSearchManagerMetrics extends BaseModelManagerMetrics {

	private final ThroughputMetric docsPublishedMetric;
	private final ThroughputMetric docsRetrievedByIdMetric;
	private final ThroughputMetric writeFacetMetric;
	private final ThroughputMetric readFacetMetric;
	private final ThroughputMetric queryMetric;

	private final StatusMetric statusMetric;

	public SolrSearchManagerMetrics(final String id, final IRuntimeContext context, final SolrServerRepository repository) {
		super(id, BaseSolrSearchManager.class, context, repository, new ThroughputMetric(id + ".search.docs.published"), new ThroughputMetric(id + ".search.docs.retrieved.byId"), new StatusMetric(id.concat(".search.status"), "ok", "created"), new ThroughputMetric(id + ".search.facets.write"), new ThroughputMetric(id + ".search.facets.read"), new ThroughputMetric(id + ".search.query"));
		docsPublishedMetric = getMetric(0, ThroughputMetric.class);
		docsRetrievedByIdMetric = getMetric(1, ThroughputMetric.class);
		statusMetric = getMetric(2, StatusMetric.class);
		writeFacetMetric = getMetric(3, ThroughputMetric.class);
		readFacetMetric = getMetric(4, ThroughputMetric.class);
		queryMetric = getMetric(5, ThroughputMetric.class);
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

	/**
	 * Returns the queryMetric.
	 * 
	 * @return the queryMetric
	 */
	public ThroughputMetric getQueryMetric() {
		return queryMetric;
	}

	/**
	 * Returns the readFacetMetric.
	 * 
	 * @return the readFacetMetric
	 */
	public ThroughputMetric getReadFacetMetric() {
		return readFacetMetric;
	}

	/**
	 * Returns the statusMetric.
	 * 
	 * @return the statusMetric
	 */
	public StatusMetric getStatusMetric() {
		return statusMetric;
	}

	/**
	 * Returns the writeFacetMetric.
	 * 
	 * @return the writeFacetMetric
	 */
	public ThroughputMetric getWriteFacetMetric() {
		return writeFacetMetric;
	}

}
