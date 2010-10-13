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

import org.eclipse.gyrex.cds.result.IListingResultFacetValue;

/**
 * 
 */
public class SolrListingResultFacetValue implements IListingResultFacetValue {

	private final long count;
	private final String value;
	private final String solrFilterQuery;

	/**
	 * Creates a new instance.
	 * 
	 * @param count
	 */
	public SolrListingResultFacetValue(final long count, final String value, final String solrFilterQuery) {
		this.count = count;
		this.value = value;
		this.solrFilterQuery = solrFilterQuery;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResultFacetValue#getCount()
	 */
	@Override
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResultFacetValue#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResultFacetValue#toFilterQuery()
	 */
	@Override
	public String toFilterQuery() {
		return solrFilterQuery;
	}

	@Override
	public String toString() {
		return value + " (" + count + ")";
	}

}
