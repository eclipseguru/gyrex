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
package org.eclipse.gyrex.cds.query;

/**
 * A filter used in queries.
 * <p>
 * Filters are used to filter the set of documents to be searched. They can be
 * exclusive or inclusive. For example, using a filter it is possible to exclude
 * documents with a specific attribute (eg. not in stock) from the search. It's
 * also possible to limit the search to only those document with a specific
 * attribute set (eg. with tag AAA and/or BBB).
 * </p>
 * <p>
 * This interface must be implemented by contributors of a document model
 * implementation. As such it is considered part of a service provider API which
 * may evolve faster than the general API. Please get in touch with the
 * development team through the prefered channels listed on <a
 * href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay up-to-date
 * of possible changes.
 * </p>
 * <p>
 * Clients may not implement or extend this interface directly. If
 * specialization is desired they should look at the options provided by the
 * model implementation.
 * </p>
 */
public interface IFilter<T extends IFilter<T>> {

	/**
	 * Returns the filter type.
	 * 
	 * @return the filter type
	 * @see #ofType(FilterType)
	 */
	FilterType getType();

	/**
	 * Sets the filter type.
	 * <p>
	 * If the filter type is {@link FilterType#INCLUSIVE} all documents must
	 * match the filter in order to be considered part of the result.
	 * </p>
	 * <p>
	 * If the filter type is {@link FilterType#EXCLUSIVE} any document that
	 * matches the filter will not be part of the result.
	 * </p>
	 * <p>
	 * The default type is {@link FilterType#INCLUSIVE} if none was set.
	 * </p>
	 * 
	 * @param type
	 *            the filter type to set (may not be <code>null</code>)
	 * @return the filter for convenience
	 */
	T ofType(FilterType type);
}
