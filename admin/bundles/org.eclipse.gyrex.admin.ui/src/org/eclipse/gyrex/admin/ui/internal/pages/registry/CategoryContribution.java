/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.pages.registry;

import org.eclipse.core.runtime.IConfigurationElement;

import org.apache.commons.lang.StringUtils;

/**
 * A category which collects page information
 */
public class CategoryContribution implements Comparable<CategoryContribution> {

	private final String id;
	private final IConfigurationElement element;

	public CategoryContribution(final IConfigurationElement element) {
		this.element = element;
		id = element.getAttribute("id");
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("id is required");
		}
	}

	@Override
	public int compareTo(final CategoryContribution o) {
		return getSortKey().compareTo(o.getSortKey());
	}

	public String getId() {
		return id;
	}

	public String getName() {
		final String name = element.getAttribute("name");
		return name != null ? name : getId();
	}

	public String getSortKey() {
		String value = element.getAttribute("sortKey");
		if (StringUtils.isNotBlank(value)) {
			return value;
		}
		value = getName();
		if (StringUtils.isNotBlank(value)) {
			return value;
		}
		return getId();
	}

}
