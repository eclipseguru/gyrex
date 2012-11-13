/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/**
 * Dialog field containing a label and a link control.
 */
public class LinkDialogField extends DialogField {

	protected static GridData gridDataForLink(final int span) {
		final GridData gd = new GridData(GridData.FILL, DEFAULT_VERTICAL_ALIGN, false, false);
		gd.horizontalSpan = span;
		return gd;
	}

	private String fText;
	private Link fLinkControl;

	public LinkDialogField() {
		super();
		fText = ""; //$NON-NLS-1$
	}

	/**
	 * Creates and returns a new link control.
	 * 
	 * @param parent
	 *            the parent
	 * @return the link control
	 */
	protected Link createLinkControl(final Composite parent) {
		final Link link = new Link(parent, SWT.LEFT | SWT.WRAP);
		link.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		return link;
	}

	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Link link = getLinkControl(parent);
		link.setLayoutData(gridDataForLink(nColumns - 1));

		return new Control[] { label, link };
	}

	/**
	 * Creates or returns the created link control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 * @return the link control
	 */
	public Link getLinkControl(final Composite parent) {
		if (fLinkControl == null) {
			assertCompositeNotNull(parent);

			fLinkControl = createLinkControl(parent);
			// moved up due to 1GEUNW2
			fLinkControl.setText(fText);
			fLinkControl.setFont(parent.getFont());
			fLinkControl.setEnabled(isEnabled());
		}
		return fLinkControl;
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * @return the text, can not be <code>null</code>
	 */
	public String getText() {
		return fText;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fLinkControl)) {
			setTextWithoutUpdate(fText);
		}
	}

	@Override
	public boolean setFocus() {
		if (isOkToUse(fLinkControl)) {
			fLinkControl.setFocus();
		}
		return true;
	}

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setText(final String text) {
		fText = text;
		if (isOkToUse(fLinkControl)) {
			fLinkControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setTextWithoutUpdate(final String text) {
		fText = text;
		if (isOkToUse(fLinkControl)) {
			fLinkControl.setText(text);
		}
	}

	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fLinkControl)) {
			fLinkControl.setEnabled(isEnabled());
		}
	}

}
