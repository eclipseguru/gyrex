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
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentAttribute;
import org.eclipse.gyrex.cds.solr.internal.SolrCdsActivator;
import org.eclipse.gyrex.common.status.BundleStatusUtil;
import org.eclipse.gyrex.monitoring.metrics.ThroughputMetric;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 */
public class PublishJob extends Job {

	public static final Object FAMILY = new Object();

	private final Iterable<IDocument> documents;
	private final SolrServer solrServer;
	private final SolrDocumentManagerMetrics solrListingsManagerMetrics;
	private final boolean commit;

	public PublishJob(final Iterable<IDocument> documents, final SolrServer solrServer, final SolrDocumentManagerMetrics solrListingsManagerMetrics, final boolean commit) {
		super("Solr Document Publish");
		this.documents = documents;
		this.solrServer = solrServer;
		this.solrListingsManagerMetrics = solrListingsManagerMetrics;
		this.commit = commit;
		setSystem(true);
		setPriority(LONG);
	}

	@Override
	public boolean belongsTo(final Object family) {
		return FAMILY == family;
	}

	private SolrInputDocument createSolrDoc(final IDocument document) {
		final SolrInputDocument solrDoc = new SolrInputDocument();
		final Collection<IDocumentAttribute> attributes = document.getAttributes().values();
		for (final IDocumentAttribute attr : attributes) {
			final Collection<?> values = attr.getValues();
			for (final Object value : values) {
				solrDoc.addField(attr.getId(), value);
			}
		}
		return solrDoc;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		// check if we are active
		BundleStatusUtil statusUtil;
		try {
			statusUtil = SolrCdsActivator.getInstance().getStatusUtil();
		} catch (final IllegalStateException e) {
			return Status.CANCEL_STATUS;
		}
		// collect stats
		final ThroughputMetric publishedMetric = solrListingsManagerMetrics.getDocsPublishedMetric();
		final long requestStarted = publishedMetric.requestStarted();

		// create solr docs
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		for (final IDocument document : documents) {
			docs.add(createSolrDoc(document));
		}
		try {
			// add to repository
			if (commit) {
//				final UpdateRequest req = new UpdateRequest();
//				req.add(docs);
//				req.setCommitWithin((int) TimeUnit.MINUTES.toMillis(3)); // TODO: should be configurable
//				req.process(solrServer);
				solrServer.add(docs);
				solrServer.commit();
			} else {
				solrServer.add(docs);
			}
			publishedMetric.requestFinished(docs.size(), System.currentTimeMillis() - requestStarted);
		} catch (final Exception e) {
			publishedMetric.requestFailed();
			return statusUtil.createError(1, "error while submitting documents to Solr", e);
		}
		return Status.OK_STATUS;
	}

}
