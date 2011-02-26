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
package org.eclipse.gyrex.cds.documents;

import java.util.Map;

import org.eclipse.gyrex.model.common.IModelManager;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

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
 * Clients may not extend this interface directly. If specialization is desired
 * they should look at the options provided by the model implementation.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDocumentManager extends IModelManager {

	/**
	 * Commits everything to the underlying Solr repository.
	 * 
	 * @param collection
	 *            the collection (maybe <code>null</code> for the default
	 *            collection)
	 * @param waitFlush
	 *            <code>true</code> if the method should block till all changes
	 *            have been committed, <code>false</code> otherwise
	 * @param waitSearcher
	 *            <code>true</code> if the method should block till new
	 *            searchers have been opened after committing,
	 *            <code>false</code> otherwise
	 */
	void commit(boolean waitFlush, boolean waitSearcher);

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
	 * Optimizes and commits everything to the underlying Solr repository.
	 * 
	 * @param collection
	 *            the collection (maybe <code>null</code> for the default
	 *            collection)
	 * @param waitFlush
	 *            <code>true</code> if the method should block till all changes
	 *            have been committed, <code>false</code> otherwise
	 * @param waitSearcher
	 *            <code>true</code> if the method should block till new
	 *            searchers have been opened after committing,
	 *            <code>false</code> otherwise
	 */
	void optimize(boolean waitFlush, boolean waitSearcher);

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
	 * Executes a SolrJ query.
	 * <p>
	 * Note, this API depends on the SolrJ and Solr API. Thus, it is bound to
	 * the evolution of external API which might not follow the Gyrex <a
	 * href="http://wiki.eclipse.org/Evolving_Java-based_APIs"
	 * target="_blank">API evolution</a> and <a
	 * href="http://wiki.eclipse.org/Version_Numbering"
	 * target="_blank">versioning</a> guidelines.
	 * </p>
	 * 
	 * @param query
	 *            the <code>SolrQuery</code> object
	 * @return the <code>QueryResponse</code> object
	 */
	QueryResponse query(SolrQuery query);

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

	/**
	 * Allows to temporarily disabled commits from the manager.
	 * <p>
	 * When disabled, the manager will never commit any changes
	 * {@link IDocumentManager#publish(Iterable) submitted} to the underlying
	 * Solr repository. Instead, {@link #commit(boolean, boolean)} must be
	 * called manually in order to apply changes to the Solr repository.
	 * </p>
	 * 
	 * @param collection
	 *            the collection (maybe <code>null</code> for the default
	 *            collection)
	 * @param enabled
	 *            <code>true</code> if the manager is allowed to commit changes,
	 *            <code>false</code> otherwise
	 * @return <code>true</code> if commit was previously enabled,
	 *         <code>false</code> otherwise
	 */
	boolean setCommitsEnabled(boolean enabled);

}
