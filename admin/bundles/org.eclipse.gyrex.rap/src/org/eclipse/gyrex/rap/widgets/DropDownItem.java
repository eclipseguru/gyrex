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
package org.eclipse.gyrex.rap.widgets;

import static com.google.common.base.Preconditions.checkArgument;

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

/**
 * A widget providing drop-down like behavior.
 * <p>
 * The widget appearance depends on styling.
 * </p>
 */
public abstract class DropDownItem extends Composite {

	private static final long serialVersionUID = 1L;
	private final String text;
	private final String customVariant;
	private final ToolBar toolBar;
	private final ToolItem toolItem;
	private boolean selected;
	private boolean open;

	/**
	 * Creates a new instance hosted in the specified parent.
	 *
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot
	 *            be null)
	 * @param text
	 *            the drop down item text (must not be <code>null</code>)
	 * @param customVariant
	 *            base name for {@link RWT#CUSTOM_VARIANT} (must not be
	 *            <code>null</code>)
	 */
	public DropDownItem(final Composite parent, final String text, final String customVariant) {
		super(parent, SWT.NONE);
		checkArgument(text != null, "text must no be null");
		checkArgument(customVariant != null, "customVariant must no be null");
		this.text = text;
		this.customVariant = customVariant;

		setLayout(new FillLayout());
		setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// tool bar
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

	/**
	 * Called when the drop-down shall be opened.
	 * <p>
	 * Subclasses must override and open the content. Implementors must call
	 * {@link #setOpen(boolean)} with an appropriate state if the drop down was
	 * opened and closed later on (most likely due to user interaction).
	 *
	 * @param location
	 */
	protected abstract void openDropDown(final Point location);

	/**
	 * Sets the widget's open state.
	 *
	 * @param open
	 *            <code>true</code> if open, <code>false</code> otherwise
	 */
	public void setOpen(final boolean open) {
		this.open = open;
		updateCustomVariant();
	}

	/**
	 * Sets the widget's selection state.
	 *
	 * @param open
	 *            <code>true</code> if selected, <code>false</code> otherwise
	 */
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
