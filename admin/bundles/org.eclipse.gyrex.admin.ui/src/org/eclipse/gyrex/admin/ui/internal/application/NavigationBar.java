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

import java.util.Collections;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.pages.registry.AdminPageRegistry;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.CategoryContribution;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.PageContribution;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class NavigationBar extends Composite {

	private final List<CategoryContribution> categories;

	public NavigationBar(final Composite parent) {
		super(parent, SWT.NONE);
		setLayout(AdminUiUtil.createGridLayoutWithoutMargin(5, false));
		setData(WidgetUtil.CUSTOM_VARIANT, "navigation");

		// get and sort categories
		categories = AdminPageRegistry.getInstance().getCategories();
		Collections.sort(categories);

		// create UI
		for (final CategoryContribution category : categories) {
			createNavigationDropDown(category);
		}
	}

	private void changeSelectedDropDownEntry(final PageContribution page, final DropDownNavigation navEntry) {
		navEntry.setSelected(pageBelongsToDropDownNav(page, navEntry));
	}

	private void createNavigationDropDown(final CategoryContribution category) {
		new DropDownNavigation(this, category) {
			@Override
			protected void openPage(final PageContribution page) {
				NavigationBar.this.openPage(page);
			}
		};
	}

	public PageContribution findInitialPage() {
		final Control[] children = getChildren();
		for (final Control control : children) {
			if (control instanceof DropDownNavigation) {
				return ((DropDownNavigation) control).findFirstPage();
			}
		}
		return null;
	}

	protected abstract void openPage(PageContribution page);

	private boolean pageBelongsToDropDownNav(final PageContribution page, final DropDownNavigation navEntry) {
		final CategoryContribution category = navEntry.getCategory();
		return category.getId().equals(page.getCategoryId());
	}

	public void selectNavigationEntry(final PageContribution page) {
		final Control[] children = getChildren();
		for (final Control control : children) {
			if (control instanceof DropDownNavigation) {
				changeSelectedDropDownEntry(page, (DropDownNavigation) control);
			}
		}
	}

}
