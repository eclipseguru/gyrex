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

import org.eclipse.gyrex.admin.ui.internal.pages.AdminPageRegistry;
import org.eclipse.gyrex.admin.ui.internal.pages.CategoryContribution;
import org.eclipse.gyrex.admin.ui.internal.pages.PageContribution;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

abstract class DropDownNavigation extends Composite {

	private static final long serialVersionUID = 1L;
	private final Menu pullDownMenu;
	private final CategoryContribution category;
	private final List<PageContribution> pages;

	public DropDownNavigation(final Composite parent, final CategoryContribution category) {
		super(parent, SWT.NONE);
		this.category = category;
		pullDownMenu = createMenu(parent);
		setLayout(new FillLayout());
		setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// get pages for category and sort
		pages = AdminPageRegistry.getInstance().getPages(category);
		Collections.sort(pages);

		// build menu
		createMenuItems();
		createDropDownToolItem();
	}

	private void createDropDownToolItem() {
		final ToolBar toolBar = new ToolBar(this, SWT.HORIZONTAL);
		toolBar.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");
		final ToolItem toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		toolItem.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");
		toolItem.setText(category.getName().replace("&", "&&"));
		toolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				toolItemSelected(toolBar, event);
			}

		});
	}

	private Menu createMenu(final Composite parent) {
		final Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		menu.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");
		return menu;
	}

	private void createMenuItem(final PageContribution page) {
		final MenuItem item = new MenuItem(pullDownMenu, SWT.PUSH | SWT.LEFT);
		item.setText(page.getName().replace("&", "&&"));
		item.setData(WidgetUtil.CUSTOM_VARIANT, "navigation");
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				openPage(page);
			}
		});
	}

	private void createMenuItems() {
		for (final PageContribution page : pages) {
			createMenuItem(page);
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

	private void openMenu(final Point point) {
		pullDownMenu.setLocation(point);
		pullDownMenu.setVisible(true);
	}

	protected abstract void openPage(PageContribution contribution);

	void toolItemSelected(final ToolBar toolBar, final SelectionEvent event) {
		final Rectangle pos = ((ToolItem) event.getSource()).getBounds();
		openMenu(toolBar.toDisplay(pos.x, pos.y + pos.height));
	}

}
