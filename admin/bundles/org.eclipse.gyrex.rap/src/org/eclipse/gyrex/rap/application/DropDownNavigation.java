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

import java.util.Collections;
import java.util.List;

import org.eclipse.gyrex.rap.widgets.DropDownItem;

import org.eclipse.rap.rwt.RWT;
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

	private final Category category;
	private final List<PageHandle> pages;
	private final Menu pullDownMenu;

	public DropDownNavigation(final Composite parent, final Category category, final PageProvider pageProvider) {
		super(parent, category.getName(), "navigation");
		this.category = category;

		// get pages for category and sort
		pages = pageProvider.getPages(category);
		Collections.sort(pages);

		// menu
		pullDownMenu = new Menu(parent.getShell(), SWT.POP_UP);
		pullDownMenu.setData(RWT.CUSTOM_VARIANT, getCustomVariant());

		// build menu
		for (final PageHandle page : pages) {
			createMenuItem(page);
		}
	}

	private void createMenuItem(final PageHandle page) {
		final MenuItem menuItem = new MenuItem(pullDownMenu, SWT.PUSH | SWT.LEFT);
		menuItem.setText(page.getName().replace("&", "&&"));
		menuItem.setData(RWT.CUSTOM_VARIANT, getCustomVariant());
		menuItem.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				openPage(page);
			}
		});
	}

	PageHandle findFirstPage() {
		for (final PageHandle page : pages)
			return page;
		return null;
	}

	Category getCategory() {
		return category;
	}

	@Override
	protected void openDropDown(final Point location) {
		if (pullDownMenu.getItemCount() == 0)
			return;
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

	protected abstract void openPage(final PageHandle page);
}
