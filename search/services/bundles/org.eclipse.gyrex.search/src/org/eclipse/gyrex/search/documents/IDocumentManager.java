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
package org.eclipse.gyrex.cds.documents;

import java.util.Map;

import org.eclipse.gyrex.model.common.IModelManager;

/**
 * The manager for working with {@link IDocument documents}.
 * <p>
 * The document model manager provides a generic way of working with document in
 * a repository.
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
	 * Finds multiple listing by their {@link IDocument#getId() ids}.
	 * 
	 * @param ids
	 *            the listing ids to find
	 * @return an unmodifiable map of found {@link IDocument} with
	 *         {@link IDocument#getId() the listing id} as map key and the
	 *         {@link IDocument} as map value
	 */
	Map<String, IDocument> findById(Iterable<String> ids);

	/**
	 * Finds a listing by its {@link IDocument#getId()}.
	 * 
	 * @param id
	 *            the listing id
	 * @return the found {@link IDocument} or <code>null</code>
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
	void publish(Iterable<IDocument> documents);

	/**
	 * Removes a set of documents from the repository.
	 * <p>
	 * Note, a remove operation is considered to finish asynchronously, i.e.
	 * when this method returns the documents might still be accessible using
	 * the <code>find...</code> methods. Depending on the repository and amount
	 * of input the process is allowed to take a few minutes till several hours
	 * (or even days if you are removing millions of documents).
	 * </p>
	 * 
	 * @param documentIds
	 *            the document ids to remove
	 */
	void remove(Iterable<String> documentIds);
}
