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
package org.eclipse.cloudfree.services.listings;

import java.util.concurrent.Future;


import org.eclipse.cloudfree.services.common.IService;
import org.eclipse.cloudfree.services.listings.query.ListingQuery;
import org.eclipse.cloudfree.services.listings.restult.IListingResult;
import org.eclipse.cloudfree.services.listings.restult.IListingResultCallback;

/**
 * The listing service.
 * <p>
 * The CloudFree platform uses the concept of a listing service to deliver
 * listings (eg., products) to storefronts or other external sites. The listing
 * service defines methods for querying a listings repository.
 * </p>
 * <p>
 * This interface must be implemented by clients that wish to provide a custom
 * listing service implementation.
 * </p>
 */
public interface IListingService extends IService {

	IListingResult findListings(ListingQuery query);

	/**
	 * Finds listings matching the specified query.
	 * <p>
	 * The query is designed to support raw input strings provided by users with
	 * no special escaping. <code>'+'</code> and <code>'-'</code> characters are
	 * treated as <em>"mandatory"</em> and <em>"prohibited"</em> modifiers for
	 * the subsequent terms. Text wrapped in <em>balanced</em> quote characters
	 * <code>'"'</code> are treated as phrases, any query containing an odd
	 * number of quote characters is evaluated as if there were no quote
	 * characters at all. Wildcards in a query are not supported.
	 * </p>
	 * 
	 * @param query
	 *            the query object
	 * @param callback
	 *            an optional callback interface for receiving the result in an
	 *            asynchronous manner instead of using the future
	 * @return the listings result
	 */
	Future<IListingResult> findListings(ListingQuery query, IListingResultCallback callback);
}
