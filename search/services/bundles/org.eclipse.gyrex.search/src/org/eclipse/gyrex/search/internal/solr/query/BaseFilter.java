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
package org.eclipse.gyrex.cds.solr.internal.query;

import org.eclipse.gyrex.cds.query.FilterType;
import org.eclipse.gyrex.cds.query.IFilter;

/**
 * Base {@link IFilter} implementation
 */
public abstract class BaseFilter {

	private FilterType type;

	public FilterType getType() {
		return type != null ? type : FilterType.INCLUSIVE;
	}

	public BaseFilter ofType(final FilterType type) {
		if (type == null) {
			throw new IllegalArgumentException("type must not be null");
		}
		this.type = type;
		return this;
	}

	public abstract String toFilterQuery();

}
