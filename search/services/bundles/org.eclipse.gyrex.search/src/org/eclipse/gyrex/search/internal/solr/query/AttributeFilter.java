/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.query;

import org.eclipse.gyrex.cds.query.FilterType;
import org.eclipse.gyrex.cds.query.IAttributeFilter;
import org.eclipse.gyrex.cds.query.QueryUtil;

import org.eclipse.osgi.util.NLS;

/**
 * {@link IAttributeFilter} implementation
 */
class AttributeFilter extends BaseFilter implements IAttributeFilter {

	private final String attributeId;
	private String value;

	/**
	 * Creates a new instance.
	 * 
	 * @param attributeId
	 */
	public AttributeFilter(final String attributeId) {
		this.attributeId = attributeId;
	}

	@Override
	public void matchValue(final String value) {
		this.value = value;
	}

	@Override
	public AttributeFilter ofType(final FilterType type) {
		return (AttributeFilter) super.ofType(type);
	}

	@Override
	public String toFilterQuery() {
		switch (getType()) {
			case EXCLUSIVE:
				return NLS.bind("-{0}:{1}", attributeId, QueryUtil.escapeQueryChars(value));

			case INCLUSIVE:
			default:
				return NLS.bind("{0}:{1}", attributeId, QueryUtil.escapeQueryChars(value));
		}
	}
}
