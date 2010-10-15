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

import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.query.IQuery;
import org.eclipse.gyrex.context.IRuntimeContext;

import org.eclipse.core.runtime.IAdaptable;

/**
 * The result of a query.
 * <p>
 * This object is the result of a search using the content delivery service. It
 * provides general information about the found documents, the documents itself
 * and optionally (implementation/context specific) a set of filter to further
 * narrow the results (useful for faceted search).
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
public interface IResult extends IAdaptable {

	/**
	 * Returns the context this result is associated to.
	 * <p>
	 * Note, this is the context of content delivery service which generated the
	 * result.
	 * </p>
	 * 
	 * @return the context
	 */
	IRuntimeContext getContext();

	/**
	 * Returns a map of facets available in the result.
	 * 
	 * @return an unmodifiable map of all facets in the result with
	 *         {@link IFacet#getAttributeId() the attribute id} as the map key
	 *         and the {@link IFacet facet} as the value
	 */
	Map<String, IResultFacet> getFacets();

	/**
	 * Returns the list of documents that match the query.
	 * 
	 * @return the list of documents
	 */
	IDocument[] getListings();

	/**
	 * Returns the total number of documents found.
	 * 
	 * @return the number of found documents
	 */
	long getNumFound();

	/**
	 * Returns the query that was passed to the content delivery service to
	 * generate the result.
	 * 
	 * @return the query
	 */
	IQuery getQuery();

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
