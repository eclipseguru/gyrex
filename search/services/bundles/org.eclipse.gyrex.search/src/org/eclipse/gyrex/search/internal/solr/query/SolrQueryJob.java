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
package org.eclipse.cloudfree.cds.service.solr.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;


import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.eclipse.cloudfree.cds.model.solr.ISolrQueryExecutor;
import org.eclipse.cloudfree.cds.service.query.ListingQuery;
import org.eclipse.cloudfree.cds.service.query.ListingQuery.SortDirection;
import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.configuration.preferences.PlatformScope;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * 
 */
public class SolrQueryJob extends Job {

	static SolrQuery createSolrQuery(final ListingQuery query) {
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
		final String[] facetFields = getFacetFields();
		if (null != facetFields) {
			solrQuery.addFacetField(facetFields);
		}
		final String[] facetQueries = getFacetQueries();
		if (null != facetQueries) {
			for (final String string : facetQueries) {
				solrQuery.addFacetQuery(string);
			}
		}

		// dimension
		switch (query.getResultDimension()) {
			case FULL:
				solrQuery.setFields("*");
				break;

			case COMPACT:
			default:
				// TODO this must be configurable per context/repository
				//solrQuery.setFields("id", "name", "title", "description", "price", "score", "img48", "uripath", "category", "tags");
				// default to what is defined in solrconfig.xml
				solrQuery.setFields((String[]) null);
				break;
		}

		return solrQuery;
	}

	private static String[] getFacetFields() {
		final Preferences facets = new PlatformScope().getNode("org.eclipse.cloudfree.cds.service.solr").node("facets");
		try {
			final List<String> fields = new ArrayList<String>();
			for (final String facetId : facets.keys()) {
				final String facetString = facets.get(facetId, null);
				if (null != facetString) {
					final String[] split = StringUtils.split(facetString, ',');
					if ((split.length == 3) && split[1].equals("field") && StringUtils.isNotBlank(split[2])) {
						fields.add(split[2]);
					}
				}
			}
			if (fields.isEmpty()) {
				return null;
			}
			return fields.toArray(new String[fields.size()]);
		} catch (final BackingStoreException e) {
			return null;
		}
	}

	private static String[] getFacetQueries() {
		final Preferences facets = new PlatformScope().getNode("org.eclipse.cloudfree.cds.service.solr").node("facets");
		try {
			final List<String> queries = new ArrayList<String>();
			for (final String facetId : facets.keys()) {
				final String facetString = facets.get(facetId, null);
				if (null != facetString) {
					final String[] split = StringUtils.split(facetString, ',');
					if ((split.length == 3) && split[1].equals("queries") && StringUtils.isNotBlank(split[2])) {
						final String[] split2 = StringUtils.split(split[2], ';');
						for (final String queryString : split2) {
							final String[] split3 = StringUtils.split(queryString, '=');
							if ((split3.length == 2) && StringUtils.isNotBlank(split3[0])) {
								queries.add(split3[0]);
							}
						}
					}
				}
			}
			if (queries.isEmpty()) {
				return null;
			}
			return queries.toArray(new String[queries.size()]);
		} catch (final BackingStoreException e) {
			return null;
		}
	}

	private final ListingQuery query;

	private final ISolrQueryExecutor queryExecutor;

	private final AtomicReference<SolrListingFuture> solrListingFutureRef = new AtomicReference<SolrListingFuture>();

	private final IContext context;

	/**
	 * Creates a new instance.
	 * 
	 * @param query
	 * @param queryExecutor
	 * @param context
	 */
	public SolrQueryJob(final ListingQuery query, final ISolrQueryExecutor queryExecutor, final IContext context) {
		super("Solr Query Job - " + query);
		this.query = query;
		this.queryExecutor = queryExecutor;
		this.context = context;
		setPriority(SHORT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask(query.toString(), 3);
		try {
			monitor.subTask("creating query");
			final SolrQuery solrQuery = createSolrQuery(query);
			monitor.worked(1);
			final SolrListingFuture solrListingFuture = solrListingFutureRef.get();
			if ((null == solrListingFuture) || monitor.isCanceled() || solrListingFuture.isCancelled()) {
				return Status.CANCEL_STATUS;
			}

			monitor.subTask("querying solr server " + queryExecutor);
			final QueryResponse response = queryExecutor.query(solrQuery);
			monitor.worked(1);
			if ((monitor.isCanceled()) || solrListingFuture.isCancelled()) {
				return Status.CANCEL_STATUS;
			}

			monitor.subTask("submitting response...");
			if (null != response) {
				solrListingFuture.onResult(new SolrListingResult(context, query, response));
			}

		} catch (final Exception e) {
			final SolrListingFuture solrListingFuture = solrListingFutureRef.get();
			if ((null == solrListingFuture) || monitor.isCanceled() || solrListingFuture.isCancelled()) {
				return Status.CANCEL_STATUS;
			}
			solrListingFuture.onError(e);
			return Status.CANCEL_STATUS;
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	void setCallback(final SolrListingFuture solrListingFuture) {
		solrListingFutureRef.set(solrListingFuture);
	}

}
