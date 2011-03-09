/*******************************************************************************
 * Copyright (c) 2008, 2010 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 *******************************************************************************/
package org.eclipse.gyrex.search.internal.solr;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.model.common.provider.ModelProvider;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.Repository;
import org.eclipse.gyrex.search.ISearchManager;
import org.eclipse.gyrex.search.solr.ISolrSearchConstants;

/**
 * Solr based CDS model provider.
 */
public class SolrSearchModelProvider extends ModelProvider {

	/**
	 * Creates a new instance.
	 */
	public SolrSearchModelProvider() {
		super(ISolrSearchConstants.SEARCH_CONTENT_TYPE, ISearchManager.class);
	}

	@Override
	public BaseModelManager<? extends SolrServerRepository> createModelManagerInstance(final Class modelManagerType, final Repository repository, final IRuntimeContext context) {
		if (ISearchManager.class.equals(modelManagerType) && (repository instanceof SolrServerRepository)) {
			return new SolrSearchManager(context, (SolrServerRepository) repository);
		}
		return null;
	}

}
