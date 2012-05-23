/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH, EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gunnar Wagenknecht - extracted from RAP Examples
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import java.util.List;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
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

public abstract class DropDownMenu extends Composite {

	private static final long serialVersionUID = 1L;
	private final Menu pullDownMenu;
	private final String text;
	private final String customVariant;
	private final ToolBar toolBar;
	private final ToolItem toolItem;
	private boolean selected;
	private boolean open;

	public DropDownMenu(final Composite parent, final String text, final String customVariant) {
		super(parent, SWT.NONE);
		this.text = text;
		this.customVariant = customVariant;

		// menu
		pullDownMenu = createMenu(parent);
		setLayout(new FillLayout());
		setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// toolbar
		toolBar = new ToolBar(this, SWT.HORIZONTAL);
		toolBar.setData(WidgetUtil.CUSTOM_VARIANT, customVariant);

		// tool item
		toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		toolItem.setData(WidgetUtil.CUSTOM_VARIANT, customVariant);
		toolItem.setText(text.replace("&", "&&"));
		toolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				toolItemSelected(toolBar, event);
			}

		});
	}

	private Menu createMenu(final Composite parent) {
		final Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		menu.setData(WidgetUtil.CUSTOM_VARIANT, customVariant);
		return menu;
	}

	private void createMenuItem(final String item) {
		final MenuItem menuItem = new MenuItem(pullDownMenu, SWT.PUSH | SWT.LEFT);
		menuItem.setText(item.replace("&", "&&"));
		menuItem.setData(WidgetUtil.CUSTOM_VARIANT, customVariant);
		menuItem.addSelectionListener(new SelectionAdapter() {
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

	/**
	 * Returns the text.
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the ToolItem which represents the drop-down element.
	 * 
	 * @return the toolItem
	 */
	public ToolItem getToolItem() {
		return toolItem;
	}

	protected abstract void openItem(String item);

	private void openMenu(final Point point) {
		// set open
		setOpen(true);

		// reset when menu is hidden
		pullDownMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuHidden(final MenuEvent e) {
				setOpen(false);
				pullDownMenu.removeMenuListener(this);
			}
		});

		// show menu
		pullDownMenu.setLocation(point);
		pullDownMenu.setVisible(true);
	}

	public void setOpen(final boolean open) {
		this.open = open;
		updateCustomVariant();
	}

	public void setSelected(final boolean selected) {
		this.selected = selected;
		updateCustomVariant();
	}

	void toolItemSelected(final ToolBar toolBar, final SelectionEvent event) {
		if (pullDownMenu.getItemCount() == 0) {
			return;
		}

		final Rectangle pos = ((ToolItem) event.getSource()).getBounds();
		openMenu(toolBar.toDisplay(pos.x, pos.y + pos.height));
	}

	private void updateCustomVariant() {
		String variant = customVariant;
		if (selected) {
			variant += "Selected";
		}
		if (open) {
			variant += "Open";
		}
		toolItem.setData(WidgetUtil.CUSTOM_VARIANT, variant);

	}

}
