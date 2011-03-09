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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.gyrex.model.common.ModelUtil;
import org.eclipse.gyrex.search.ISearchManager;
import org.eclipse.gyrex.search.documents.IDocument;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.query.IQuery;
import org.eclipse.gyrex.search.result.IResult;
import org.eclipse.gyrex.search.result.IResultFacet;
import org.junit.Test;

/**
 * Solr CDS service tests
 */
public class SolrCdsServiceTest extends BaseSolrTest {

	@Test
	public void test001_CdsBasics() throws Exception {
		
		@SuppressWarnings("restriction")
		final ISearchManager docManager = ModelUtil.getManager(ISearchManager.class, getContext());
		
		// init facets (note, requires copyField support in schema)
		final IFacet colorFacet = docManager.createFacet("color");
		colorFacet.setName("Color");
		docManager.saveFacet(colorFacet);

		// publish dummy docs
		final IDocument doc1 = docManager.createDocument();
		final IDocument doc2 = docManager.createDocument();
		doc1.getOrCreate("color").ofType(String.class).add("blue");
		doc2.getOrCreate("color").ofType(String.class).add("red");
		docManager.publishDocuments(Arrays.asList(doc1, doc2));
		waitForPendingSolrPublishOps();

		// query for all
		final IQuery query = docManager.createQuery();
		assertNotNull(query);
		final IResult result = docManager.findByQuery(query);
		assertNotNull(result);

		// check facets
		final Map<String, IResultFacet> facets = result.getFacets();
		assertNotNull(facets);
		assertTrue("facet 'color' is missing", facets.containsKey("color"));
	}
}
