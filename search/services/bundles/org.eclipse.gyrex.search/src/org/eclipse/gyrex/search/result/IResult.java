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


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gyrex.cds.model.IListing;
import org.eclipse.gyrex.cds.service.query.ListingQuery;
import org.eclipse.gyrex.context.IRuntimeContext;

/**
 * The listing result.
 * <p>
 * This object is the result of a search using the listings service. It provides
 * general information about the found listings, the listings itself and
 * optionally (implementation/context specific) a set of filter to further
 * narrow the results (useful for guided navigation).
 * </p>
 * <p>
 * This interfaces extends the {@link IAdaptable} interface in order to support
 * extensibility of the result.
 * </p>
 * <p>
 * Note, clients which contribute a listing service must not implement this
 * interface directly but subclass
 * {@link org.eclipse.gyrex.cds.service.implementors.BaseListingResult
 * BaseListingResult} instead.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              They must subclass
 *              {@link org.eclipse.gyrex.cds.service.implementors.BaseListingResult
 *              BaseListingResult} instead.
 */
public interface IListingResult extends IAdaptable {

	/**
	 * Returns the context this result is associated to.
	 * <p>
	 * Note, this is the context of listing service which generated the result.
	 * </p>
	 * 
	 * @return the context
	 */
	IRuntimeContext getContext();

	/**
	 * Returns the facets available in the result.
	 * 
	 * @return a list of facets
	 */
	IListingResultFacet[] getFacets();

	/**
	 * Returns the list of listings that match the query.
	 * 
	 * @return the list of listings
	 */
	IListing[] getListings();

	/**
	 * Returns the total number of listings found.
	 * 
	 * @return the number of found listings
	 */
	long getNumFound();

	/**
	 * Returns the query that was passed to the listing service to generate the
	 * result.
	 * 
	 * @return the query
	 */
	ListingQuery getQuery();

	/**
	 * Returns the query time in milliseconds.
	 * 
	 * @return the query time
	 */
	long getQueryTime();

	/**
	 * Returns the start offset (zero based).
	 * 
	 * @return the start offset
	 */
	long getStartOffset();
}
