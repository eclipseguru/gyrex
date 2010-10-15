/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.facets;

import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;
import org.eclipse.gyrex.model.common.IModelManager;
import org.eclipse.gyrex.model.common.ModelException;

/**
 * Model manager for managing {@link IFacet facets}.
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
public interface IFacetManager extends IModelManager {

	/**
	 * Creates a transient facet for the specified attribute id.
	 * <p>
	 * The facet will not be contained in the map returned by
	 * {@link #getFacets()} until it has been {@link #save(IFacet) saved}.
	 * </p>
	 * 
	 * @param attributeId
	 *            the id of the {@link IDocumentAttribute attribute} to create
	 *            the facet for
	 * @return a transient facet
	 */
	IFacet create(String attributeId) throws IllegalArgumentException;

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
	void delete(IFacet facet) throws IllegalArgumentException, ModelException;

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
	 */
	Map<String, IFacet> getFacets();

	/**
	 * Saves a facet from the underlying repository.
	 * <p>
	 * If the facet is transient it will be inserted, otherwise it will be saved
	 * overwriting any existing data.
	 * </p>
	 * 
	 * @param facet
	 *            the facet to save
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 * @throws ModelException
	 *             if an error occurred while saving the facet
	 */
	void save(IFacet facet) throws IllegalArgumentException, ModelException;
}
