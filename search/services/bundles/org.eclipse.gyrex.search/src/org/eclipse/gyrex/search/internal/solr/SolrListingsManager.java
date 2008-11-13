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
package org.eclipse.cloudfree.listings.model.solr.internal;

import java.util.UUID;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.listings.model.IListing;
import org.eclipse.cloudfree.listings.model.IListingManager;
import org.eclipse.cloudfree.listings.model.documents.Document;
import org.eclipse.cloudfree.listings.model.solr.ISolrQueryExecutor;
import org.eclipse.cloudfree.model.common.provider.BaseModelManager;
import org.eclipse.cloudfree.monitoring.metrics.ThroughputMetric;
import org.eclipse.cloudfree.persistence.solr.internal.SolrRepository;

/**
 * {@link IListingManager} implementation based on Apache Solr.
 */
public class SolrListingsManager extends BaseModelManager<SolrRepository> implements IListingManager {

	private static String createMetricsId(final IContext context, final SolrRepository repository) {
		return "org.eclipse.cloudfree.listings.model.solr.manager[" + context.getContextPath().toString() + "," + repository.getRepositoryId() + "].metrics";
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 *            the context
	 * @param repository
	 *            the repository
	 */
	protected SolrListingsManager(final IContext context, final SolrRepository repository) {
		super(context, repository, new SolrListingsManagerMetrics(createMetricsId(context, repository)));
	}

	@Override
	public IListing findById(final String id) {
		// collect stats
		final ThroughputMetric retrievedByIdMetric = getSolrListingsManagerMetrics().getDocsRetrievedByIdMetric();
		final long requestStarted = retrievedByIdMetric.requestStarted();

		// build query
		final SolrQuery query = new SolrQuery();
		query.setQuery(Document.ID + ":" + ClientUtils.escapeQueryChars(id));
		query.setStart(0).setRows(1);
		query.setFields("*");

		// execute
		final QueryResponse response = getRepository().query(query);
		final SolrDocumentList results = response.getResults();
		if (!results.isEmpty()) {
			// got result
			try {
				return new SolrListing(results.iterator().next());
			} finally {
				retrievedByIdMetric.requestFinished(1, System.currentTimeMillis() - requestStarted);
			}
		}

		// nothing found
		retrievedByIdMetric.requestFailed();
		return null;
	}

	@Override
	public final Object getAdapter(final Class adapter) {
		if (ISolrQueryExecutor.class.equals(adapter)) {
			return new SolrQueryExecutor(getRepository());
		}
		return super.getAdapter(adapter);
	}

	private SolrListingsManagerMetrics getSolrListingsManagerMetrics() {
		return (SolrListingsManagerMetrics) getMetrics();
	}

	//	public IListing findByFieldValue(final String path) {
	//		try {
	//			final SolrQuery query = new SolrQuery();
	//			query.setStart(0).setRows(1);
	//			query.setQuery(Document.URI_PATH + ":" + path);
	//			final QueryResponse response = getRepository().getSolrServer().query(query);
	//			final SolrDocumentList results = response.getResults();
	//			if (!results.isEmpty()) {
	//				return new SolrListing(results.iterator().next());
	//			}
	//		} catch (final SolrServerException e) {
	//			throw new RuntimeException(e);
	//		}
	//		return null;
	//	}

	@Override
	public void publish(final Iterable<Document> documents) {
		// assign ids
		for (final Document document : documents) {
			if (null == document.getId()) {
				document.setId(UUID.randomUUID().toString());
			}
		}

		// publish
		new PublishJob(documents, getRepository(), getSolrListingsManagerMetrics()).schedule();
	}

}
