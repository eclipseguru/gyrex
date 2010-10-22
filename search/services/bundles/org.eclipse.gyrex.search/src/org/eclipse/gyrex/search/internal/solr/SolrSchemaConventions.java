/*******************************************************************************
 * Copyright (c) 2010 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

import org.apache.commons.lang.StringUtils;

/**
 * Some conventions for the underlying Solr schema.
 */
public class SolrSchemaConventions {

	private static final String FACET_FIELDNAME_SUFFIX = "_facet";

	/**
	 * Converts the specified facet field name into an
	 * {@link IDocumentAttribute#getId() attribute id}.
	 * 
	 * @param fieldName
	 *            the field name as used in the Solr schema
	 * @return the {@link IDocumentAttribute#getId() attribute id}
	 */
	public static String facetAttributeId(final String fieldName) {
		return StringUtils.removeEnd(fieldName, FACET_FIELDNAME_SUFFIX);
	}

	/**
	 * Converts the specified {@link IDocumentAttribute#getId() attribute id}
	 * into a facet field name.
	 * 
	 * @param attributeId
	 *            the {@link IDocumentAttribute#getId() attribute id}
	 * @return the facet field name used in the Solr schema
	 */
	public static String facetFieldName(final String attributeId) {
		if (attributeId == null) {
			throw new IllegalArgumentException("attribute id must not be null");
		}
		return attributeId.concat(FACET_FIELDNAME_SUFFIX);
	}

	private SolrSchemaConventions() {
		// empty
	}

}
