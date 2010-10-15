/*******************************************************************************
 * Copyright (c) 2008, 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.result;

import org.eclipse.gyrex.cds.query.IFacetFilter;
import org.eclipse.gyrex.cds.query.IQuery;

/**
 * A {@link IResultFacet facet} value.
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
public interface IResultFacetValue {

	/**
	 * Returns the number of listings which match the facet value.
	 * 
	 * @return the number of listings which match the facet valu
	 */
	long getCount();

	/**
	 * Returns the raw, un-escaped facet value string.
	 * <p>
	 * Note, the value is not escaped in any way. Before using it in a
	 * {@link IQuery#addFilterQuery(String) filter query} it has to be escaped.
	 * However, {@link IFacetFilter} generally work with un-escaped values so it
	 * can be used as is in {@link IFacetFilter}.
	 * </p>
	 * 
	 * @return the value
	 */
	String getValue();

	/**
	 * Returns a filter query which can be passed to
	 * {@link IQuery#addFilterQuery(String)} to narrow a query on the facet
	 * value.
	 * <p>
	 * Note, the returned filter query may only work within the same listing
	 * service implementation.
	 * </p>
	 * 
	 * @return a filter query
	 */
	String toFilterQuery();

}
