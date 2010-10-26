/**
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.cds.solr.tests;

import static junit.framework.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.eclipse.gyrex.cds.solr.ISolrCdsConstants;
import org.eclipse.gyrex.cds.solr.internal.documents.PublishJob;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.tests.internal.BaseContextTest;
import org.eclipse.gyrex.persistence.PersistenceUtil;
import org.eclipse.gyrex.persistence.internal.storage.DefaultRepositoryLookupStrategy;
import org.eclipse.gyrex.persistence.solr.ISolrRepositoryConstants;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.solr.SolrServerType;
import org.eclipse.gyrex.persistence.solr.internal.SolrActivator;
import org.eclipse.gyrex.persistence.storage.settings.IRepositoryPreferences;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.junit.BeforeClass;

/**
 * Base class for Solr tests.
 */
@SuppressWarnings("restriction")
public abstract class BaseSolrTest extends BaseContextTest {

	protected static final String TEST_REPO_ID = BaseSolrTest.class.getSimpleName().toLowerCase();

	static void initDocumentManager(final IRuntimeContext context) throws BackingStoreException, IOException, SolrServerException {
		DefaultRepositoryLookupStrategy.setRepository(context, ISolrCdsConstants.DOCUMENT_CONTENT_TYPE, TEST_REPO_ID);
		IRepositoryPreferences preferences;
		try {
			preferences = SolrCdsTestsActivator.getInstance().getRepositoryRegistry().createRepository(TEST_REPO_ID, ISolrRepositoryConstants.PROVIDER_ID);
		} catch (final IllegalStateException e) {
			// assume already exist
			preferences = SolrCdsTestsActivator.getInstance().getRepositoryRegistry().getRepositoryPreferences(TEST_REPO_ID);
		}
		assertNotNull(preferences);
		preferences.getPreferences().put(ISolrRepositoryConstants.PREF_KEY_SERVER_TYPE, SolrServerType.EMBEDDED.name());
		preferences.flush();

		// create Solr index

		// empty repo
		final SolrServerRepository repo = (SolrServerRepository) PersistenceUtil.getRepository(context, ISolrCdsConstants.DOCUMENT_CONTENT_TYPE);
		repo.getSolrServer().deleteByQuery("*:*");
		repo.getSolrServer().commit();
	}

	@BeforeClass
	public static void setupSolrIndex() throws Exception {
		// the configuration template
		final File configTemplate = new File(FileLocator.toFileURL(SolrCdsTestsActivator.getInstance().getBundle().getEntry("conf-solr")).getFile());

		// create Solr instance directory
		final File indexDir = SolrActivator.getInstance().getEmbeddedSolrCoreBase(TEST_REPO_ID);
		if (!indexDir.isDirectory()) {
			// initialize dir
			indexDir.mkdirs();
		}

		// copy config & schema
		FileUtils.copyDirectory(configTemplate, indexDir);

		// create core or reload containers
		final CoreContainer coreContainer = SolrActivator.getInstance().getEmbeddedCoreContainer();
		if (null == coreContainer) {
			throw new IllegalStateException("no coreContainer");
		}
		final SolrCore core = coreContainer.getCore(TEST_REPO_ID);
		try {
			if (null == core) {
				// there should be an "admin" core for such requests
				final EmbeddedSolrServer adminServer = new EmbeddedSolrServer(coreContainer, "admin");
				CoreAdminRequest.createCore(TEST_REPO_ID, TEST_REPO_ID, adminServer);
			} else {
				coreContainer.reload(TEST_REPO_ID);
			}
		} finally {
			if (null != core) {
				core.close();
			}
		}
	}

	static void waitForPendingSolrPublishOps() {
		try {
			Job.getJobManager().join(PublishJob.FAMILY, null);
		} catch (final OperationCanceledException e) {
			// ignore
		} catch (final InterruptedException e) {
			// ok
			Thread.currentThread().interrupt();
		}
	}

	@Override
	protected IPath getPrimaryTestContextPath() {
		return new Path("/__internal/org/eclipse/gyrex/cds/solr/tests");
	}

	@Override
	protected void initContext() throws Exception {
		final IRuntimeContext context = getContext();
		initDocumentManager(context);
	}
}
