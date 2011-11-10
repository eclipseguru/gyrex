/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Dialog field containing a label and a FormText control.
 */
public class FormTextDialogField extends DialogField {

	protected static GridData gridDataForText(final int span) {
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		return gd;
	}

	private String fText;
	private FormText fTextControl;
	private boolean fParseTags;
	private boolean fExpandURLs;

	public FormTextDialogField() {
		super();
		fText = ""; //$NON-NLS-1$
	}

	@Override
	public void adaptToForm(final FormToolkit toolkit) {
		if (isOkToUse(fTextControl)) {
			final FormText formText = fTextControl;
			formText.setHyperlinkSettings(toolkit.getHyperlinkGroup());
			toolkit.adapt(formText, false, true);
			formText.setMenu(fTextControl.getParent().getMenu());
			formText.setFont(fTextControl.getParent().getFont());
		}
	}

	/**
	 * Creates and returns a new text control.
	 * 
	 * @param parent
	 *            the parent
	 * @return the text control
	 * @since 3.6
	 */
	protected FormText createTextControl(final Composite parent) {
		return new FormText(parent, SWT.WRAP);
	}

	// ------- layout helpers

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final FormText text = getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 1));

		return new Control[] { label, text };
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	// ------- ui creation

	/**
	 * @return the text, can not be <code>null</code>
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 * @return the text control
	 */
	public FormText getTextControl(final Composite parent) {
		if (fTextControl == null) {
			assertCompositeNotNull(parent);
			fTextControl = createTextControl(parent);
			fTextControl.setText(fText, fParseTags, fExpandURLs);
			fTextControl.setFont(parent.getFont());
			fTextControl.setEnabled(isEnabled());
		}
		return fTextControl;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTextControl)) {
			setText(fText, fParseTags, fExpandURLs);
		}
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setText(final String text, final boolean parseTags, final boolean expandURLs) {
		fText = text;
		fParseTags = parseTags;
		fExpandURLs = expandURLs;
		if (isOkToUse(fTextControl)) {
			fTextControl.setText(text, parseTags, expandURLs);
		}
	}

	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(isEnabled());
		}
	}

}
