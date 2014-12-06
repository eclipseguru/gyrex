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
package org.eclipse.gyrex.admin.ui.internal.pages.registry;

import org.eclipse.gyrex.rap.application.Page;
import org.eclipse.gyrex.rap.application.PageHandle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.apache.commons.lang.StringUtils;

/**
 * A page contributed by an extension for the admin ui.
 */
public class PageContribution extends PageHandle {

	final IConfigurationElement element;

	public PageContribution(final IConfigurationElement element) {
		super(element.getAttribute("id"));
		this.element = element;
		setKeywords(StringUtils.split(element.getAttribute("keywords")));
		setName(element.getAttribute("name"));
		setSortKey(element.getAttribute("sortKey"));
		setCategoryId(element.getAttribute("categoryId"));
	}

	Page createPage() throws CoreException {
		return (Page) element.createExecutableExtension("class");
	}
}
