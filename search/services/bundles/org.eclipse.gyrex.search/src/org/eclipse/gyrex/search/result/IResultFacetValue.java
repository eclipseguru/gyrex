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
package org.eclipse.gyrex.cds.service.result;

import org.eclipse.gyrex.cds.service.query.ListingQuery;

/**
 * A {@link IListingResultFacet facet} value.
 */
public interface IListingResultFacetValue {

	/**
	 * Returns the number of listings which match the facet value.
	 * 
	 * @return the number of listings which match the facet valu
	 */
	long getCount();

	/**
	 * Returns the raw, un-encoded facet value.
	 * <p>
	 * Note, the value is not encoded in any way. Before using it in a
	 * {@link ListingQuery#addFilterQuery(String) filter query} it has to be
	 * encoded.
	 * </p>
	 * 
	 * @return the value
	 */
	String getValue();

	/**
	 * Returns a filter query which can be passed to
	 * {@link ListingQuery#addFilterQuery(String)} to narrow a query on the
	 * facet value.
	 * <p>
	 * Note, the returned filter query may only work within the same listing
	 * service implementation.
	 * </p>
	 * 
	 * @return a filter query
	 */
	String toFilterQuery();

}
