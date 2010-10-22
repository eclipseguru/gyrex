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
package org.eclipse.gyrex.cds.solr.internal.facets;

import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.cds.solr.ISolrCdsConstants;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.model.common.provider.ModelProvider;
import org.eclipse.gyrex.persistence.context.preferences.ContextPreferencesRepository;
import org.eclipse.gyrex.persistence.storage.Repository;

/**
 * Facets model provider.
 */
public class FacetModelProvider extends ModelProvider {

	/**
	 * Creates a new instance.
	 */
	public FacetModelProvider() {
		super(ISolrCdsConstants.FACET_CONTENT_TYPE, IFacetManager.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.model.common.provider.ModelProvider#createModelManagerInstance(java.lang.Class, org.eclipse.gyrex.persistence.storage.Repository, org.eclipse.gyrex.context.IRuntimeContext)
	 */
	@Override
	public BaseModelManager createModelManagerInstance(final Class modelManagerType, final Repository repository, final IRuntimeContext context) {
		if (IFacetManager.class.equals(modelManagerType) && (repository instanceof ContextPreferencesRepository)) {
			return new FacetManager(context, (ContextPreferencesRepository) repository);
		}
		return null;
	}

}
