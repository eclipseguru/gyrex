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
package org.eclipse.cloudfree.services.listings.restult;

/**
 * A facet available in a listing result.
 * <p>
 * Facets are used to classify listings in a listing repository. See the
 * Wikipedia article <a
 * href="http://en.wikipedia.org/wiki/Faceted_classification">Faceted
 * classification</a> for further details about the concept of facetting.
 * </p>
 */
public interface IListingResultFacet {

	/**
	 * Returns the id of the facet.
	 * <p>
	 * The facet id uniquely identifies the facet. Typically, this could be the
	 * field name of a field-based facet.
	 * </p>
	 * 
	 * @return the facet id
	 */
	String getId();

	/**
	 * Returns the label for the facet.
	 * <p>
	 * Note, the label is already localized depending on the result's context.
	 * </p>
	 * 
	 * @return the facet label
	 */
	String getLabel();

	/**
	 * Returns the facet values.
	 * 
	 * @return the facet values
	 */
	IListingResultFacetValue[] getValues();

}
