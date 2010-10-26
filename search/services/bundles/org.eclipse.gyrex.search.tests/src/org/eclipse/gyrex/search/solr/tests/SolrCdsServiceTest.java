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
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.gyrex.cds.IContentDeliveryService;
import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentManager;
import org.eclipse.gyrex.cds.facets.IFacet;
import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.cds.query.IQuery;
import org.eclipse.gyrex.cds.result.IResult;
import org.eclipse.gyrex.cds.result.IResultFacet;
import org.eclipse.gyrex.model.common.ModelUtil;

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
		final IContentDeliveryService service = getContext().get(IContentDeliveryService.class);
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
		final IResult result = service.findByQuery(query);
		assertNotNull(result);

		// check facets
		final Map<String, IResultFacet> facets = result.getFacets();
		assertNotNull(facets);
		assertTrue("facet 'color' is missing", facets.containsKey("color"));
	}
}
