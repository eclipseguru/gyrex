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
package org.eclipse.cloudfree.listings.service.solr.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cloudfree.services.listings.restult.IListingResultFacet;
import org.eclipse.cloudfree.services.listings.restult.IListingResultFacetValue;

public class SolrListingResultFacet implements IListingResultFacet {

	private final String id;
	private final String label;
	private final List<SolrListingResultFacetValue> values;

	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 * @param label
	 */
	public SolrListingResultFacet(final String id, final String label) {
		this.id = id;
		this.label = label;
		values = new ArrayList<SolrListingResultFacetValue>(4);
	}

	void addValue(final SolrListingResultFacetValue value) {
		values.add(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.services.listings.restult.IListingResultFacet#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.services.listings.restult.IListingResultFacet#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.services.listings.restult.IListingResultFacet#getValues()
	 */
	@Override
	public IListingResultFacetValue[] getValues() {
		return values.toArray(new IListingResultFacetValue[values.size()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SolrListingResultFacet[ " + id + " " + values + " ]";
	}
}
