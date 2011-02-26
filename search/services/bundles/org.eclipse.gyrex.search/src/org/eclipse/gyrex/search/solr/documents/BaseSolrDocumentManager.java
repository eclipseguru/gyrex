/*******************************************************************************
 * Copyright (c) 2008, 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *******************************************************************************/
package org.eclipse.gyrex.cds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentManager;
import org.eclipse.gyrex.cds.internal.solr.documents.PublishJob;
import org.eclipse.gyrex.cds.internal.solr.documents.SolrDocumentManagerMetrics;
import org.eclipse.gyrex.cds.internal.solr.documents.StoredDocument;
import org.eclipse.gyrex.cds.internal.solr.documents.TransientDocument;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.model.common.provider.ModelProvider;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.exceptions.ResourceFailureException;

import org.eclipse.osgi.util.NLS;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * {@link IDocumentManager} base implementation based on Apache Solr.
 * <p>
 * Usually there's no need to override any method in an implementation. This
 * already contains each required functionality to access solr repositories.
 * <p>
 * The cause to make it abstract lays in the concept of {@link ModelProvider}
 * and {@link IRuntimeContext}. For each repository access we have to ask the
 * context for a {@link BaseModelManager} implementation which is registered as
 * unique class via model provider.
 * <p>
 * So each solr repository needs an own implementation of a model manager
 */
public abstract class BaseSolrDocumentManager extends BaseModelManager<org.eclipse.gyrex.persistence.solr.SolrServerRepository> implements IDocumentManager {

	private final AtomicBoolean commitsAllowed = new AtomicBoolean(true);
	private final SolrServer writeServer;
	private final SolrServer queryServer;

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 *            the context
	 * @param repository
	 *            the repository
	 * @param modelManagerImplementaionId
	 *            must be unique for each context. See
	 *            {@link BaseModelManager#createMetricsId(String, IRuntimeContext, org.eclipse.gyrex.persistence.storage.Repository)}
	 */
	protected BaseSolrDocumentManager(final IRuntimeContext context, final SolrServerRepository repository, final String modelManagerImplementationId) {
		super(context, repository, new SolrDocumentManagerMetrics(createMetricsId(modelManagerImplementationId, context, repository), createMetricsDescription("Solr based document manager", context, repository)));

		writeServer = getRepository().getSolrServer();
		queryServer = getRepository().getSolrServerOptimizedForQuery();

		if ((null == writeServer) || (null == queryServer)) {
			throw new IllegalStateException("Solr servers to write and query must not be null.");
		}
	}

	@Override
	public void commit(final boolean waitFlush, final boolean waitSearcher) {
		try {
			writeServer.commit(waitFlush, waitSearcher);
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error committing to collection in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public IDocument createDocument() {
		return new TransientDocument();
	}

	@Override
	public Map<String, IDocument> findById(final Iterable<String> ids) {
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
			queryStr.append(IDocument.ATTRIBUTE_ID).append(":(");
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
			final QueryResponse response = query(query);
			final SolrDocumentList results = response.getResults();

			// check for result
			if (!results.isEmpty()) {
				final Map<String, IDocument> map = new HashMap<String, IDocument>(results.size());
				for (final Iterator<SolrDocument> stream = results.iterator(); stream.hasNext();) {
					final StoredDocument doc = new StoredDocument(stream.next());
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
	public IDocument findById(final String id) {
		if (null == id) {
			throw new IllegalArgumentException("id must not be null");
		}

		// collect stats
		final ThroughputMetric retrievedByIdMetric = getSolrListingsManagerMetrics().getDocsRetrievedByIdMetric();
		final long requestStarted = retrievedByIdMetric.requestStarted();
		try {
			// build query
			final SolrQuery query = new SolrQuery();
			query.setQuery(IDocument.ATTRIBUTE_ID + ":" + ClientUtils.escapeQueryChars(id));
			query.setStart(0).setRows(1);
			query.setFields("*");

			// query
			final QueryResponse response = query(query);
			final SolrDocumentList results = response.getResults();

			// check for result
			if (!results.isEmpty()) {
				retrievedByIdMetric.requestFinished(1, System.currentTimeMillis() - requestStarted);
				return new StoredDocument(results.iterator().next());
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

	public String getRepositoryId() {
		return getRepository().getRepositoryId();
	}

	private SolrDocumentManagerMetrics getSolrListingsManagerMetrics() {
		return (SolrDocumentManagerMetrics) getMetrics();
	}

	@Override
	public void optimize(final boolean waitFlush, final boolean waitSearcher) {
		try {
			writeServer.optimize(waitFlush, waitSearcher);
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error optimizing collection in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public void publish(final Iterable<IDocument> documents) {
		// create a copy of the list to avoid clearing the list by outsiders
		final List<IDocument> docsToPublish = new ArrayList<IDocument>();

		// assign ids and copy docs into the list
		for (final IDocument document : documents) {
			if (null == document.getId()) {
				document.setId(UUID.randomUUID().toString());
			}
			docsToPublish.add(document);
		}

		// publish
		new PublishJob(docsToPublish, writeServer, getSolrListingsManagerMetrics(), commitsAllowed.get()).schedule();
	}

	@Override
	public QueryResponse query(final SolrQuery solrQuery) {
		final String query = solrQuery.toString();
		// TODO: limit should be configurable
		try {
			final int urlLengthLimit = 2000;
			if (query.length() > urlLengthLimit) {
				return queryServer.query(solrQuery, SolrRequest.METHOD.POST);
			} else {
				return queryServer.query(solrQuery, SolrRequest.METHOD.GET);
			}
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error querying collection in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.documents.IDocumentCollection#remove(java.lang.Iterable)
	 */
	@Override
	public void remove(final Iterable<String> documentIds) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setCommitsEnabled(final boolean enabled) {
		return commitsAllowed.getAndSet(enabled);
	}

}
