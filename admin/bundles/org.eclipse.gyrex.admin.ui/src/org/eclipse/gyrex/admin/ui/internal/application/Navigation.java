/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import java.util.Collections;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.pages.AdminPageRegistry;
import org.eclipse.gyrex.admin.ui.internal.pages.CategoryContribution;
import org.eclipse.gyrex.admin.ui.internal.pages.PageContribution;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public abstract class Navigation {

	private final Composite composite;
	private final List<CategoryContribution> categories;

	public Navigation(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(ExampleUtil.createGridLayoutWithoutMargin(9, false));
		composite.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");

		// get and sort categories
		categories = AdminPageRegistry.getInstance().getCategories();
		Collections.sort(categories);

		// create UI
		createNavigationControls(composite);
	}

	private void changeSelectedDropDownEntry(final PageContribution page, final DropDownNavigation navEntry) {
		final boolean belongsToDropDownNav = pageBelongsToDropDownNav(page, navEntry);
		final ToolItem item = ((ToolBar) navEntry.getChildren()[0]).getItem(0);
		if (belongsToDropDownNav) {
			item.setData(WidgetUtil.CUSTOM_VARIANT, "selected");
		} else {
			item.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");
		}
	}

	private void createNavigationControls(final Composite parent) {
		for (final CategoryContribution category : categories) {
			createNavigationDropDown(parent, category);
		}
	}

	private void createNavigationDropDown(final Composite parent, final CategoryContribution category) {
		new DropDownNavigation(parent, category) {
			@Override
			protected void openPage(final PageContribution page) {
				Navigation.this.openPage(page);
			}
		};
	}

	public PageContribution findInitialPage() {
		final Control[] children = composite.getChildren();
		for (final Control control : children) {
			if (control instanceof DropDownNavigation) {
				return ((DropDownNavigation) control).findFirstPage();
			}
		}
		return null;
	}

	public Control getControl() {
		return composite;
	}

	protected abstract void openPage(PageContribution page);

	private boolean pageBelongsToDropDownNav(final PageContribution page, final DropDownNavigation navEntry) {
		final CategoryContribution category = navEntry.getCategory();
		return category.getId().equals(page.getCategoryId());
	}

	public void selectNavigationEntry(final PageContribution page) {
		final Control[] children = composite.getChildren();
		for (final Control control : children) {
			if (control instanceof DropDownNavigation) {
				changeSelectedDropDownEntry(page, (DropDownNavigation) control);
			}
		}
	}

}
