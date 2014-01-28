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

/**
 * Dialog field containing a second label.
 */
public class DescriptionDialogField extends DialogField {

	protected static GridData gridDataForDescription(final int span) {
		final GridData gd = new GridData(GridData.FILL, DEFAULT_VERTICAL_ALIGN, false, false);
		gd.horizontalSpan = span;
		return gd;
	}

	private String fText;
	private Label fDescriptionControl;

	public DescriptionDialogField() {
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
	protected Label createDescriptionControl(final Composite parent) {
		final Label desc = new Label(parent, SWT.LEFT | SWT.WRAP);
		desc.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		return desc;
	}

	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Label description = getDescriptionControl(parent);
		description.setLayoutData(gridDataForDescription(nColumns - 1));

		return new Control[] { label, description };
	}

	/**
	 * Creates or returns the created description control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 * @return the description control
	 */
	public Label getDescriptionControl(final Composite parent) {
		if (fDescriptionControl == null) {
			assertCompositeNotNull(parent);

			fDescriptionControl = createDescriptionControl(parent);
			// moved up due to 1GEUNW2
			fDescriptionControl.setText(fText);
			fDescriptionControl.setFont(parent.getFont());
			fDescriptionControl.setEnabled(isEnabled());
		}
		return fDescriptionControl;
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
		if (isOkToUse(fDescriptionControl)) {
			setTextWithoutUpdate(fText);
		}
	}

	@Override
	public boolean setFocus() {
		if (isOkToUse(fDescriptionControl)) {
			fDescriptionControl.setFocus();
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
		if (isOkToUse(fDescriptionControl)) {
			fDescriptionControl.setText(text);
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
		if (isOkToUse(fDescriptionControl)) {
			fDescriptionControl.setText(text);
		}
	}

	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fDescriptionControl)) {
			fDescriptionControl.setEnabled(isEnabled());
		}
	}

}
