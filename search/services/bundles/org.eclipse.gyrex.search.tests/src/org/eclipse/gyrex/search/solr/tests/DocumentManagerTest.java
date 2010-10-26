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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.util.Collections;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentManager;
import org.eclipse.gyrex.cds.solr.internal.documents.PublishJob;

import org.eclipse.core.runtime.jobs.Job;

import org.junit.Test;

/**
 * Document manager tests
 */
public class DocumentManagerTest extends BaseSolrTest {

	@Test
	public void test001_ManagerBasics() throws Exception {
		final IDocumentManager manager = getContext().get(IDocumentManager.class);
		assertNotNull(manager);

		assertNull(manager.findById("test"));

		final IDocument doc = manager.createDocument();
		assertNotNull(doc);

		doc.setId("test2");
		assertEquals("test2", doc.getId());

		doc.setId("test");
		assertEquals("test", doc.getId());

		manager.publish(Collections.singleton(doc));
		Job.getJobManager().join(PublishJob.FAMILY, null);

		final IDocument doc2 = manager.findById("test");
		assertNotNull(doc2);
		assertEquals("test", doc2.getId());
	}
}
