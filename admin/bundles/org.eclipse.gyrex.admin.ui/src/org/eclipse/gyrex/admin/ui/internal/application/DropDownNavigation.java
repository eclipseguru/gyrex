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
import org.eclipse.gyrex.admin.ui.internal.widgets.DropDownItem;

import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

abstract class DropDownNavigation extends DropDownItem {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private static List<String> getItemLabels(final List<PageContribution> pages) {
		final ArrayList<String> list = new ArrayList<String>();
		for (final PageContribution pageContribution : pages) {
			list.add(pageContribution.getName());
		}
		return list;
	}

	private final CategoryContribution category;
	private final List<PageContribution> pages;
	private final Menu pullDownMenu;

	public DropDownNavigation(final Composite parent, final CategoryContribution category) {
		super(parent, category.getName(), "navigation");
		this.category = category;

		// get pages for category and sort
		pages = AdminPageRegistry.getInstance().getPages(category);
		Collections.sort(pages);

		// menu
		pullDownMenu = new Menu(parent.getShell(), SWT.POP_UP);
		pullDownMenu.setData(WidgetUtil.CUSTOM_VARIANT, getCustomVariant());

		// build menu
		createMenuItems(getItemLabels(pages));
	}

	private void createMenuItem(final String item) {
		final MenuItem menuItem = new MenuItem(pullDownMenu, SWT.PUSH | SWT.LEFT);
		menuItem.setText(item.replace("&", "&&"));
		menuItem.setData(WidgetUtil.CUSTOM_VARIANT, getCustomVariant());
		menuItem.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				openItem(item);
			}
		});
	}

	public void createMenuItems(final List<String> items) {
		// dispose existing
		for (final MenuItem menuItem : pullDownMenu.getItems()) {
			menuItem.dispose();
		}

		// create new
		for (final String item : items) {
			createMenuItem(item);
		}
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
	protected void openDropDown(final Point location) {
		if (pullDownMenu.getItemCount() == 0) {
			return;
		}
		// set open
		setOpen(true);

		// reset when menu is hidden
		pullDownMenu.addMenuListener(new MenuAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuHidden(final MenuEvent e) {
				setOpen(false);
				pullDownMenu.removeMenuListener(this);
			}
		});

		// show menu
		pullDownMenu.setLocation(location);
		pullDownMenu.setVisible(true);
	}

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
