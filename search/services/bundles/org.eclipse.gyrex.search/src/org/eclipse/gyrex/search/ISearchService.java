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
package org.eclipse.gyrex.cds;

import org.eclipse.gyrex.cds.query.IQuery;
import org.eclipse.gyrex.cds.result.IResult;
import org.eclipse.gyrex.services.common.IService;

/**
 * The content delivery service.
 * <p>
 * Gyrex uses the concept of a content delivery service to deliver documents
 * (eg., products) to clients (eg., websites). The delivery service defines
 * methods for querying a document repository.
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
public interface IContentDeliveryService extends IService {

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
	 * Finds documents matching the specified query.
	 * 
	 * @param query
	 *            the query object
	 * @return the result
	 */
	IResult findByQuery(IQuery query);

}
