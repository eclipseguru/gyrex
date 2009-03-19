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
package org.eclipse.gyrex.cds.model;


import org.eclipse.core.runtime.IPath;
import org.eclipse.gyrex.model.common.IModelObject;

/**
 * A listing model object.
 * <p>
 * Listings are a core element of the listings story. They can represent
 * products for sale on a storefront or items for sale in an auction. However,
 * the key is that they are not limited to those two possibilities. Basically, a
 * listing can by anything that you want to present in some way to somebody.
 * It's also possible that listings are digital goods which can be downloaded or
 * simply texts which can be viewed on-line.
 * </p>
 * <p>
 * Therefore, listings do not provide a fixed structure. They are unstructured
 * documents consisting of a bunch of simple name-value attributes. The
 * attributes describe the listing further.
 * </p>
 * <p>
 * In order to provide a common infrastructure for working with listings a set
 * of base listing attributes is defined by this interface.
 * </p>
 * <p>
 * Listings can be navigated. In order to make the navigation as flexible as
 * possible it will be based on the listing attributes as well. A set of common
 * navigational attributes is defined by this interface.
 * </p>
 * <p>
 * By definition, listings do <strong>not</strong> implement multi-language
 * behavior. Instead, different language should be represented by different
 * listings. It may also be necessary to separate listings in different
 * languages into different repositories because a single repository may be
 * optimized for a single language only.
 * </p>
 */
public interface IListing extends IModelObject {

	/**
	 * Returns the listing attribute with the specified name.
	 * 
	 * @return the listing attribute
	 */
	IListingAttribute getAttribute(String name);

	/**
	 * Returns the listing attributes.
	 * 
	 * @return the listing attributes
	 */
	IListingAttribute[] getAttributes();

	/**
	 * Returns a human-readable description of the listing.
	 * 
	 * @return a human-readable description
	 */
	String getDescription();

	/**
	 * Returns the milliseconds from the Java epoch of
	 * <code>1970-01-01T00:00:00Z</code> when the listing should be hidden.
	 * <p>
	 * This allows to control the visibility of listings.
	 * </p>
	 * 
	 * @return the milliseconds from the Java epoch of
	 *         <code>1970-01-01T00:00:00Z</code> when the listing should be
	 *         hidden, or <code>0</code> if the listings visibility end time is
	 *         not limited
	 */
	long getEnd();

	/**
	 * Returns a machine generated unique identifier of a listing. It's purpose
	 * is to locate a single specific listing if necessary.
	 * 
	 * @return a machine generated unique identifier of a listing
	 */
	String getId();

	/**
	 * Returns a human-readable name of a listing which is typically an
	 * identifier that is unique and makes sense in a specific context (eg. a
	 * product/sku number).
	 * 
	 * @return a human-readable name of a listing
	 */
	String getName();

	/**
	 * Returns all paths a listing is located in (eg.
	 * <code>"folder/sub/subsub"</code>).
	 * 
	 * @return all paths a listing is located in
	 */
	IPath[] getPaths();

	/**
	 * Returns the milliseconds from the Java epoch of
	 * <code>1970-01-01T00:00:00Z</code> when the listing should be visible.
	 * <p>
	 * This allows to control the visibility of listings.
	 * </p>
	 * 
	 * @return the milliseconds from the Java epoch of
	 *         <code>1970-01-01T00:00:00Z</code> when the listing should be
	 *         visible, or <code>0</code> if the listings visibility start time
	 *         is not limited
	 */
	long getStart();

	/**
	 * Returns all tags (aka. labels) attached to a listing.
	 * <p>
	 * This allows to navigate listings through a tag cloud or to filter based
	 * on tags.
	 * </p>
	 * <p>
	 * Because tags are optional, an empty array will be returned if no tags are
	 * attached with the listing.
	 * </p>
	 * 
	 * @return all tags attached to a listing
	 */
	String[] getTags();

	/**
	 * Returns a human-readable listing title.
	 * 
	 * @return a human-readable listing title
	 */
	String getTitle();

	/**
	 * Returns a canonical URI pathname to the listing.
	 * <p>
	 * The URI path is useful for building search engine friendly site URLs. The
	 * URI does not need to be unique across the board but should be unique
	 * within the same lookup context (eg. unique across all auction listings of
	 * a site). Thus, it must be interpreted relative to the context base.
	 * </p>
	 * <p>
	 * The returned URI path is absolute, will start with a slash and is
	 * guaranteed to not contain relative segments such as <code>"."</code> and
	 * <code>".."</code>.
	 * 
	 * @return a canonical URI pathname to the listing
	 */
	String getUriPath();

}
