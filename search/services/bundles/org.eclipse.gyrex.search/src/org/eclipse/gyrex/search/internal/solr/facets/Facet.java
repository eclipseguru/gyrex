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
package org.eclipse.gyrex.cds.solr.internal.facets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.cds.query.FacetSelectionStrategy;
import org.eclipse.gyrex.cds.query.TermCombination;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.osgi.util.NLS;

/**
 * A {@link IFacet facet}.
 */
public class Facet extends PlatformObject implements IFacet {

	private final String attributeId;
	private final FacetManager manager;
	private final Map<Locale, String> names;
	private TermCombination termCombination;
	private FacetSelectionStrategy selectionStrategy;
	private boolean enabled = true;

	/**
	 * Creates a new instance.
	 * 
	 * @param attributeId
	 * @param manager
	 */
	public Facet(final String attributeId, final FacetManager manager) {
		this(attributeId, manager, null);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param attributeId
	 * @param manager
	 * @param bytes
	 */
	public Facet(final String attributeId, final FacetManager manager, final byte[] bytes) {
		this.attributeId = attributeId;
		this.manager = manager;
		names = new HashMap<Locale, String>(1);
		if (bytes != null) {
			initializeFromByteArray(bytes);
		}
	}

	@Override
	public String getAttributeId() {
		return attributeId;
	}

	@Override
	public IFacetManager getManager() throws IllegalStateException {
		return manager;
	}

	@Override
	public String getName() {
		return getName(Locale.ROOT);
	}

	@Override
	public String getName(final Locale... localeLookupList) {
		for (final Locale locale : localeLookupList) {
			final String name = names.get(locale);
			if (name != null) {
				return name;
			}
		}
		return null;
	}

	@Override
	public Map<Locale, String> getNames() {
		return Collections.unmodifiableMap(names);
	}

	@Override
	public FacetSelectionStrategy getSelectionStrategy() {
		return selectionStrategy;
	}

	@Override
	public TermCombination getTermCombination() {
		return termCombination;
	}

	private void initializeFromByteArray(final byte[] bytes) {
		try {
			final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));

			// names (HashMap is serialize)
			final Map readNames = (Map) in.readObject();
			if (readNames != null) {
				for (final Object entry : readNames.entrySet()) {
					final Object key = ((Entry) entry).getKey();
					final Object value = ((Entry) entry).getValue();
					if ((key instanceof Locale) && (value instanceof String)) {
						names.put((Locale) key, (String) value);
					}
				}
			}

			// enabled
			enabled = in.readBoolean();

			// term combination
			termCombination = (TermCombination) in.readObject();

			// selection strategy
			selectionStrategy = (FacetSelectionStrategy) in.readObject();

		} catch (final Exception e) {
			throw new IllegalStateException(NLS.bind("Error while serializing facet {0}. {1}", attributeId, e.getMessage()), e);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setName(final String name) {
		setName(name, Locale.ROOT);
	}

	@Override
	public void setName(final String name, final Locale locale) {
		if (name != null) {
			if (locale == null) {
				throw new IllegalArgumentException("locale must not be null if a name is provided");
			}
			names.put(locale, name);
		} else {
			if (locale != null) {
				names.remove(locale);
			} else {
				names.clear();
			}
		}

	}

	@Override
	public void setSelectionStrategy(final FacetSelectionStrategy strategy) {
		selectionStrategy = strategy;
	}

	@Override
	public void setTermCombination(final TermCombination combination) {
		termCombination = combination;
	}

	public byte[] toByteArray() {
		// TODO use JSON instead of Java serialization?
		try {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final ObjectOutputStream out = new ObjectOutputStream(bytes);

			// names (HashMap is serialize)
			out.writeObject(names);

			// enabled
			out.writeBoolean(enabled);

			// term combination
			out.writeObject(termCombination);

			// selection strategy
			out.writeObject(selectionStrategy);

			return bytes.toByteArray();
		} catch (final IOException e) {
			throw new IllegalStateException(NLS.bind("Error while serializing facet {0}. {1}", attributeId, e.getMessage()), e);
		}
	}

}
