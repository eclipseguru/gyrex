/*******************************************************************************
 * Copyright (c) 2008, 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *******************************************************************************/
package org.eclipse.gyrex.search;

import org.eclipse.gyrex.search.documents.IDocumentManager;
import org.eclipse.gyrex.search.facets.IFacetManager;
import org.eclipse.gyrex.search.query.IQuery;
import org.eclipse.gyrex.search.result.IResult;
import org.eclipse.gyrex.services.common.IService;

/**
 * The search service.
 * <p>
 * Gyrex uses the concept of a search to deliver documents (eg., product
 * listings) to clients (eg., websites). The search service defines methods for
 * querying a document repository.
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
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISearchService extends IService {

	/**
	 * Creates a new query object.
	 * <p>
	 * This is the primary way of creating {@link IQuery query} object
	 * instances.
	 * </p>
	 * 
	 * @return a model implementation of {@link IQuery}.
	 */
	IQuery createQuery();

	/**
	 * Finds documents matching the specified query using default managers
	 * available in the context the service operates in.
	 * 
	 * @param query
	 *            the query object
	 * @return the result
	 */
	IResult findByQuery(IQuery query);

	/**
	 * Finds documents matching the specified query within the specified
	 * managers.
	 * <p>
	 * This method may be called if the default managers aren't used but more
	 * specialized ones.
	 * </p>
	 * 
	 * @param query
	 *            the query object
	 * @param documentManager
	 *            the documentManager
	 * @param facetManager
	 *            the facet manager
	 * @return the result
	 */
	IResult findByQuery(IQuery query, IDocumentManager documentManager, IFacetManager facetManager);

}
