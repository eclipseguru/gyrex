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
 */
package org.eclipse.gyrex.search.solr.tests;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.gyrex.model.common.ModelUtil;
import org.eclipse.gyrex.search.ISearchService;
import org.eclipse.gyrex.search.documents.IDocument;
import org.eclipse.gyrex.search.documents.IDocumentManager;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.facets.IFacetManager;
import org.eclipse.gyrex.search.query.IQuery;
import org.eclipse.gyrex.search.result.IResult;
import org.eclipse.gyrex.search.result.IResultFacet;

import org.junit.Test;

/**
 * Solr CDS service tests
 */
public class SolrCdsServiceTest extends BaseSolrTest {

	@Override
	protected void initContext() throws Exception {
		// super
		super.initContext();

		// facet manager as well
		FacetManagerTest.initFacetManager(getContext());
	}

	@Test
	public void test001_CdsBasics() throws Exception {
		final ISearchService service = getContext().get(ISearchService.class);
		assertNotNull(service);

		// init facets (note, requires copyField support in schema)
		final IFacetManager facetManager = ModelUtil.getManager(IFacetManager.class, getContext());
		final IFacet colorFacet = facetManager.create("color");
		colorFacet.setName("Color");
		facetManager.save(colorFacet);

		// publish dummy docs
		final IDocumentManager docManager = ModelUtil.getManager(IDocumentManager.class, getContext());
		final IDocument doc1 = docManager.createDocument();
		final IDocument doc2 = docManager.createDocument();
		doc1.getOrCreate("color").ofType(String.class).add("blue");
		doc2.getOrCreate("color").ofType(String.class).add("red");
		docManager.publish(Arrays.asList(doc1, doc2));
		waitForPendingSolrPublishOps();

		// query for all
		final IQuery query = service.createQuery();
		assertNotNull(query);
		final IResult result = service.findByQuery(query, docManager, facetManager);
		assertNotNull(result);

		// check facets
		final Map<String, IResultFacet> facets = result.getFacets();
		assertNotNull(facets);
		assertTrue("facet 'color' is missing", facets.containsKey("color"));
	}
}
