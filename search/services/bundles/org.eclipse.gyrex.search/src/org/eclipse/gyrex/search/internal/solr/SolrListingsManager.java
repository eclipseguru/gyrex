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
package org.eclipse.gyrex.cds.model.solr.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.gyrex.cds.model.IListing;
import org.eclipse.gyrex.cds.model.IListingManager;
import org.eclipse.gyrex.cds.model.documents.Document;
import org.eclipse.gyrex.cds.model.solr.ISolrListingManager;
import org.eclipse.gyrex.cds.model.solr.ISolrQueryExecutor;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.internal.SolrRepository;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * {@link IListingManager} implementation based on Apache Solr.
 */
public class SolrListingsManager extends BaseModelManager<SolrRepository> implements IListingManager, ISolrListingManager {

	private static String createMetricsId(final IRuntimeContext context, final SolrRepository repository) {
		return "org.eclipse.gyrex.cds.model.solr.manager[" + context.getContextPath().toString() + "," + repository.getRepositoryId() + "].metrics";
	}

	private final AtomicBoolean commitsAllowed = new AtomicBoolean(true);

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 *            the context
	 * @param repository
	 *            the repository
	 */
	protected SolrListingsManager(final IRuntimeContext context, final SolrRepository repository) {
		super(context, repository, new SolrListingsManagerMetrics(createMetricsId(context, repository)));
	}

	@Override
	public void commit(final boolean waitFlush, final boolean waitSearcher) {
		getRepository().commit(waitFlush, waitSearcher);
	}

	@Override
	public Map<String, IListing> findById(final Iterable<String> ids) {
		if (null == ids) {
			throw new IllegalArgumentException("ids must not be null");
		}

		// collect stats
		final ThroughputMetric retrievedByIdMetric = getSolrListingsManagerMetrics().getDocsRetrievedByIdMetric();
		final long requestStarted = retrievedByIdMetric.requestStarted();
		try {
			// build query
			final SolrQuery query = new SolrQuery();
			final StringBuilder queryStr = new StringBuilder();
			int length = 0;
			queryStr.append(Document.ID).append(":(");
			for (final String id : ids) {
				if (StringUtils.isBlank(id)) {
					throw new IllegalArgumentException("unsupport blank id found in ids list");
				}
				if (length > 0) {
					queryStr.append(" OR ");
				}
				queryStr.append(ClientUtils.escapeQueryChars(id));
				length++;
			}
			if (length == 0) {
				throw new IllegalArgumentException("ids list is empty");
			}
			query.setQuery(queryStr.append(')').toString());
			query.setStart(0).setRows(length);
			query.setFields("*");

			// execute
			final QueryResponse response = getRepository().query(query);
			final SolrDocumentList results = response.getResults();

			// check for result
			if (!results.isEmpty()) {
				final Map<String, IListing> map = new HashMap<String, IListing>(results.size());
				for (final Iterator<SolrDocument> stream = results.iterator(); stream.hasNext();) {
					final SolrListing doc = new SolrListing(stream.next());
					map.put(doc.getId(), doc);
				}
				retrievedByIdMetric.requestFinished(length, System.currentTimeMillis() - requestStarted);
				return Collections.unmodifiableMap(map);
			}

			// nothing found
			retrievedByIdMetric.requestFinished(length, System.currentTimeMillis() - requestStarted);
			return Collections.emptyMap();
		} catch (final RuntimeException e) {
			retrievedByIdMetric.requestFailed();
			throw e;
		} catch (final Error e) {
			retrievedByIdMetric.requestFailed();
			throw e;
		}
	}

	@Override
	public IListing findById(final String id) {
		if (null == id) {
			throw new IllegalArgumentException("id must not be null");
		}

		// collect stats
		final ThroughputMetric retrievedByIdMetric = getSolrListingsManagerMetrics().getDocsRetrievedByIdMetric();
		final long requestStarted = retrievedByIdMetric.requestStarted();
		try {
			// build query
			final SolrQuery query = new SolrQuery();
			query.setQuery(Document.ID + ":" + ClientUtils.escapeQueryChars(id));
			query.setStart(0).setRows(1);
			query.setFields("*");

			// query
			final QueryResponse response = getRepository().query(query);
			final SolrDocumentList results = response.getResults();

			// check for result
			if (!results.isEmpty()) {
				retrievedByIdMetric.requestFinished(1, System.currentTimeMillis() - requestStarted);
				return new SolrListing(results.iterator().next());
			}

			// nothing found
			retrievedByIdMetric.requestFinished(1, System.currentTimeMillis() - requestStarted);
			return null;
		} catch (final RuntimeException e) {
			retrievedByIdMetric.requestFailed();
			throw e;
		} catch (final Error e) {
			retrievedByIdMetric.requestFailed();
			throw e;
		}
	}

	@Override
	public final Object getAdapter(final Class adapter) {
		if (ISolrListingManager.class.equals(adapter)) {
			return this;
		}
		if (ISolrQueryExecutor.class.equals(adapter)) {
			return new SolrQueryExecutor(getRepository());
		}
		if (SolrRepository.class.equals(adapter)) {
			return getRepository();
		}
		return super.getAdapter(adapter);
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

	private SolrListingsManagerMetrics getSolrListingsManagerMetrics() {
		return (SolrListingsManagerMetrics) getMetrics();
	}

	@Override
	public void optimize(final boolean waitFlush, final boolean waitSearcher) {
		getRepository().optimize(waitFlush, waitSearcher);
	}

	@Override
	public void publish(final Iterable<Document> documents) {
		// create a copy of the list to avoid clearing the list by outsiders
		final List<Document> docsToPublish = new ArrayList<Document>();

		// assign ids and copy docs into the list
		for (final Document document : documents) {
			if (null == document.getId()) {
				document.setId(UUID.randomUUID().toString());
			}
			docsToPublish.add(document);
		}

		// publish
		new PublishJob(docsToPublish, getRepository(), getSolrListingsManagerMetrics(), commitsAllowed.get()).schedule();
	}

	@Override
	public boolean setCommitsEnabled(final boolean enabled) {
		return commitsAllowed.getAndSet(enabled);
	}

}
