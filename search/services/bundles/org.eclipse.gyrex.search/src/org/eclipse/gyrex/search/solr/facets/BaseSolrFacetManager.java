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
package org.eclipse.gyrex.search.solr.facets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelException;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.RepositoryMetadata;
import org.eclipse.gyrex.search.documents.IDocumentManager;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.facets.IFacetManager;
import org.eclipse.gyrex.search.internal.SearchActivator;
import org.eclipse.gyrex.search.internal.solr.facets.Facet;
import org.eclipse.gyrex.search.internal.solr.facets.FacetManagerMetrics;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import org.osgi.service.prefs.BackingStoreException;

/**
 * Base class for {@link IFacetManager facet managers} based on Apache Solr.
 * <p>
 * This implementation uses a {@link SolrServerRepository Solr repository} for
 * accessing Solr. Any custom content type definitions need to be bound to such
 * a repository. Typically, the same repository is used that the document
 * manager uses. The facet configuration will be stored as metadata within the
 * repository.
 * </p>
 * <p>
 * Clients that want to contribute a specialize document model backed by Solr
 * may subclass this class. This class implements {@link IDocumentManager} and
 * shields subclasses from API evolutions.
 * </p>
 */
public abstract class BaseSolrFacetManager extends BaseModelManager<SolrServerRepository> implements IFacetManager {

	/**
	 * Creates a new instance.
	 *
	 * @param context
	 * @param repository
	 * @param modelManagerImplementaionId
	 *            must be unique for each context. See
	 *            {@link BaseModelManager#createMetricsId(String, IRuntimeContext, org.eclipse.gyrex.persistence.storage.Repository)}
	 */
	public BaseSolrFacetManager(final IRuntimeContext context, final SolrServerRepository repository, final String modelManagerImplementaionId) {
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
			RepositoryMetadata facetsMetadata = getFacetsMetadata();
			facetsMetadata.remove(facet.getAttributeId());
			facetsMetadata.flush();
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SearchActivator.SYMBOLIC_NAME, "Unable to remove facet. " + e.getMessage(), e));
		}
	}

	private RepositoryMetadata getFacetsMetadata() {
		return getRepository().getMetadata("facets");
	}

	@Override
	public Map<String, IFacet> getFacets() throws ModelException {
		try {
			RepositoryMetadata facetsMetadata = getFacetsMetadata();
			final Collection<String> keys = facetsMetadata.getKeys();
			final Map<String, IFacet> map = new HashMap<String, IFacet>(keys.size());
			for (final String key : keys) {
				final byte[] bytes = facetsMetadata.get(key);
				if (bytes != null) {
					map.put(key, new Facet(key, this, bytes));
				}
			}
			return Collections.unmodifiableMap(map);
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SearchActivator.SYMBOLIC_NAME, "Unable to load facets. " + e.getMessage(), e));
		}
	}

	@Override
	public void save(final IFacet facet) throws IllegalArgumentException, ModelException {
		checkFacet(facet);
		try {
			RepositoryMetadata facetsMetadata = getFacetsMetadata();
			facetsMetadata.put(facet.getAttributeId(), ((Facet) facet).toByteArray());
			facetsMetadata.flush();
		} catch (final BackingStoreException e) {
			throw new ModelException(new Status(IStatus.ERROR, SearchActivator.SYMBOLIC_NAME, "Unable to remove facet. " + e.getMessage(), e));
		}
	}

}
