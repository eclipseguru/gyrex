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
package org.eclipse.cloudfree.listings.model.solr.internal;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.eclipse.cloudfree.listings.model.solr.ISolrQueryExecutor;
import org.eclipse.cloudfree.persistence.solr.internal.SolrRepository;

/**
 * 
 */
final class SolrQueryExecutor implements ISolrQueryExecutor {

	private final SolrRepository solrRepository;

	/**
	 * Creates a new instance.
	 * 
	 * @param solrListingsManager
	 */
	SolrQueryExecutor(final SolrRepository solrRepository) {
		this.solrRepository = solrRepository;
	}

	private SolrRepository getRepository() {
		return solrRepository;
	}

	@Override
	public QueryResponse query(final SolrQuery query) {
		return getRepository().query(query);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SolrQueryExecutor[ " + solrRepository + " ]";
	}
}