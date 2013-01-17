/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH, EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gunnar Wagenknecht - extracted from RAP Examples and refactored
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public abstract class DropDownItem extends Composite {

	private static final long serialVersionUID = 1L;
	private final String text;
	private final String customVariant;
	private final ToolBar toolBar;
	private final ToolItem toolItem;
	private boolean selected;
	private boolean open;

	public DropDownItem(final Composite parent, final String text, final String customVariant) {
		super(parent, SWT.NONE);
		this.text = text;
		this.customVariant = customVariant;

		setLayout(new FillLayout());
		setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// toolbar
		toolBar = new ToolBar(this, SWT.HORIZONTAL);
		toolBar.setData(RWT.CUSTOM_VARIANT, customVariant);

		// tool item
		toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		toolItem.setData(RWT.CUSTOM_VARIANT, customVariant);
		toolItem.setText(text.replace("&", "&&"));
		toolItem.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				toolItemSelected(toolBar, event);
			}

		});
	}

	protected String getCustomVariant() {
		return customVariant;
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

	protected abstract void openDropDown(final Point location);

	public void setOpen(final boolean open) {
		this.open = open;
		updateCustomVariant();
	}

	public void setSelected(final boolean selected) {
		this.selected = selected;
		updateCustomVariant();
	}

	void toolItemSelected(final ToolBar toolBar, final SelectionEvent event) {
		final Rectangle pos = ((ToolItem) event.getSource()).getBounds();
		openDropDown(toolBar.toDisplay(pos.x, pos.y + pos.height));
	}

	private void updateCustomVariant() {
		String variant = customVariant;
		if (selected) {
			variant += "Selected";
		}
		if (open) {
			variant += "Open";
		}
		toolItem.setData(RWT.CUSTOM_VARIANT, variant);

	}

}
