/*******************************************************************************
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - merged IDocumentManager, IFacetManager and ISearchService (https://bugs.eclipse.org/bugs/show_bug.cgi?id=339327)
 *******************************************************************************/
package org.eclipse.gyrex.search.solr.tests;

import org.eclipse.gyrex.junit.GyrexServerResource;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Context Test Suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ SolrFacetTest.class, DocumentManagerTest.class, SolrCdsServiceTest.class })
public class AllSolrCdsTests {
	@ClassRule
	public static final GyrexServerResource server = new GyrexServerResource();

}
