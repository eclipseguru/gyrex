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
package org.eclipse.gyrex.cds.result;

import java.util.Map;

import org.eclipse.gyrex.cds.facets.IFacet;

/**
 * A facet available in a listing result.
 * <p>
 * Facets are used to classify listings in a listing repository. See the
 * Wikipedia article <a
 * href="http://en.wikipedia.org/wiki/Faceted_classification">Faceted
 * classification</a> for further details about the concept of facetting.
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
public interface IResultFacet {

	/**
	 * Returns the underlying facet.
	 * 
	 * @return the underlying facet
	 */
	IFacet getFacet();

	/**
	 * Returns a map of values available in the facet result.
	 * 
	 * @return an unmodifiable map of all values in the facet result with
	 *         {@link IResultFacetValue#getValue() the value string} as the map
	 *         key and {@link IResultFacetValue the value object} as the value
	 */
	Map<String, IResultFacetValue> getValues();

}
