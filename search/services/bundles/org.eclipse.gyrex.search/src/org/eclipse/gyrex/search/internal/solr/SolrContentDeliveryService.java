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
package org.eclipse.gyrex.cds.solr.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cds.IContentDeliveryService;
import org.eclipse.gyrex.cds.documents.IDocumentManager;
import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.cds.query.FacetSelectionStrategy;
import org.eclipse.gyrex.cds.query.IFacetFilter;
import org.eclipse.gyrex.cds.query.IQuery;
import org.eclipse.gyrex.cds.query.SortDirection;
import org.eclipse.gyrex.cds.result.IResult;
import org.eclipse.gyrex.cds.solr.internal.query.FacetFilter;
import org.eclipse.gyrex.cds.solr.internal.query.QueryImpl;
import org.eclipse.gyrex.cds.solr.internal.result.ResultImpl;
import org.eclipse.gyrex.cds.solr.solrj.ISolrQueryExecutor;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.model.common.ModelUtil;
import org.eclipse.gyrex.services.common.provider.BaseService;
import org.eclipse.gyrex.services.common.status.IStatusMonitor;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solr based {@link IContentDeliveryService} implementation.
 */
public class SolrContentDeliveryService extends BaseService implements IContentDeliveryService {

	private static final Logger LOG = LoggerFactory.getLogger(SolrContentDeliveryService.class);

	private final AtomicLong facetsMapRefTime = new AtomicLong();
	private final AtomicReference<Map<String, IFacet>> facetsMapRef = new AtomicReference<Map<String, IFacet>>();

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param statusMonitor
	 * @param metrics
	 */
	SolrContentDeliveryService(final IRuntimeContext context, final IStatusMonitor statusMonitor) {
		super(context, statusMonitor, new SolrContentDeliveryServiceMetrics(createMetricsId("org.eclipse.gyrex.cds.solr.service", context)));
	}

	@Override
	public IQuery createQuery() {
		return new QueryImpl();
	}

	public SolrQuery createSolrQuery(final QueryImpl query) {
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
		if (SolrCdsDebug.debug) {
			solrQuery.setShowDebugInfo(true);
		}

		return solrQuery;
	}

	@Override
	public IResult findByQuery(final IQuery query) {
		if ((query == null) || !(query instanceof QueryImpl)) {
			throw new IllegalStateException("Invalid query. Must be created using #createQuery from this service instance.");
		}

		final IDocumentManager manager = ModelUtil.getManager(IDocumentManager.class, getContext());
		final ISolrQueryExecutor queryExecutor = (ISolrQueryExecutor) manager.getAdapter(ISolrQueryExecutor.class);
		if (null == queryExecutor) {
			throw new IllegalStateException("The context document manager is not a Solr based listing manager.");
		}

		// create query
		final SolrQuery solrQuery = createSolrQuery((QueryImpl) query);
		final QueryResponse response = queryExecutor.query(solrQuery);
		return new ResultImpl(getContext(), (QueryImpl) query, response);
	}

	private Map<String, IFacet> getFacets() {
		refreshFacetsCache();
		return facetsMapRef.get();
	}

	private void refreshFacetsCache() {
		final long time = facetsMapRefTime.get();
		if (System.currentTimeMillis() - time > 60000) {
			if (SolrCdsDebug.debug) {
				LOG.debug("Refreshing facets configuration in context. {}", getContext().getContextPath());
			}
			final IFacetManager facetManager = getContext().get(IFacetManager.class);
			if (facetManager != null) {
				try {
					facetsMapRef.set(facetManager.getFacets());
				} catch (final ModelException e) {
					LOG.warn("Error while reading facets from underlying data store. None will be used. {}", e.getMessage());
				}
			} else {
				if (SolrCdsDebug.debug) {
					LOG.debug("No facet manager available in context. {}", getContext().getContextPath());
				}
			}
			facetsMapRefTime.set(System.currentTimeMillis());
		}
	}
}
