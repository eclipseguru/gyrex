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
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *******************************************************************************/
package org.eclipse.gyrex.cds;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.cds.internal.solr.SolrCdsActivator;
import org.eclipse.gyrex.cds.internal.solr.facets.Facet;
import org.eclipse.gyrex.cds.internal.solr.facets.FacetManagerMetrics;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.model.common.provider.ModelProvider;
import org.eclipse.gyrex.persistence.context.preferences.ContextPreferencesRepository;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import org.osgi.service.prefs.BackingStoreException;

/**
 * Base Model manager implementation for {@link IFacet facets}.
 * <p>
 * Usually there's no need to override any method in an implementation. This
 * already contains each required functionality to access solr facet
 * repositories.
 * <p>
 * The cause to make it abstract lays in the concept of {@link ModelProvider}
 * and {@link IRuntimeContext}. For each repository access we have to ask the
 * context for a {@link BaseModelManager} implementation which is registered as
 * unique class via model provider.
 * <p>
 * So each solr facet repository needs an own implementation of a model manager
 */
public abstract class BaseFacetManager extends BaseModelManager<ContextPreferencesRepository> implements IFacetManager {

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param repository
	 * @param modelManagerImplementaionId
	 *            must be unique for each context. See
	 *            {@link BaseModelManager#createMetricsId(String, IRuntimeContext, org.eclipse.gyrex.persistence.storage.Repository)}
	 */
	public BaseFacetManager(final IRuntimeContext context, final ContextPreferencesRepository repository, final String modelManagerImplementaionId) {
		super(context, repository, new FacetManagerMetrics(createMetricsId(modelManagerImplementaionId, context, repository), createMetricsDescription("context preferences based facet manager", context, repository)));
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
