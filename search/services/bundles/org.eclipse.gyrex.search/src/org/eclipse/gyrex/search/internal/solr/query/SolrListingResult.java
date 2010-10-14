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
package org.eclipse.gyrex.cds.service.solr.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.query.ListingQuery;
import org.eclipse.gyrex.cds.result.IListingResultFacet;
import org.eclipse.gyrex.cds.solr.internal.ListingsSolrModelActivator;
import org.eclipse.gyrex.cds.solr.internal.SolrListing;
import org.eclipse.gyrex.cds.spi.result.BaseListingResult;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.preferences.IRuntimeContextPreferences;
import org.eclipse.gyrex.context.preferences.PreferencesUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 */
public class SolrListingResult extends BaseListingResult {

	private final QueryResponse response;
	private IDocument[] listings;
	private IListingResultFacet[] facets;

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param query
	 * @param response
	 */
	protected SolrListingResult(final IRuntimeContext context, final ListingQuery query, final QueryResponse response) {
		super(context, query);
		this.response = response;
	}

	private IListingResultFacet createResultFacet(final FacetField facetField) {
		final SolrListingResultFacet facet = getFacetFieldInfo(facetField.getName());
		if (null == facet) {
			return null;
		}

		final List<Count> values = facetField.getValues();
		if (null == values) {
			return null;
		}

		for (final Count count : values) {
			// TODO: this should be configurable
			if (count.getCount() >= 0) {
				facet.addValue(new SolrListingResultFacetValue(count.getCount(), count.getName(), count.getAsFilterQuery()));
			}
		}

		if (facet.getValues().length == 0) {
			return null;
		}

		return facet;
	}

	private void createResultFacets(final Map<String, Integer> facetQuery, final List<IListingResultFacet> facets) {
		final Map<String, SolrListingResultFacet> queryFacets = new LinkedHashMap<String, SolrListingResultFacet>(facetQuery.size());
		for (final Entry<String, Integer> facetQueryEntry : facetQuery.entrySet()) {
			final String[] queryInfo = getFacetQueryValueInfo(facetQueryEntry.getKey());
			if (null != queryInfo) {
				// [facet id, label, query label]
				final String facetId = queryInfo[0];
				if (!queryFacets.containsKey(facetId)) {
					queryFacets.put(facetId, new SolrListingResultFacet(facetId, queryInfo[1]));
				}
				final long count = facetQueryEntry.getValue();
				// TODO: this should be configurable
				if (count >= 0) {
					queryFacets.get(facetId).addValue(new SolrListingResultFacetValue(count, queryInfo[2], facetQueryEntry.getKey()));
				}
			}
		}
		for (final SolrListingResultFacet queryFacet : queryFacets.values()) {
			if (queryFacet.getValues().length > 0) {
				facets.add(queryFacet);
			}
		}
	}

	private SolrListingResultFacet getFacetFieldInfo(final String fieldName) {
		final IRuntimeContextPreferences preferences = PreferencesUtil.getPreferences(getContext());
		final String[] activeFacets = StringUtils.split(preferences.get(ListingsSolrModelActivator.SYMBOLIC_NAME, "activeFacets", null), ',');
		for (final String facetId : activeFacets) {
			final String facetString = preferences.get(ListingsSolrModelActivator.SYMBOLIC_NAME, "facets/" + facetId, null);
			if (null != facetString) {
				final String[] split = StringUtils.split(facetString, ',');
				if ((split.length == 3) && split[1].equals("field") && StringUtils.isNotBlank(split[2])) {
					if (split[2].equals(fieldName)) {
						return new SolrListingResultFacet(facetId, split[0]);
					}
				}
			}
		}
		return null;
	}

	private String[] getFacetQueryValueInfo(final String query) {
		final IRuntimeContextPreferences preferences = PreferencesUtil.getPreferences(getContext());
		final String[] activeFacets = StringUtils.split(preferences.get(ListingsSolrModelActivator.SYMBOLIC_NAME, "activeFacets", null), ',');
		for (final String facetId : activeFacets) {
			final String facetString = preferences.get(ListingsSolrModelActivator.SYMBOLIC_NAME, "facets/" + facetId, null);
			if (null != facetString) {
				final String[] split = StringUtils.split(facetString, ',');
				if ((split.length == 3) && split[1].equals("queries") && StringUtils.isNotBlank(split[2])) {
					final String[] split2 = StringUtils.split(split[2], ';');
					for (final String queryString : split2) {
						final String[] split3 = StringUtils.split(queryString, '=');
						if ((split3.length == 2) && StringUtils.isNotBlank(split3[0])) {
							if (split3[0].equals(queryString)) {
								// id, label, query label
								return new String[] { facetId, split[0], split3[1] };
							}
						}
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResult#getFacets()
	 */
	@Override
	public IListingResultFacet[] getFacets() {
		if (null != facets) {
			return facets;
		}
		final List<FacetField> facetFields = response.getFacetFields();
		final Map<String, Integer> facetQuery = response.getFacetQuery();

		if ((null == facetFields) && (null == facetQuery)) {
			return facets = new IListingResultFacet[0];
		}

		final List<IListingResultFacet> facets = new ArrayList<IListingResultFacet>();
		if (null != facetFields) {
			for (final FacetField facetField : facetFields) {
				final IListingResultFacet facet = createResultFacet(facetField);
				if (null != facet) {
					facets.add(facet);
				}
			}
		}
		if (null != facetQuery) {
			createResultFacets(facetQuery, facets);
		}
		return this.facets = facets.toArray(new IListingResultFacet[facets.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResult#getListings()
	 */
	@Override
	public IDocument[] getListings() {
		if (null != listings) {
			return listings;
		}
		final SolrDocumentList results = response.getResults();
		final IDocument[] listings = new IDocument[results.size()];
		for (int i = 0; i < listings.length; i++) {
			listings[i] = new SolrListing(results.get(i));
		}
		return this.listings = listings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResult#getNumFound()
	 */
	@Override
	public long getNumFound() {
		return response.getResults().getNumFound();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResult#getQueryTime()
	 */
	@Override
	public long getQueryTime() {
		return response.getQTime();
	}

	@Override
	public long getStartOffset() {
		return response.getResults().getStart();
	}

}
