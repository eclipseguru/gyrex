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
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 *******************************************************************************/
package org.eclipse.gyrex.search.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.RepositoryMetadata;
import org.eclipse.gyrex.persistence.storage.exceptions.ResourceFailureException;
import org.eclipse.gyrex.search.ISearchManager;
import org.eclipse.gyrex.search.documents.IDocument;
import org.eclipse.gyrex.search.documents.IDocumentAttribute;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.internal.SearchActivator;
import org.eclipse.gyrex.search.internal.SearchDebug;
import org.eclipse.gyrex.search.internal.solr.SolrSearchManagerMetrics;
import org.eclipse.gyrex.search.internal.solr.documents.StoredDocument;
import org.eclipse.gyrex.search.internal.solr.documents.TransientDocument;
import org.eclipse.gyrex.search.internal.solr.facets.Facet;
import org.eclipse.gyrex.search.internal.solr.query.AttributeFilter;
import org.eclipse.gyrex.search.internal.solr.query.FacetFilter;
import org.eclipse.gyrex.search.internal.solr.query.QueryImpl;
import org.eclipse.gyrex.search.internal.solr.result.ResultImpl;
import org.eclipse.gyrex.search.query.FacetSelectionStrategy;
import org.eclipse.gyrex.search.query.IAttributeFilter;
import org.eclipse.gyrex.search.query.IFacetFilter;
import org.eclipse.gyrex.search.query.IQuery;
import org.eclipse.gyrex.search.query.SortDirection;
import org.eclipse.gyrex.search.result.IResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link ISearchManager search managers} based on Apache Solr.
 * <p>
 * This implementation uses a {@link SolrServerRepository Solr repository} for
 * accessing Solr. Any custom content type definitions need to be bound to such
 * a repository.
 * </p>
 * <p>
 * Clients that want to contribute a specialize document model backed by Solr
 * may subclass this class. This class implements {@link ISearchManager} and
 * shields subclasses from API evolutions.
 * </p>
 */
public abstract class BaseSolrSearchManager extends BaseModelManager<org.eclipse.gyrex.persistence.solr.SolrServerRepository> implements ISearchManager {

	private static final Logger LOG = LoggerFactory.getLogger(BaseSolrSearchManager.class);

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
	protected BaseSolrSearchManager(final IRuntimeContext context, final SolrServerRepository repository, final String metricsId) {
		super(context, repository, new SolrSearchManagerMetrics(metricsId, context, repository));
	}

	private void checkFacet(final IFacet facet) {
		if (facet == null) {
			throw new IllegalArgumentException("facet must not be null");
		}
		if (!(facet instanceof Facet)) {
			throw new IllegalArgumentException(NLS.bind("facet type {0} not supported by this manager", facet.getClass()));
		}
	}

