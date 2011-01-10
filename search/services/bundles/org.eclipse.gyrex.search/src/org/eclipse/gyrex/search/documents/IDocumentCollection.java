/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
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

import java.util.Map;

import org.eclipse.gyrex.model.common.IModelObject;

/**
 * A collection of documents.
 * <p>
 * A collection allows to group similar documents together (eg. all documents in
 * a particular local and/or of a particular type).
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
public interface IDocumentCollection extends IModelObject {

	/**
	 * Finds multiple documents by their {@link IDocument#getId() ids}.
	 * 
	 * @param ids
	 *            the listing ids to find
	 * @return an unmodifiable map of found {@link IDocument} with
	 *         {@link IDocument#getId() the listing id} as map key and the
	 *         {@link IDocument} as map value
	 */
	Map<String, IDocument> findById(Iterable<String> ids);

	/**
	 * Finds a document by its {@link IDocument#getId()}.
	 * 
	 * @param id
	 *            the listing id
	 * @return the found {@link IDocument} or <code>null</code> if not found
	 */
	IDocument findById(String id);

	/**
	 * Publishes a set of documents to the repository.
	 * <p>
	 * If a document does not have an id, the manager will assign a new
	 * generated id to the document prior to submitting the documents to the
	 * repository. The mechanism used for generating the id is implementation
	 * specific.
	 * </p>
	 * <p>
	 * If the repository already contains an entry for a document with the same
	 * id, the entry will be <em>replaced</em>, otherwise the document will be
	 * <em>added</em>.
	 * </p>
	 * <p>
	 * Note, a publish operation may finish asynchronously, i.e. when this
	 * method returns the documents might not be accessible immediately using
	 * the <code>find...</code> methods. Depending on the repository and amount
	 * of input the process is allowed to take a few minutes till several hours
	 * (or even days if you are feeding millions of documents).
	 * </p>
	 * 
	 * @param documents
	 *            the documents to publish
	 */
	void publish(Iterable<IDocument> documents);

	/**
	 * Removes a set of documents from a repository.
	 * <p>
	 * Note, a remove operation may finish asynchronously, i.e. when this method
	 * returns the documents might still be accessible using the
	 * <code>find...</code> methods. Depending on the repository and amount of
	 * input the process is allowed to take a few minutes till several hours (or
	 * even days if you are removing millions of documents).
	 * </p>
	 * 
	 * @param documentIds
	 *            the document ids to remove
	 */
	void remove(Iterable<String> documentIds);
}
