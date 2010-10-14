/*******************************************************************************
 * Copyright (c) 2010 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.facets;

import java.util.Locale;
import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;
import org.eclipse.gyrex.model.common.IModelObject;
import org.eclipse.gyrex.model.common.contracts.IModelManagerAware;

/**
 * A facet is used to implement <a
 * href="http://en.wikipedia.org/wiki/Faceted_search">faceted search</a>.
 * <p>
 * The content delivery
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
	 * Indicates the select strategy used by the facet.
	 * 
	 * @return the select strategy
	 * @see SelectionStrategy
	 */
	SelectionStrategy getSelectionStrategy();

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
	 * Sets the selection strategy that should be used.
	 * 
	 * @param strategy
	 *            the strategy to use
	 * @see SelectionStrategy
	 */
	void setSelectionStrategy(SelectionStrategy strategy);

}
