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
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 *******************************************************************************/
package org.eclipse.gyrex.search;

import java.util.Collection;
import java.util.Map;

import org.eclipse.gyrex.model.common.IModelManager;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.search.documents.IDocument;
import org.eclipse.gyrex.search.documents.IDocumentAttribute;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.query.IQuery;
import org.eclipse.gyrex.search.result.IResult;

/**
 * The manager for working with {@link IDocument documents}, {@link IFacet
 * facets} and {@link IDocumentCollection collections}. It also provides an API
 * to search {@link IDocument documents}
 * <p>
 * The document model manager provides a generic way of working with documents
 * and facets in a repository.
 * </p>
 * <p>
 * This interface must be implemented by contributors of a search model
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
public interface ISearchManager extends IModelManager {
	/**
	 * Creates and returns a new transient document.
	 * <p>
	 * The document will not be contained in the repository until it has been
	 * {@link #publishDocuments(Collection) published}.
	 * </p>
	 * 
	 * @param attributeId
	 *            the id of the {@link IDocumentAttribute attribute} to create
	 *            the facet for
	 * @return a transient facet
	 */
	IDocument createDocument();

	/**
	 * Creates a transient facet for the specified attribute id.
	 * <p>
	 * The facet will not be contained in the map returned by
	 * {@link #getFacets()} until it has been {@link #saveFacet(IFacet) saved}.
	 * </p>
	 * 
	 * @param attributeId
	 *            the id of the {@link IDocumentAttribute attribute} to create
	 *            the facet for
	 * @return a transient facet
	 */
	IFacet createFacet(String attributeId) throws IllegalArgumentException;

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
	 * Deletes a facet from the underlying repository.
	 * 
	 * @param facet
	 *            the facet to delete
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 * @throws ModelException
	 *             if an error occurred while deleting the facet
	 */
	void deleteFacet(IFacet facet) throws IllegalArgumentException, ModelException;

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
	 * Finds a document by its {@link IDocument#getId()}.
	 * 
	 * @param id
	 *            the listing id
	 * @return the found {@link IDocument} or <code>null</code> if not found
	 */
	IDocument findDocumentById(String id);

	/**
	 * Finds multiple documents by their {@link IDocument#getId() ids}.
	 * 
	 * @param ids
	 *            the listing ids to find
	 * @return an unmodifiable map of found {@link IDocument} with
	 *         {@link IDocument#getId() the listing id} as map key and the
	 *         {@link IDocument} as map value
	 */
	Map<String, IDocument> findDocumentsById(Collection<String> ids);

	/**
	 * Loads and returns a map of all facets.
	 * <p>
	 * The returned map will not contain any created transient facet. It will be
	 * loaded from the underling repository all the time. Thus, clients may not
	 * call that method too often (eg. on every search request) but keep an
	 * instance of facets around for a longer period of time.
	 * </p>
	 * <p>
	 * Modifications to facets in the underlying repository will not update the
	 * returned map. Clients need to get a new map in order to <em>see</em>
	 * those modifications.
	 * </p>
	 * 
	 * @return an unmodifiable map of all facets with
	 *         {@link IFacet#getAttributeId() the attribute id} as the map key
	 *         and the {@link IFacet facet} as the value
	 * @throws ModelException
	 *             if an error occurred while loading the facet
	 */
	Map<String, IFacet> getFacets() throws ModelException;

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
	void publishDocuments(Collection<IDocument> documents);

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
	void removeDocuments(Collection<String> documentIds);

	/**
	 * Saves a facet from the underlying repository.
	 * <p>
	 * If the facet does not exist it will be inserted, otherwise it will be
	 * saved overwriting any existing data.
	 * </p>
	 * 
	 * @param facet
	 *            the facet to save
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 * @throws ModelException
	 *             if an error occurred while saving the facet
	 */
	void saveFacet(IFacet facet) throws IllegalArgumentException, ModelException;
}
