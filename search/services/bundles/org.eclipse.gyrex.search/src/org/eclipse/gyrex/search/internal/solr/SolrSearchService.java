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
package org.eclipse.gyrex.search.internal.solr;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.model.common.ModelUtil;
import org.eclipse.gyrex.search.ISearchService;
import org.eclipse.gyrex.search.documents.IDocumentManager;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.facets.IFacetManager;
import org.eclipse.gyrex.search.internal.SearchActivator;
import org.eclipse.gyrex.search.internal.SearchDebug;
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
import org.eclipse.gyrex.search.solr.documents.BaseSolrDocumentManager;
import org.eclipse.gyrex.search.solr.facets.BaseSolrFacetManager;
import org.eclipse.gyrex.services.common.provider.BaseService;
import org.eclipse.gyrex.services.common.status.IStatusMonitor;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solr based {@link ISearchService} implementation.
 */
public class SolrSearchService extends BaseService implements ISearchService {

	private static final Logger LOG = LoggerFactory.getLogger(SolrSearchService.class);

	private final AtomicLong facetsMapRefTime = new AtomicLong();
	private final AtomicReference<Map<String, IFacet>> facetsMapRef = new AtomicReference<Map<String, IFacet>>();

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param statusMonitor
	 * @param metrics
	 */
	SolrSearchService(final IRuntimeContext context, final IStatusMonitor statusMonitor) {
		super(context, statusMonitor, new SolrSearchServiceMetrics(createMetricsId(SearchActivator.SYMBOLIC_NAME + "solr.service", context), createMetricsDescription("Solr based CDS", context)));
	}

	@Override
	public IQuery createQuery() {
		return new QueryImpl();
	}

	SolrQuery createSolrQuery(final QueryImpl query, final IFacetManager facetManager) {
		final SolrQuery solrQuery = new SolrQuery();

		// advanced or user query
		if (null != query.getAdvancedQuery()) {
			solrQuery.setQueryType("standard");
			solrQuery.setQuery(query.getAdvancedQuery());
		} else {
			solrQuery.setQueryType("dismax");
			solrQuery.setQuery(query.getQuery());
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
		final Map<String, IFacet> facets = getFacets(facetManager);
		if (facets != null) {
			// remember facets
			query.setFacetsInUse(facets);

			// enable facetting
			for (final IFacet facet : facets.values()) {
				final String facetField = SolrSchemaConventions.facetFieldName(facet.getAttributeId());
				final FacetSelectionStrategy selectionStrategy = facet.getSelectionStrategy();
				if ((null != selectionStrategy) && (selectionStrategy == FacetSelectionStrategy.MULTI)) {
					solrQuery.addFacetField("{!ex=" + facet.getAttributeId() + "}" + facetField);
				} else {
					solrQuery.addFacetField(facetField);
				}
			}

			// facet filters
			for (final IFacetFilter facetFilter : query.getFacetFilters()) {
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

		return solrQuery;
	}

	@Override
	public IResult findByQuery(final IQuery query) {
		final IRuntimeContext context = getContext();
		final IDocumentManager documentManager = ModelUtil.getManager(IDocumentManager.class, context);
		final IFacetManager facetManager = ModelUtil.getManager(IFacetManager.class, context);
		return findByQuery(query, documentManager, facetManager);
	}

	@Override
	public IResult findByQuery(final IQuery query, final IDocumentManager documentManager, final IFacetManager facetManager) {
		if ((query == null) || !(query instanceof QueryImpl)) {
			throw new IllegalArgumentException("Invalid query. Must be created using #createQuery from this service instance.");
		}

		if (!(documentManager instanceof BaseSolrDocumentManager)) {
			throw new IllegalArgumentException("Invalid document manager. Does not match the service implementaion.");
		}

		if (!(facetManager instanceof BaseSolrFacetManager)) {
			throw new IllegalArgumentException("Invalid facet manager. Does not match the service implementaion.");
		}

		// create query
		final SolrQuery solrQuery = createSolrQuery((QueryImpl) query, facetManager);
		final QueryResponse response = ((BaseSolrDocumentManager) documentManager).query(solrQuery);
		return new ResultImpl(getContext(), (QueryImpl) query, response);
	}

	private Map<String, IFacet> getFacets(final IFacetManager facetManager) {
		refreshFacetsCache(facetManager);
		return facetsMapRef.get();
	}

	private void refreshFacetsCache(final IFacetManager facetManager) {
		final long time = facetsMapRefTime.get();
		if (System.currentTimeMillis() - time > 60000) {
			if (SearchDebug.debug) {
				LOG.debug("Refreshing facets configuration in context. {}", getContext().getContextPath());
			}
			if (facetManager != null) {
				try {
					facetsMapRef.set(facetManager.getFacets());
				} catch (final ModelException e) {
					LOG.warn("Error while reading facets from underlying data store. None will be used. {}", e.getMessage());
				}
			} else {
				if (SearchDebug.debug) {
					LOG.debug("No facet manager available in context. {}", getContext().getContextPath());
				}
			}
			facetsMapRefTime.set(System.currentTimeMillis());
		}
	}
}
