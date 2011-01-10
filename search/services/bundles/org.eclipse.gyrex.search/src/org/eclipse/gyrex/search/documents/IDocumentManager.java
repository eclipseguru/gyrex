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
package org.eclipse.gyrex.cds.documents;

import org.eclipse.gyrex.model.common.IModelManager;
import org.eclipse.gyrex.model.common.exceptions.ObjectNotFoundException;

/**
 * The manager for working with {@link IDocument documents} and
 * {@link IDocumentCollection collections}.
 * <p>
 * The document model manager provides a generic way of working with documents
 * contained in collections a repository.
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
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDocumentManager extends IModelManager {

	/**
	 * Creates and returns a new transient document.
	 * <p>
	 * The document will not be contained in the repository until it has been
	 * {@link #publish(Iterable) published}.
	 * </p>
	 * 
	 * @param attributeId
	 *            the id of the {@link IDocumentAttribute attribute} to create
	 *            the facet for
	 * @return a transient facet
	 */
	IDocument createDocument();

	/**
	 * Returns the specified collection.
	 * <p>
	 * Documents are organized in {@link IDocumentCollection collections}. The
	 * collections are typically created and managed through an administration
	 * backend of the underlying repository.
	 * </p>
	 * 
	 * @param collection
	 *            the collection
	 * @return the found {@link IDocumentCollection collection}
	 * @throws ObjectNotFoundException
	 *             if the specified collection is unknown
	 */
	IDocumentCollection getCollection(String collection) throws ObjectNotFoundException;
}
