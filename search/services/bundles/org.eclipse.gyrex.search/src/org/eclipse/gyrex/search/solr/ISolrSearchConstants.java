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
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 *******************************************************************************/
package org.eclipse.gyrex.search.solr;

import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;
import org.eclipse.gyrex.search.ISearchManager;

/**
 * Interface with shared constants of the Solr based search implementation.
 */
public interface ISolrSearchConstants {

	/**
	 * The {@link RepositoryContentType content type} required for
	 * {@link ISearchManager document model implementation}.
	 */
	RepositoryContentType SEARCH_CONTENT_TYPE = new RepositoryContentType("application", "x-gyrex-search-solr", SolrServerRepository.TYPE_NAME, "1.0");
}