	/**
	 * Commits everything to the underlying Solr repository.
	 * 
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

	@Override
	public final IFacet createFacet(final String attributeId) throws IllegalArgumentException {
		final ThroughputMetric writeFacetMetric = getSolrSearchManagerMetrics().getWriteFacetMetric();
		final long start = writeFacetMetric.requestStarted();
		final Facet facet = new Facet(attributeId, this);
		writeFacetMetric.requestFinished(1, System.currentTimeMillis() - start);
		return facet;
	}

	@Override
	public final IQuery createQuery() {
		return new QueryImpl();
	}

	private SolrInputDocument createSolrDoc(final IDocument document) {
		final SolrInputDocument solrDoc = new SolrInputDocument();
		final Collection<IDocumentAttribute<?>> attributes = document.getAttributes().values();
		for (final IDocumentAttribute<?> attr : attributes) {
			final Collection<?> values = attr.getValues();
			for (final Object value : values) {
				solrDoc.addField(attr.getId(), value);
			}
		}
		return solrDoc;
	}

	/**
	 * Creates a {@link SolrQuery} based on the specified {@link IQuery query}.
	 * <p>
	 * Subclasses may override if the default implementation is not sufficient.
	 * </p>
	 * 
	 * @param query
	 *            the query
	 * @return a {@link SolrQuery}
	 */
	protected SolrQuery createSolrQuery(final IQuery query) {
		if (!(query instanceof QueryImpl)) {
			throw new IllegalArgumentException("invalid query object; not created by this manager");
		}
		final QueryImpl queryImpl = (QueryImpl) query;
		final SolrQuery solrQuery = new SolrQuery();

		// advanced or user query
		// TODO we need to better understand query types and user requirements
		if (null != query.getAdvancedQuery()) {
//			solrQuery.setQueryType("standard");
			solrQuery.setQuery(query.getAdvancedQuery());
		} else if (null != query.getQuery()) {
//			solrQuery.setQueryType("edismax");
			solrQuery.setQuery(query.getQuery());
		} else {
			solrQuery.setQuery("*:*");
		}

		// paging
		solrQuery.setStart(new Integer((int) query.getStartIndex()));
		solrQuery.setRows(new Integer(query.getMaxResults()));

		// filters
		for (final String filterQuery : query.getFilterQueries()) {
			solrQuery.addFilterQuery(filterQuery);
		}

		// attribute filters
		for (final IAttributeFilter attributeFilter : query.getAttributeFilters()) {
			solrQuery.addFilterQuery(((AttributeFilter) attributeFilter).toFilterQuery());
		}

		// sorting
		final Map<String, SortDirection> sortFields = query.getSortFields();
		for (final Entry<String, SortDirection> sortEntry : sortFields.entrySet()) {
			switch (sortEntry.getValue()) {
				case DESCENDING:
					solrQuery.addSortField(sortEntry.getKey(), ORDER.desc);
					break;
				case ASCENDING:
				default:
					solrQuery.addSortField(sortEntry.getKey(), ORDER.asc);
					break;
			}
		}

		// facets
		final Map<String, IFacet> facets = getFacets();
		if (facets != null) {
			// remember facets
			queryImpl.setFacetsInUse(facets);

			// enable facetting
			for (final IFacet facet : facets.values()) {
				if (!facet.isEnabled()) {
					continue;
				}
				final String facetField = facet.getAttributeId();
				final FacetSelectionStrategy selectionStrategy = facet.getSelectionStrategy();
				if ((null != selectionStrategy) && (selectionStrategy == FacetSelectionStrategy.MULTI)) {
					solrQuery.addFacetField("{!ex=" + facet.getAttributeId() + "}" + facetField);
				} else {
					solrQuery.addFacetField(facetField);
				}
			}

			// facet filters
			for (final IFacetFilter facetFilter : query.getFacetFilters()) {
				if (!facetFilter.getFacet().isEnabled()) {
					continue;
				}
				solrQuery.addFilterQuery(((FacetFilter) facetFilter).toFilterQuery());
			}

		} else {
			solrQuery.setFacet(false);
		}

		// dimension
		switch (query.getResultProjection()) {
			case FULL:
				solrQuery.setFields("*");
				break;

			case COMPACT:
			default:
				// TODO this should be configurable per context/repository
				// for now, default to what is defined in solrconfig.xml
				solrQuery.setFields((String[]) null);
				break;
		}

		// debugging
		if (SearchDebug.debug) {
			solrQuery.setShowDebugInfo(true);
		}

		// additional query options
		for (final Entry<String, String> entry : query.getQueryOptions().entrySet()) {
			// note, we use #set (instead of add) because any option may override any
			// of the previously configured things
			solrQuery.setParam(entry.getKey(), entry.getValue());
		}

		return solrQuery;
	}

