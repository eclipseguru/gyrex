/**
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 */
package org.eclipse.gyrex.search.solr.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.util.Collections;

import org.eclipse.gyrex.search.ISearchManager;
import org.eclipse.gyrex.search.documents.IDocument;

import org.junit.Test;

/**
 * Document manager tests
 */
public class DocumentManagerTest extends BaseSolrTest {

	@Test
	public void test001_ManagerBasics() throws Exception {
		final ISearchManager manager = getContext().get(ISearchManager.class);
		assertNotNull(manager);

		assertNull(manager.findDocumentById("test"));

		final IDocument doc = manager.createDocument();
		assertNotNull(doc);

		doc.setId("test2");
		assertEquals("test2", doc.getId());

		doc.setId("test");
		assertEquals("test", doc.getId());

		manager.publishDocuments(Collections.singleton(doc));
		waitForPendingSolrPublishOps();

		final IDocument doc2 = manager.findDocumentById("test");
		assertNotNull(doc2);
		assertEquals("test", doc2.getId());
	}
}
