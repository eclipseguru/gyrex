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

import java.util.Locale;
import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;
import org.eclipse.gyrex.cds.query.FacetSelectionStrategy;
import org.eclipse.gyrex.cds.query.TermCombination;
import org.eclipse.gyrex.model.common.IModelObject;
import org.eclipse.gyrex.model.common.contracts.IModelManagerAware;

/**
 * A facet is used to implement <a
 * href="http://en.wikipedia.org/wiki/Faceted_search">faceted search</a>.
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
public interface IFacet extends IModelObject, IModelManagerAware<IFacetManager> {

	/**
	 * Returns the id of the {@link IDocumentAttribute attribute} used to
	 * "obtain" facet values.
	 * 
	 * @return the attribute id
	 */
	String getAttributeId();

	/**
	 * Returns the facet name for the default locale.
	 * <p>
	 * This is a convenience method which just calles
	 * {@link #getName(Locale...)} with {@link Locale#ROOT} as the only locale
	 * value. It should be used when locale semantics aren't desired.
	 * </p>
	 * 
	 * @return the facet name for the default locale
	 */
	String getName();

	/**
	 * Returns the facet name matching a particular locale.
	 * <p>
	 * The specified lookup list is iterated and the first match will be
	 * returned.
	 * </p>
	 * 
	 * @param localeLookupList
	 *            the locale lookup list (may not be <code>null</code>; use
	 *            {@link Locale#ROOT} for default locale)
	 * @return the facet name (maybe <code>null</code> if not available)
	 */
	String getName(Locale... localeLookupList);

	/**
	 * Returns a map of all defined names.
	 * 
	 * @return an unmodifiable map of all defined facet names.
	 */
	Map<Locale, String> getNames();

	/**
	 * Returns the default select strategy used by the facet.
	 * 
	 * @return the select strategy (may be <code>null</code> if no default is
	 *         set)
	 * @see #setSelectionStrategy(FacetSelectionStrategy)
	 */
	FacetSelectionStrategy getSelectionStrategy();

	/**
	 * Returns the default term combination used by the facet.
	 * 
	 * @return the default term combination (may be <code>null</code> if no
	 *         default is set)
	 * @see #setTermCombination(TermCombination)
	 */
	TermCombination getTermCombination(TermCombination combination);

	/**
	 * Sets or unsets a human-readable name for the default locale.
	 * <p>
	 * This is a convenience method which just calles
	 * {@link #setName(String, Locale)} with {@link Locale#ROOT} as the locale
	 * value. It should be used when locale semantics aren't desired.
	 * </p>
	 * 
	 * @param name
	 *            the name to set (or <code>null</code> to unset)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 */
	void setName(String name);

	/**
	 * Sets or unsets a human-readable name for a particular locale.
	 * <p>
	 * A name is a short, precise name that is typically used in the UI.
	 * </p>
	 * <p>
	 * Note, although it's possible to specify <code>null</code> for the
	 * <code>locale</code> parameter, callers may only do so when unsetting.
	 * Otherwise the result of this method is undefined.
	 * </p>
	 * 
	 * @param name
	 *            the name to set (or <code>null</code> to unset)
	 * @param locale
	 *            the locale to set the name for (maybe <code>null</code> for
	 *            <em>all</em> locales; use {@link Locale#ROOT} for default
	 *            locale)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 */
	void setName(String name, Locale locale);

	/**
	 * Sets the default selection strategy that should be used.
	 * 
	 * @param strategy
	 *            the strategy to use
	 * @see FacetSelectionStrategy
	 */
	void setSelectionStrategy(FacetSelectionStrategy strategy);

	/**
	 * Sets the default term combination that should be used.
	 * 
	 * @param combination
	 *            the term combination to set
	 * @see TermCombination
	 */
	void setTermCombination(TermCombination combination);
}