	@Override
	public final void deleteFacet(final IFacet facet) throws IllegalArgumentException, ModelException {
		checkFacet(facet);
		try {
			final RepositoryMetadata facetsMetadata = getFacetsMetadata();
			facetsMetadata.sync();
			facetsMetadata.remove(facet.getAttributeId());
			facetsMetadata.flush();
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SearchActivator.SYMBOLIC_NAME, "Unable to remove facet. " + e.getMessage(), e));
		}
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
		getSolrSearchManagerMetrics().getStatusMetric().setStatus("closed", "manager closed");
	}

	@Override
	public IResult findByQuery(final IQuery query) {
		if ((query == null) || !(query instanceof QueryImpl)) {
			throw new IllegalArgumentException("Invalid query. Must be created using #createQuery from this service instance.");
		}

		final SolrQuery solrQuery = createSolrQuery(query);
		final QueryResponse response = query(solrQuery);
		return new ResultImpl(getContext(), (QueryImpl) query, response);
	}

	@Override
	public final IDocument findDocumentById(final String id) {
		if (null == id) {
			throw new IllegalArgumentException("id must not be null");
		}

		// collect stats
		final ThroughputMetric retrievedByIdMetric = getSolrSearchManagerMetrics().getDocsRetrievedByIdMetric();
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

	@Override
	public final Map<String, IDocument> findDocumentsById(final Collection<String> ids) {
		if (null == ids) {
			throw new IllegalArgumentException("ids must not be null");
		}

		// collect stats
		final ThroughputMetric retrievedByIdMetric = getSolrSearchManagerMetrics().getDocsRetrievedByIdMetric();
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
	public final Map<String, IFacet> getFacets() throws ModelException {
		final ThroughputMetric readFacetMetric = getSolrSearchManagerMetrics().getReadFacetMetric();
		try {
			final long start = readFacetMetric.requestStarted();
			final RepositoryMetadata facetsMetadata = getFacetsMetadata();
			final Collection<String> keys = facetsMetadata.getKeys();
			final Map<String, IFacet> map = new HashMap<String, IFacet>(keys.size());
			for (final String key : keys) {
				final byte[] bytes = facetsMetadata.get(key);
				if (bytes != null) {
					map.put(key, new Facet(key, this, bytes));
				}
			}
			readFacetMetric.requestFinished(map.size(), System.currentTimeMillis() - start);
			return Collections.unmodifiableMap(map);
		} catch (final BackingStoreException e) {
			readFacetMetric.requestFailed();
			throw new ModelException(new Status(IStatus.ERROR, SearchActivator.SYMBOLIC_NAME, "Unable to load facets. " + e.getMessage(), e));
		}
	}

	private RepositoryMetadata getFacetsMetadata() {
		return getRepository().getMetadata("facets");
	}

	protected final String getRepositoryId() {
		return getRepository().getRepositoryId();
	}

	private final SolrSearchManagerMetrics getSolrSearchManagerMetrics() {
		return (SolrSearchManagerMetrics) getMetrics();
	}

	/**
	 * Optimizes and commits everything to the underlying Solr repository.
	 * 
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
	public final void publishDocuments(final Collection<IDocument> documents) {
//		// create a copy of the list to avoid clearing the list by outsiders
//		final List<IDocument> docsToPublish = new ArrayList<IDocument>();
//
//		// assign ids and copy docs into the list
//		for (final IDocument document : documents) {
//			if (null == document.getId()) {
//				document.setId(UUID.randomUUID().toString());
//			}
//			docsToPublish.add(document);
//		}
//
//		// publish
//		new PublishJob(docsToPublish, getRepository().getSolrServer(), getSolrSearchManagerMetrics(), commitsAllowed.get()).schedule();

		// collect stats
		final ThroughputMetric publishedMetric = getSolrSearchManagerMetrics().getDocsPublishedMetric();
		final long requestStarted = publishedMetric.requestStarted();

		// create solr docs
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		for (final IDocument document : documents) {
			// generate id
			if (null == document.getId()) {
				document.setId(UUID.randomUUID().toString());
			}
			// convert to solr doc
			docs.add(createSolrDoc(document));
		}
		try {
			// add to repository
			final SolrServer solrServer = getRepository().getSolrServer();
			if (commitsAllowed.get()) {
//				final UpdateRequest req = new UpdateRequest();
//				req.add(docs);
//				req.setCommitWithin((int) TimeUnit.MINUTES.toMillis(3)); // TODO: should be configurable
//				req.process(solrServer);
				solrServer.add(docs);
				solrServer.commit();
			} else {
				solrServer.add(docs);
			}
			publishedMetric.requestFinished(docs.size(), System.currentTimeMillis() - requestStarted);
		} catch (final Exception e) {
			publishedMetric.requestFailed();
			throw new ResourceFailureException(String.format("Error publishing documents. %s", ExceptionUtils.getRootCauseMessage(e)), e);
		}
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
	 * @param solrQuery
	 *            the <code>SolrQuery</code> object
	 * @return the <code>QueryResponse</code> object
	 */
	public final QueryResponse query(final SolrQuery solrQuery) {
		final ThroughputMetric queryMetric = getSolrSearchManagerMetrics().getQueryMetric();
		final String query = solrQuery.toString();
		try {
			final SolrServer server = getRepository().getSolrServerOptimizedForQuery();
			if (SearchDebug.searchRequests) {
				LOG.debug("[SEARCH] {} (using {})", solrQuery, server);
			}

			final long started = queryMetric.requestStarted();
			final int urlLengthLimit = 2000; // TODO: limit should be configurable
			QueryResponse response;
			if (query.length() > urlLengthLimit) {
				response = server.query(solrQuery, SolrRequest.METHOD.POST);
			} else {
				response = server.query(solrQuery, SolrRequest.METHOD.GET);
			}
			queryMetric.requestFinished(1, System.currentTimeMillis() - started);
			return response;

		} catch (final Exception e) {
			queryMetric.requestFailed();
			throw new ResourceFailureException(NLS.bind("Error querying documents in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public final void removeDocuments(final Collection<String> documentIds) {
		try {
			getRepository().getSolrServer().deleteById(documentIds instanceof List ? (List<String>) documentIds : new ArrayList<String>(documentIds));
		} catch (final Exception e) {
			throw new ResourceFailureException(NLS.bind("Error removing documents in repository {0}. {1}", new Object[] { getRepositoryId(), e.getMessage() }), e);
		}
	}

	@Override
	public final void saveFacet(final IFacet facet) throws IllegalArgumentException, ModelException {
		final ThroughputMetric writeFacetMetric = getSolrSearchManagerMetrics().getWriteFacetMetric();
		final long start = writeFacetMetric.requestStarted();
		checkFacet(facet);
		try {
			final RepositoryMetadata facetsMetadata = getFacetsMetadata();
			facetsMetadata.sync();
			facetsMetadata.put(facet.getAttributeId(), ((Facet) facet).toByteArray());
			facetsMetadata.flush();
			writeFacetMetric.requestFinished(1, System.currentTimeMillis() - start);
		} catch (final BackingStoreException e) {
			writeFacetMetric.requestFailed();
			throw new ModelException(new Status(IStatus.ERROR, SearchActivator.SYMBOLIC_NAME, "Unable to save facet. " + e.getMessage(), e));
		}
	}

	/**
	 * Allows to temporarily disabled commits from the manager.
	 * <p>
	 * When disabled, the manager will never commit any changes
	 * {@link ISearchManager#publishDocuments(Collection) submitted} to the
	 * underlying Solr repository. Instead, {@link #commit(boolean, boolean)}
	 * must be called manually in order to apply changes to the Solr repository.
	 * </p>
	 * 
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
