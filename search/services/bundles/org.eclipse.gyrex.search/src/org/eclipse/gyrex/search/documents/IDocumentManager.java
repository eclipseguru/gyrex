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

import java.util.Map;

import org.eclipse.gyrex.cds.model.documents.Document;
import org.eclipse.gyrex.model.common.IModelManager;

/**
 * The model manager for {@link IListing listings}.
 * <p>
 * The listings model manager provides a low-level, generic way of working with
 * listings in a repository.
 * </p>
 * <p>
 * This interface is intended to be implemented by clients that contribute a
 * listing model implementation.
 * </p>
 */
public interface IListingManager extends IModelManager {

	/**
	 * Finds multiple listing by their {@link IListing#getId() ids}.
	 * 
	 * @param ids
	 *            the listing ids to find
	 * @return an unmodifiable map of found {@link IListing} with
	 *         {@link IListing#getId() the listing id} as map key and the
	 *         {@link IListing} as map value
	 */
	Map<String, IListing> findById(Iterable<String> ids);

	/**
	 * Finds a listing by its {@link IListing#getId()}.
	 * 
	 * @param id
	 *            the listing id
	 * @return the found {@link IListing} or <code>null</code>
	 */
	IListing findById(String id);

	/**
	 * Publishes a set of documents to the repository.
	 * <p>
	 * If a document does not have an id, the model manager will assign a new
	 * generated id to the document prior to submitting the documents to the
	 * repository. The mechanism used for generating the id is implementation
	 * specific.
	 * </p>
	 * <p>
	 * If the repository already contains a listing for a document with the same
	 * id, the listing will be updated, otherwise the document will be added.
	 * </p>
	 * <p>
	 * Note, a publish operation is considered to finish asynchronously, i.e.
	 * when this method returns the documents might not be accessible
	 * immediately using the <code>find...</code> methods. Depending on the
	 * repository and amount of input the process is allowed to take a few
	 * minutes till several hours (or even days if you are feeding millions of
	 * documents).
	 * </p>
	 * 
	 * @param documents
	 *            the documents to publish
	 */
	void publish(Iterable<Document> documents);

}
