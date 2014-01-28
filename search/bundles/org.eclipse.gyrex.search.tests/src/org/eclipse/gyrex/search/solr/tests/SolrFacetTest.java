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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Locale;
import java.util.Map;

import org.eclipse.gyrex.search.ISearchManager;
import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.internal.solr.facets.Facet;
import org.eclipse.gyrex.search.query.FacetSelectionStrategy;
import org.eclipse.gyrex.search.query.TermCombination;
import org.junit.Test;

/**
 *
 */
@SuppressWarnings("restriction")
public class SolrFacetTest extends BaseSolrTest {

	/** TEST_FACET */
	private static final String TEST_FACET = "Test Facet";

	@Test
	public void test001_FacetSerialization() throws Exception {
		final Facet facet = new Facet("test", null);
		facet.setName("Test");
		facet.setName("Test de", Locale.GERMAN);
		facet.setName("Test de_DE", Locale.GERMANY);

		// check locale
		assertEquals("Test de_DE", facet.getName(Locale.GERMANY));
		assertEquals("Test de", facet.getName(Locale.GERMAN));
		assertEquals("Test de_DE", facet.getName(Locale.GERMANY, Locale.GERMAN));
		assertEquals("Test", facet.getName(Locale.UK, Locale.ROOT));
		assertNull(facet.getName(Locale.UK));

		// serialize
		final byte[] bs = facet.toByteArray();

		// re-construct
		final Facet facet2 = new Facet("test", null, bs);

		// check properties
		assertEquals(facet.getName(), facet2.getName());
		assertNull(facet2.getSelectionStrategy());
		assertNull(facet2.getTermCombination());

		// check locales
		assertEquals(facet.getName(Locale.GERMANY), facet2.getName(Locale.GERMANY));
		assertEquals(facet.getName(Locale.GERMAN), facet2.getName(Locale.GERMAN));
		assertEquals(facet.getName(Locale.GERMANY, Locale.GERMAN), facet2.getName(Locale.GERMANY, Locale.GERMAN));
		assertEquals(facet.getName(Locale.UK, Locale.ROOT), facet2.getName(Locale.UK, Locale.ROOT));
		assertNull(facet2.getName(Locale.UK));

	}

	@Test
	public void test002_ManagerBasics() throws Exception {
		final ISearchManager manager = getContext().get(ISearchManager.class);
		assertNotNull(manager);

		// try to remove any existing facets
		final Map<String, IFacet> existingFacets = manager.getFacets();
		assertNotNull(existingFacets);
		if (!existingFacets.isEmpty()) {
			for (final IFacet facet : existingFacets.values()) {
				manager.deleteFacet(facet);
				assertNotNull(manager.getFacets());
				assertFalse(manager.getFacets().containsKey(facet.getAttributeId()));
			}
		}

		// check empty
		assertNotNull(manager.getFacets());
		assertTrue(manager.getFacets().isEmpty());

		// create transient facet
		final IFacet facet = manager.createFacet("test");
		assertNotNull(facet);

		// must still be empty
		assertNotNull(manager.getFacets());
		assertFalse(manager.getFacets().containsKey(facet.getAttributeId()));

		// now save facet
		manager.saveFacet(facet);

		// must not be empty anymore
		assertNotNull(manager.getFacets());
		assertEquals(1, manager.getFacets().size());
		assertTrue(manager.getFacets().containsKey(facet.getAttributeId()));
	}

	@Test
	public void test003_FacetPersistence() throws Exception {
		final ISearchManager manager = getContext().get(ISearchManager.class);
		assertNotNull(manager);

		// create facet
		final IFacet facet = manager.createFacet("test");
		assertNotNull(facet);

		facet.setName(TEST_FACET);
		assertEquals(facet.getName(), TEST_FACET);

		facet.setSelectionStrategy(FacetSelectionStrategy.MULTI);
		assertEquals(facet.getSelectionStrategy(), FacetSelectionStrategy.MULTI);

		facet.setTermCombination(TermCombination.AND);
		assertEquals(facet.getTermCombination(), TermCombination.AND);

		// now save facet
		manager.saveFacet(facet);

		// re-load
		final IFacet saveFacet = manager.getFacets().get(facet.getAttributeId());
		assertNotNull(saveFacet);

		// must be different object
		assertNotSame(facet, saveFacet);

		// check properties
		assertEquals(facet.getName(), saveFacet.getName());
		assertEquals(facet.getSelectionStrategy(), saveFacet.getSelectionStrategy());
		assertEquals(facet.getTermCombination(), saveFacet.getTermCombination());
	}
}
