/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - adapted to Gyrex Console
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.pages.registry.AdminPageRegistry;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.CategoryContribution;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.PageContribution;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

abstract class DropDownNavigation extends DropDownMenu {

	private static List<String> getItemLabels(final List<PageContribution> pages) {
		final ArrayList<String> list = new ArrayList<String>();
		for (final PageContribution pageContribution : pages) {
			list.add(pageContribution.getName());
		}
		return list;
	}

	private final CategoryContribution category;
	private final List<PageContribution> pages;

	public DropDownNavigation(final Composite parent, final CategoryContribution category) {
		super(parent, category.getName(), "navigation");
		this.category = category;
		setLayout(new FillLayout());
		setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// get pages for category and sort
		pages = AdminPageRegistry.getInstance().getPages(category);
		Collections.sort(pages);

		// build menu
		createMenuItems(getItemLabels(pages));
	}

	public PageContribution findFirstPage() {
		for (final PageContribution page : pages) {
			return page;
		}
		return null;
	}

	public CategoryContribution getCategory() {
		return category;
	}

	@Override
	protected void openItem(final String item) {
		for (final PageContribution page : pages) {
			if (item.equals(page.getName())) {
				openPage(page);
				return;
			}
		}
	}

	protected abstract void openPage(final PageContribution page);
}
