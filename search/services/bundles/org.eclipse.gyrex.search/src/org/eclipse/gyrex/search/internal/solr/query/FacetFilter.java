/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.query;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.query.FacetSelectionStrategy;
import org.eclipse.gyrex.cds.query.FilterType;
import org.eclipse.gyrex.cds.query.IFacetFilter;
import org.eclipse.gyrex.cds.query.QueryUtil;
import org.eclipse.gyrex.cds.query.TermCombination;
import org.eclipse.gyrex.cds.solr.internal.SolrSchemaConventions;

/**
 *
 */
public class FacetFilter extends BaseFilter implements IFacetFilter {

	private final IFacet facet;
	private TermCombination combination;
	private FacetSelectionStrategy selectionStrategy;

	private final List<String> values = new ArrayList<String>(1);

	/**
	 * Creates a new instance.
	 * 
	 * @param facet
	 */
	public FacetFilter(final IFacet facet) {
		this.facet = facet;
	}

	@Override
	public IFacetFilter combineUsing(final TermCombination combination) {
		this.combination = combination;
		return this;
	}

	@Override
	public IFacet getFacet() {
		return facet;
	}

	@Override
	public FacetSelectionStrategy getSelectionStrategy() {
		if (selectionStrategy != null) {
			return selectionStrategy;
		}
		return getFacet().getSelectionStrategy();
	}

	@Override
	public TermCombination getTermCombination() {
		if (combination != null) {
			return combination;
		}

		return getFacet().getTermCombination();
	}

	@Override
	public FacetFilter ofType(final FilterType type) {
		return (FacetFilter) super.ofType(type);
	}

	@Override
	public IFacetFilter select(final FacetSelectionStrategy selectionStrategy) {
		this.selectionStrategy = selectionStrategy;
		return this;
	}

	@Override
	public String toFilterQuery() {
		if (values.isEmpty()) {
			return null;
		}

		final StringBuilder q = new StringBuilder();
		if (getType() == FilterType.EXCLUSIVE) {
			q.append('-');
		}
		if (getSelectionStrategy() == FacetSelectionStrategy.MULTI) {
			q.append("{!tag=").append(facet.getAttributeId()).append('}');
		}
		q.append(SolrSchemaConventions.facetFieldName(facet.getAttributeId())).append(':');
		if (values.size() > 1) {
			q.append('(');
			String separator = null;
			for (final String value : values) {
				if (separator != null) {
					q.append(separator);
				} else {
					separator = getTermCombination() == TermCombination.AND ? " AND " : " OR ";
				}
				q.append(QueryUtil.escapeQueryChars(value));
			}
			q.append(')');
		} else {
			q.append(QueryUtil.escapeQueryChars(values.get(0)));
		}

		return q.toString();
	}

	@Override
	public IFacetFilter withValue(final String value) {
		values.add(value);
		return this;
	}

	@Override
	public IFacetFilter withValues(final String... values) {
		for (final String value : values) {
			this.values.add(value);
		}
		return this;
	}

}
