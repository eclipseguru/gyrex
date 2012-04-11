/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.pages;

import org.eclipse.gyrex.admin.ui.pages.AdminPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.apache.commons.lang.StringUtils;

/**
 * A page contributed by an extension for the admin ui.
 */
public class PageContribution implements Comparable<PageContribution> {

	private final IConfigurationElement element;
	private final String id;
	private String[] keywords;

	public PageContribution(final IConfigurationElement element) {
		this.element = element;
		id = element.getAttribute("id");
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("id is required");
		}
	}

	@Override
	public int compareTo(final PageContribution o) {
		return getSortKey().compareTo(o.getSortKey());
	}

	public AdminPage createPage() throws CoreException {
		return (AdminPage) element.createExecutableExtension("class");
	}

	@Override
	public boolean equals(final Object provider) {
		return provider instanceof PageContribution ? getId().equals(((PageContribution) provider).getId()) : Boolean.FALSE;
	}

	public String getCategoryId() {
		return element.getAttribute("categoryId");
	}

	public String getId() {
		return id;
	}

	public String[] getKeywords() {
		if (keywords == null) {
			keywords = StringUtils.split(element.getAttribute("keywords"));
			if (keywords == null) {
				keywords = new String[0];
			}
		}

		return keywords;
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
