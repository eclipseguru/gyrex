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
package org.eclipse.gyrex.cds.solr.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.gyrex.cds.documents.Document;
import org.eclipse.gyrex.cds.documents.Field;
import org.eclipse.gyrex.common.status.BundleStatusUtil;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;
import org.eclipse.gyrex.persistence.solr.internal.SolrRepository;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.apache.solr.common.SolrInputDocument;

/**
 *
 */
public class PublishJob extends Job {

	private final Iterable<Document> documents;
	private final SolrRepository solrRepository;
	private final SolrListingsManagerMetrics solrListingsManagerMetrics;
	private final boolean commit;

	public PublishJob(final Iterable<Document> documents, final SolrRepository solrRepository, final SolrListingsManagerMetrics solrListingsManagerMetrics, final boolean commit) {
		super("Solr Document Publish");
		this.documents = documents;
		this.solrRepository = solrRepository;
		this.solrListingsManagerMetrics = solrListingsManagerMetrics;
		this.commit = commit;
		setSystem(true);
		setPriority(LONG);
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
			if (commit) {
				solrRepository.add(docs, (int) TimeUnit.MILLISECONDS.toMillis(3)); // TODO should be configurable
			} else {
				solrRepository.add(docs); // TODO should be configurable
			}
			publishedMetric.requestFinished(docs.size(), System.currentTimeMillis() - requestStarted);
		} catch (final Exception e) {
			publishedMetric.requestFailed();
			return statusUtil.createError(1, "error while submitting documents to Solr", e);
		}
		return Status.OK_STATUS;
	}

}
