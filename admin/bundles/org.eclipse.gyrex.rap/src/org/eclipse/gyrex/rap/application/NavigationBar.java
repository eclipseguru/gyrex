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
package org.eclipse.gyrex.rap.application;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

abstract class NavigationBar extends Composite {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final List<Category> categories;
	private final PageProvider pageProvider;

	public NavigationBar(final Composite parent, final PageProvider pageProvider) {
		super(parent, SWT.NONE);
		setLayout(GridLayoutFactory.fillDefaults().numColumns(5).create());
		setData(RWT.CUSTOM_VARIANT, "navigation");

		checkArgument(pageProvider != null, "PageProvider must not be null!");
		this.pageProvider = pageProvider;

		// get and sort categories
		categories = getPageProvider().getCategories();
		Collections.sort(categories);

		// create UI
		for (final Category category : categories) {
			createNavigationDropDown(category);
		}
	}

	private void changeSelectedDropDownEntry(final PageHandle page, final DropDownNavigation navEntry) {
		navEntry.setSelected(pageBelongsToDropDownNav(page, navEntry));
	}

	private void createNavigationDropDown(final Category category) {
		new DropDownNavigation(this, category, getPageProvider()) {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			protected void openPage(final PageHandle page) {
				NavigationBar.this.openPage(page);
			}
		};
	}

	PageHandle findInitialPage() {
		final Control[] children = getChildren();
		for (final Control control : children) {
			if (control instanceof DropDownNavigation)
				return ((DropDownNavigation) control).findFirstPage();
		}
		return null;
	}

	/**
	 * Returns the pageProvider.
	 *
	 * @return the pageProvider
	 */
	PageProvider getPageProvider() {
		return pageProvider;
	}

	/**
	 * Opens the selected page.
	 * <p>
	 * Subclasses must implement and react on the request to open the specified
	 * page.
	 * </p>
	 *
	 * @param page
	 *            the page to open
	 */
	protected abstract void openPage(PageHandle page);

	private boolean pageBelongsToDropDownNav(final PageHandle page, final DropDownNavigation navEntry) {
		final Category category = navEntry.getCategory();
		return category.getId().equals(page.getCategoryId());
	}

	void selectNavigationEntry(final PageHandle page) {
		final Control[] children = getChildren();
		for (final Control control : children) {
			if (control instanceof DropDownNavigation) {
				changeSelectedDropDownEntry(page, (DropDownNavigation) control);
			}
		}
	}

}
