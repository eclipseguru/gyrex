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
package org.eclipse.gyrex.cds.solr.internal.facets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.cds.solr.internal.SolrCdsActivator;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.persistence.context.preferences.ContextPreferencesRepository;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import org.osgi.service.prefs.BackingStoreException;

/**
 * Model manager implementation for {@link IFacet facets}.
 */
public class FacetManager extends BaseModelManager<ContextPreferencesRepository> implements IFacetManager {

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param repository
	 */
	FacetManager(final IRuntimeContext context, final ContextPreferencesRepository repository) {
		super(context, repository, new FacetManagerMetrics(createMetricsId(SolrCdsActivator.SYMBOLIC_NAME + ".model.facets", context, repository), createMetricsDescription("context preferences based facet manager", context, repository)));
	}

	private void checkFacet(final IFacet facet) {
		if (facet == null) {
			throw new IllegalArgumentException("facet must not be null");
		}
		if (!(facet instanceof Facet)) {
			throw new IllegalArgumentException(NLS.bind("facet type {0} not supported by this manager", facet.getClass()));
		}
	}

	@Override
	public IFacet create(final String attributeId) throws IllegalArgumentException {
		return new Facet(attributeId, this);
	}

	@Override
	public void delete(final IFacet facet) throws IllegalArgumentException, ModelException {
		checkFacet(facet);
		try {
			getRepository().remove(facet.getAttributeId());
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SolrCdsActivator.SYMBOLIC_NAME, "Unable to remove facet. " + e.getMessage(), e));
		}
	}

	@Override
	public Map<String, IFacet> getFacets() throws ModelException {
		try {
			final Collection<String> keys = getRepository().getKeys();
			final Map<String, IFacet> map = new HashMap<String, IFacet>(keys.size());
			for (final String key : keys) {
				final byte[] bytes = getRepository().get(key);
				if (bytes != null) {
					map.put(key, new Facet(key, this, bytes));
				}
			}
			return Collections.unmodifiableMap(map);
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SolrCdsActivator.SYMBOLIC_NAME, "Unable to load facets. " + e.getMessage(), e));
		}
	}

	@Override
	public void save(final IFacet facet) throws IllegalArgumentException, ModelException {
		checkFacet(facet);
		try {
			getRepository().store(facet.getAttributeId(), ((Facet) facet).toByteArray());
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SolrCdsActivator.SYMBOLIC_NAME, "Unable to remove facet. " + e.getMessage(), e));
		}
	}

}
