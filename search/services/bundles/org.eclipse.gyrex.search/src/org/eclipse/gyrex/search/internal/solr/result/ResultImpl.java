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
package org.eclipse.gyrex.cds.solr.internal.result;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.query.IQuery;
import org.eclipse.gyrex.cds.result.IResult;
import org.eclipse.gyrex.cds.result.IResultFacet;
import org.eclipse.gyrex.cds.solr.internal.SolrSchemaConventions;
import org.eclipse.gyrex.cds.solr.internal.documents.StoredDocument;
import org.eclipse.gyrex.cds.solr.internal.query.QueryImpl;
import org.eclipse.gyrex.context.IRuntimeContext;

import org.eclipse.core.runtime.PlatformObject;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 * {@link IResult} implementation
 */
public class ResultImpl extends PlatformObject implements IResult {

	private final QueryResponse response;
	private IDocument[] listings;
	private Map<String, IResultFacet> facets;
	private final IRuntimeContext context;
	private final QueryImpl query;

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param query
	 * @param response
	 */
	public ResultImpl(final IRuntimeContext context, final QueryImpl query, final QueryResponse response) {
		this.context = context;
		this.query = query;
		this.response = response;
	}

	/**
	 * Creates a {@link ResultFacet} from a {@link FacetField}.
	 */
	private ResultFacet createFacet(final FacetField facetField) {
		// look in the query if a facet has been used there (facet fields)
		final Map<String, IFacet> facetsInUse = query.getFacetsInUse();
		if (facetsInUse == null) {
			return null;
		}
		final IFacet facet = facetsInUse.get(SolrSchemaConventions.facetAttributeId(facetField.getName()));
		if (facet == null) {
			return null;
		}

		// check if there are values returned
		final List<Count> values = facetField.getValues();
		if ((null == values) || values.isEmpty()) {
			return null;
		}

		// create result facet
		final ResultFacet f = new ResultFacet(facet);
		for (final Count count : values) {
			f.addValue(new ResultFacetValue(count.getCount(), count.getName()));
		}

		return f;
	}

	@Override
	public IRuntimeContext getContext() {
		return context;
	}

	@Override
	public Map<String, IResultFacet> getFacets() {
		if (null != facets) {
			return facets;
		}
		final List<FacetField> facetFields = response.getFacetFields();

		if ((facetFields == null) || facetFields.isEmpty()) {
			return facets = Collections.emptyMap();
		}

		// create results facets from facet fields
		final Map<String, IResultFacet> facets = new HashMap<String, IResultFacet>(facetFields.size());
		if (null != facetFields) {
			for (final FacetField facetField : facetFields) {
				final ResultFacet facet = createFacet(facetField);
				if (null != facet) {
					facets.put(facetField.getName(), facet);
				}
			}
		}

		return this.facets = Collections.unmodifiableMap(facets);
	}

	@Override
	public IDocument[] getListings() {
		if (null != listings) {
			return listings;
		}
		final SolrDocumentList results = response.getResults();
		final IDocument[] listings = new IDocument[results.size()];
		for (int i = 0; i < listings.length; i++) {
			listings[i] = new StoredDocument(results.get(i));
		}
		return this.listings = listings;
	}

	@Override
	public long getNumFound() {
		return response.getResults().getNumFound();
	}

	@Override
	public IQuery getQuery() {
		return query;
	}

	@Override
	public long getQueryTime() {
		return response.getQTime();
	}

	@Override
	public long getStartOffset() {
		return response.getResults().getStart();
	}

}
