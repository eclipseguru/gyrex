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

/**
 * The CloudFree Listings Story
 * <p>
 * A common concept of on-line shop systems is to have store items which
 * represent products. Some shop systems work with products directly. In the
 * auction world this is similar. There are various reasons for decoupling the
 * store items from the products most notably being design driven (separation of
 * concerns) and performance.
 * </p>
 * <p>
 * The listing story provided by the CloudFree Platform is a based on the same
 * basic principles. However, the concept goes beyond those principles and
 * creates a full-fledged listing store which is suitable for serving auction
 * sites as well as classic storefronts and can be extended for any other
 * scenario.
 * </p>
 * <p>
 * It's also essential to note that the listing story is deeply integrated with
 * search. Actually, search will be the only way to retrieve listings from the
 * underlying store. Traditional browsing will be possible by filtering.
 * </p>
 * <p>
 * Following the CloudFree approach the listing service provides a common
 * interface for storefront/auction (i.e., web site) developers and hides the
 * complexity from them.
 * </p>
 */
package org.eclipse.cloudfree.services.listings;

