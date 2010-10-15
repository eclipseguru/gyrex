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
package org.eclipse.gyrex.cds.query;

import org.eclipse.gyrex.cds.facets.IFacet;

/**
 * A filter that operates using {@link IFacet facets}.
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
public interface IFacetFilter extends IFilter<IFacetFilter> {

	/**
	 * Sets the {@link TermCombination combination} that should be used if
	 * multiple values are set.
	 * 
	 * @param combination
	 *            the combination to set
	 * @return the facet filter for convenience
	 */
	IFacetFilter combineUsing(TermCombination combination);

	/**
	 * Sets the {@link FacetSelectionStrategy selection strategy} that should be
	 * used to influence the behavior of the facets returned in the results.
	 * 
	 * @param strategy
	 * @return the facet filter for convenience
	 */
	IFacetFilter select(FacetSelectionStrategy strategy);

	/**
	 * Sets a single value to select.
	 * <p>
	 * Blank and <code>null</code> values will be ignored.
	 * </p>
	 * <p>
	 * The value <strong>must not</strong> be escaped.
	 * </p>
	 * 
	 * @param value
	 *            the value to select
	 * @return the facet filter for convenience
	 */
	IFacetFilter withValue(String value);

	/**
	 * Sets multiple values to select.
	 * <p>
	 * Blank and <code>null</code> values will be ignored.
	 * </p>
	 * <p>
	 * The values <strong>must not</strong> be escaped.
	 * </p>
	 * 
	 * @param values
	 *            the values to select
	 * @return the facet filter for convenience
	 */
	IFacetFilter withValues(String... values);

}
