/*******************************************************************************
 * Copyright (c) 2008, 2011 Gunnar Wagenknecht and others.
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentCollection;
import org.eclipse.gyrex.cds.documents.IDocumentManager;
import org.eclipse.gyrex.cds.solr.documents.ISolrDocumentCollection;
import org.eclipse.gyrex.cds.solr.internal.SolrCdsActivator;
import org.eclipse.gyrex.cds.solr.solrj.ISolrQueryExecutor;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.exceptions.ObjectNotFoundException;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;

import org.eclipse.osgi.util.NLS;

import org.apache.solr.client.solrj.SolrServer;

/**
 * {@link IDocumentManager} implementation based on Apache Solr.
 */
public class SolrDocumentManager extends BaseModelManager<org.eclipse.gyrex.persistence.solr.SolrServerRepository> implements IDocumentManager {

	private final ConcurrentMap<String, SolrDocumentCollection> collections = new ConcurrentHashMap<String, SolrDocumentCollection>();

	/**
	 * Creates a new instance.
	 * 
	 * @param context
	 *            the context
	 * @param repository
	 *            the repository
	 */
	protected SolrDocumentManager(final IRuntimeContext context, final SolrServerRepository repository) {
		super(context, repository, new SolrDocumentManagerMetrics(createMetricsId(SolrCdsActivator.SYMBOLIC_NAME + ".model.documents", context, repository), createMetricsDescription("Solr based document manager", context, repository)));
	}

	@Override
	public IDocument createDocument() {
		return new TransientDocument();
	}

	@Override
	protected void doClose() {
		collections.clear();
	}

	@Override
	public final Object getAdapter(final Class adapter) {
		if (ISolrDocumentCollection.class.equals(adapter)) {
			throw new IllegalStateException("Please use IDocumentManager#getCollection(String) to obtain a collection and call the IDocumentCollection#getAdapter to obtain an ISolrDocumentCollection!");
		}
		if (ISolrQueryExecutor.class.equals(adapter)) {
			throw new IllegalStateException("Please use IDocumentManager#getCollection(String) to obtain a collection and call the IDocumentCollection#getAdapter to obtain an ISolrQueryExecutor!");
		}
		return super.getAdapter(adapter);
	}

	@Override
	public IDocumentCollection getCollection(final String collection) throws ObjectNotFoundException {
		if (isClosed()) {
			throw new IllegalStateException("closed");
		}

		// lazy init
		if (!collections.containsKey(collection)) {
			try {
				final SolrServer writeServer = getRepository().getSolrServer(collection);
				final SolrServer queryServer = getRepository().getSolrServerOptimizedForQuery(collection);
				collections.putIfAbsent(collection, new SolrDocumentCollection(collection, this, writeServer, queryServer));
			} catch (final IllegalArgumentException e) {
				collections.put(collection, null);
			}
		}

		// get
		final SolrDocumentCollection solrDocumentCollection = collections.get(collection);
		if (null == solrDocumentCollection) {
			throw new ObjectNotFoundException(NLS.bind("Collection {0} not configured in repository {1}", collection, getRepository().getRepositoryId()));
		}

		return solrDocumentCollection;
	}

	protected String getRepositoryId() {
		return getRepository().getRepositoryId();
	}

	SolrDocumentManagerMetrics getSolrListingsManagerMetrics() {
		return (SolrDocumentManagerMetrics) getMetrics();
	}

}
