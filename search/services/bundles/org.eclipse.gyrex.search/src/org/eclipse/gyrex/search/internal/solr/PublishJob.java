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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.apache.solr.common.SolrInputDocument;
import org.eclipse.cloudfree.common.status.BundleStatusUtil;
import org.eclipse.cloudfree.listings.model.documents.Document;
import org.eclipse.cloudfree.listings.model.documents.Field;
import org.eclipse.cloudfree.monitoring.metrics.ThroughputMetric;
import org.eclipse.cloudfree.persistence.solr.internal.SolrRepository;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 */
public class PublishJob extends Job {

	private final Iterable<Document> documents;
	private final SolrRepository solrRepository;
	private final SolrListingsManagerMetrics solrListingsManagerMetrics;

	public PublishJob(final Iterable<Document> documents, final SolrRepository solrRepository, final SolrListingsManagerMetrics solrListingsManagerMetrics) {
		super("Solr Document Publish");
		this.documents = documents;
		this.solrRepository = solrRepository;
		this.solrListingsManagerMetrics = solrListingsManagerMetrics;
	}

	private SolrInputDocument createSolrDoc(final Document document) {
		final SolrInputDocument solrDoc = new SolrInputDocument();
		final Collection<Field<?>> fields = document.getFields();
		for (final Field<?> field : fields) {
			final Collection<?> values = field.getValues();
			for (final Object value : values) {
				solrDoc.addField(field.getName(), value);
			}
		}
		return solrDoc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		// check if we are active
		BundleStatusUtil statusUtil;
		try {
			statusUtil = ListingsSolrModelActivator.getInstance().getStatusUtil();
		} catch (final IllegalStateException e) {
			return Status.CANCEL_STATUS;
		}
		// collect stats
		final ThroughputMetric publishedMetric = solrListingsManagerMetrics.getDocsPublishedMetric();
		final long requestStarted = publishedMetric.requestStarted();

		// create solr docs
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		for (final Document document : documents) {
			docs.add(createSolrDoc(document));
		}
		try {
			// add to repository
			solrRepository.add(docs);
			// commit
			solrRepository.commit();
			publishedMetric.requestFinished(docs.size(), System.currentTimeMillis() - requestStarted);
		} catch (final Exception e) {
			publishedMetric.requestFailed();
			return statusUtil.createError(1, "error while submitting documents to Solr", e);
		}
		return Status.OK_STATUS;
	}

}
