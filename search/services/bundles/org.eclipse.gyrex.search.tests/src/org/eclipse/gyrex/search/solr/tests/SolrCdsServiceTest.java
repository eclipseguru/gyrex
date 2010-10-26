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

import org.eclipse.gyrex.cds.IContentDeliveryService;

import org.junit.Test;

/**
 * Solr CDS service tests
 */
public class SolrCdsServiceTest extends BaseSolrTest {

	@Test
	public void test001_CdsBasics() throws Exception {
		final IContentDeliveryService service = getContext().get(IContentDeliveryService.class);
		assertNotNull(service);
	}
}
