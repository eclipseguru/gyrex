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
package org.eclipse.gyrex.cds.service.solr.internal;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import org.eclipse.gyrex.cds.IListingManager;
import org.eclipse.gyrex.cds.IListingService;
import org.eclipse.gyrex.cds.model.solr.ISolrQueryExecutor;
import org.eclipse.gyrex.cds.query.ListingQuery;
import org.eclipse.gyrex.cds.result.IListingResult;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.ModelUtil;
import org.eclipse.gyrex.services.common.provider.BaseService;
import org.eclipse.gyrex.services.common.status.IStatusMonitor;

/**
 * Solr based {@link IListingService} implementation.
 */
public class SolrListingService extends BaseService implements IListingService {

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 * @param statusMonitor
	 * @param metrics
	 */
	protected SolrListingService(final IRuntimeContext context, final IStatusMonitor statusMonitor) {
		super(context, statusMonitor, new SolrListingServiceMetrics(createMetricsId("org.eclipse.gyrex.cds.service.solr", context)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.IListingService#findListings(org.eclipse.gyrex.cds.service.query.ListingQuery)
	 */
	@Override
	public IListingResult findListings(final ListingQuery query) {
		final IListingManager manager = ModelUtil.getManager(IListingManager.class, getContext());

		final ISolrQueryExecutor queryExecutor = (ISolrQueryExecutor) manager.getAdapter(ISolrQueryExecutor.class);
		if (null == queryExecutor) {
			throw new IllegalStateException("The context listing manager is not a Solr based listing manager.");
		}

		// create query
		final SolrQuery solrQuery = SolrQueryJob.createSolrQuery(query, getContext());
		final QueryResponse response = queryExecutor.query(solrQuery);
		return new SolrListingResult(getContext(), query, response);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.IListingService#findListings(org.eclipse.gyrex.cds.service.query.ListingQuery, org.eclipse.gyrex.cds.service.result.IListingResultCallback)
	 */
	//	@Override
	//	public Future<IListingResult> findListings(final ListingQuery query, final IListingResultCallback callback) {
	//		final IListingManager manager = ModelUtil.getManager(IListingManager.class, getContext());
	//
	//		final ISolrQueryExecutor queryExecutor = (ISolrQueryExecutor) manager.getAdapter(ISolrQueryExecutor.class);
	//		if (null == queryExecutor) {
	//			throw new IllegalStateException("The context listing manager is not a Solr based listing manager.");
	//		}
	//
	//		// create job
	//		final SolrQueryJob solrQueryJob = new SolrQueryJob(query, queryExecutor, getContext());
	//
	//		// create future for retrieving the value from the job
	//		final SolrListingFuture solrListingFuture = new SolrListingFuture(solrQueryJob);
	//
	//		// add callback
	//		if (null != callback) {
	//			solrListingFuture.setCallback(callback);
	//		}
	//
	//		// now start the job
	//		solrQueryJob.schedule();
	//
	//		// return
	//		return solrListingFuture;
	//	}
}
