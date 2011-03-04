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
package org.eclipse.gyrex.search.solr.documents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.exceptions.ResourceFailureException;
import org.eclipse.gyrex.search.documents.IDocument;
import org.eclipse.gyrex.search.documents.IDocumentManager;
import org.eclipse.gyrex.search.internal.solr.documents.PublishJob;
import org.eclipse.gyrex.search.internal.solr.documents.SolrDocumentManagerMetrics;
import org.eclipse.gyrex.search.internal.solr.documents.StoredDocument;
import org.eclipse.gyrex.search.internal.solr.documents.TransientDocument;

import org.eclipse.osgi.util.NLS;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Base class for {@link IDocumentManager document managers} based on Apache
 * Solr.
 * <p>
 * This implementation uses a {@link SolrServerRepository Solr repository} for
 * accessing Solr. Any custom content type definitions need to be bound to such
 * a repository.
 * </p>
 * <p>
 * Clients that want to contribute a specialize document model backed by Solr
 * may subclass this class. This class implements {@link IDocumentManager} and
 * shields subclasses from API evolutions.
 * </p>
 */
public abstract class BaseSolrDocumentManager extends BaseModelManager<org.eclipse.gyrex.persistence.solr.SolrServerRepository> implements IDocumentManager {

	private final AtomicBoolean commitsAllowed = new AtomicBoolean(true);

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 *            the context
	 * @param repository
	 *            the repository
	 * @param metricsId
	 *            the metrics id
	 */
	protected BaseSolrDocumentManager(final IRuntimeContext context, final SolrServerRepository repository, final String metricsId) {
		super(context, repository, new SolrDocumentManagerMetrics(metricsId, context, repository));
	}

	/**
	 * Commits everything to the underlying Solr repository.
	 * 
	 * @param collection
	 *            the collection (maybe <code>null</code> for the default
	 *            collection)
	 * @param waitFlush
	 *            <code>true</code> if the method should block till all changes
	 *            have been committed, <code>false</code> otherwise
	 * @param waitSearcher
	 *            <code>true</code> if the method should block till new
	 *            searchers have been opened after committing,
	 *            <code>false</code> otherwise
	 */
	public final void commit(final boolean waitFlush, final boolean waitSearcher) {
		try {
			getRepository().getSolrServer().commit(waitFlush, waitSearcher);
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error committing in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public final IDocument createDocument() {
		return new TransientDocument();
	}

	/**
	 * Called by the platform when a model manager is no longer needed and all
	 * held resources should be released.
	 * <p>
	 * Subclasses which override <strong>must</strong> call super at appropriate
	 * times.
	 * </p>
	 */
	@Override
	protected void doClose() {
		// empty
	}

	@Override
	public final Map<String, IDocument> findById(final Collection<String> ids) {
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
	public final IDocument findById(final String id) {
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

	protected final String getRepositoryId() {
		return getRepository().getRepositoryId();
	}

	private final SolrDocumentManagerMetrics getSolrListingsManagerMetrics() {
		return (SolrDocumentManagerMetrics) getMetrics();
	}

	/**
	 * Optimizes and commits everything to the underlying Solr repository.
	 * 
	 * @param collection
	 *            the collection (maybe <code>null</code> for the default
	 *            collection)
	 * @param waitFlush
	 *            <code>true</code> if the method should block till all changes
	 *            have been committed, <code>false</code> otherwise
	 * @param waitSearcher
	 *            <code>true</code> if the method should block till new
	 *            searchers have been opened after committing,
	 *            <code>false</code> otherwise
	 */
	public final void optimize(final boolean waitFlush, final boolean waitSearcher) {
		try {
			getRepository().getSolrServer().optimize(waitFlush, waitSearcher);
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error optimizing repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public final void publish(final Collection<IDocument> documents) {
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
		new PublishJob(docsToPublish, getRepository().getSolrServer(), getSolrListingsManagerMetrics(), commitsAllowed.get()).schedule();
	}

	/**
	 * Executes a SolrJ query.
	 * <p>
	 * Note, this API depends on the SolrJ and Solr API. Thus, it is bound to
	 * the evolution of external API which might not follow the Gyrex <a
	 * href="http://wiki.eclipse.org/Evolving_Java-based_APIs"
	 * target="_blank">API evolution</a> and <a
	 * href="http://wiki.eclipse.org/Version_Numbering"
	 * target="_blank">versioning</a> guidelines.
	 * </p>
	 * 
	 * @param query
	 *            the <code>SolrQuery</code> object
	 * @return the <code>QueryResponse</code> object
	 */
	public final QueryResponse query(final SolrQuery solrQuery) {
		final String query = solrQuery.toString();
		// TODO: limit should be configurable
		try {
			final int urlLengthLimit = 2000;
			if (query.length() > urlLengthLimit) {
				return getRepository().getSolrServerOptimizedForQuery().query(solrQuery, SolrRequest.METHOD.POST);
			} else {
				return getRepository().getSolrServerOptimizedForQuery().query(solrQuery, SolrRequest.METHOD.GET);
			}
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error querying documents in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public final void remove(final Collection<String> documentIds) {
		try {
			getRepository().getSolrServer().deleteById(documentIds instanceof List ? (List<String>) documentIds : new ArrayList<String>(documentIds));
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error removing documents in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	/**
	 * Allows to temporarily disabled commits from the manager.
	 * <p>
	 * When disabled, the manager will never commit any changes
	 * {@link IDocumentManager#publish(Iterable) submitted} to the underlying
	 * Solr repository. Instead, {@link #commit(boolean, boolean)} must be
	 * called manually in order to apply changes to the Solr repository.
	 * </p>
	 * 
	 * @param collection
	 *            the collection (maybe <code>null</code> for the default
	 *            collection)
	 * @param enabled
	 *            <code>true</code> if the manager is allowed to commit changes,
	 *            <code>false</code> otherwise
	 * @return <code>true</code> if commit was previously enabled,
	 *         <code>false</code> otherwise
	 */
	public final boolean setCommitsEnabled(final boolean enabled) {
		return commitsAllowed.getAndSet(enabled);
	}

}
