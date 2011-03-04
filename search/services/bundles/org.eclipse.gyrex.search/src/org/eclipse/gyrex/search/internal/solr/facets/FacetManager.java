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
package org.eclipse.gyrex.search.internal.solr.facets;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.search.internal.SearchActivator;
import org.eclipse.gyrex.search.solr.facets.BaseSolrFacetManager;

/**
 * A default {@link BaseSolrFacetManager} that can be used out of the box.
 */
public class FacetManager extends BaseSolrFacetManager {

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param repository
	 */
	FacetManager(final IRuntimeContext context, final SolrServerRepository repository) {
		super(context, repository, SearchActivator.SYMBOLIC_NAME + ".solr.model.facets.metrics");
	}
}
