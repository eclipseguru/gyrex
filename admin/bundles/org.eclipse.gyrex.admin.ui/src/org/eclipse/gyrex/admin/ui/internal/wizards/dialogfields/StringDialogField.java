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

import org.eclipse.gyrex.admin.ui.internal.helper.UiUtil;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog field containing a label and a text control.
 */
public class StringDialogField extends DialogField {

	protected static GridData gridDataForText(final int span) {
		final GridData gd = new GridData(GridData.FILL, DEFAULT_VERTICAL_ALIGN, false, false);
		gd.horizontalSpan = span;
		return gd;
	}

	private String fText;
	private Text fTextControl;
	private ModifyListener fModifyListener;

	private IContentProposalProvider fContentProposalProcessor;

	public StringDialogField() {
		super();
		fText = ""; //$NON-NLS-1$
	}

	/**
	 * Creates and returns a new text control.
	 * 
	 * @param parent
	 *            the parent
	 * @return the text control
	 * @since 3.6
	 */
	protected Text createTextControl(final Composite parent) {
		return new Text(parent, SWT.SINGLE | SWT.BORDER);
	}

	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Text text = getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 1));

		return new Control[] { label, text };
	}

	private void doModifyText() {
		if (isOkToUse(fTextControl)) {
			fText = fTextControl.getText();
		}
		dialogFieldChanged();
	}

	public IContentProposalProvider getContentProposalProcessor() {
		return fContentProposalProcessor;
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

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 * @return the text control
	 */
	public Text getTextControl(final Composite parent) {
		if (fTextControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					doModifyText();
				}
			};

			fTextControl = createTextControl(parent);
			// moved up due to 1GEUNW2
			fTextControl.setText(fText);
			fTextControl.setFont(parent.getFont());
			fTextControl.addModifyListener(fModifyListener);

			fTextControl.setEnabled(isEnabled());
			if (fContentProposalProcessor != null) {
				UiUtil.addContentProposalBehavior(fTextControl, fContentProposalProcessor);
			}
		}
		return fTextControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTextControl)) {
			setTextWithoutUpdate(fText);
		}
	}

	// ------ enable / disable management

	public void setContentProposalProcessor(final IContentProposalProvider processor) {
		fContentProposalProcessor = processor;
		if ((fContentProposalProcessor != null) && isOkToUse(fTextControl)) {
			UiUtil.addContentProposalBehavior(fTextControl, fContentProposalProcessor);
		}
	}

	// ------ text access

	/*
	 * @see DialogField#setFocus
	 */
	@Override
	public boolean setFocus() {
		if (isOkToUse(fTextControl)) {
			fTextControl.setFocus();
			fTextControl.setSelection(0, fTextControl.getText().length());
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
		if (isOkToUse(fTextControl)) {
			fTextControl.setText(text);
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
		if (isOkToUse(fTextControl)) {
			fTextControl.removeModifyListener(fModifyListener);
			fTextControl.setText(text);
			fTextControl.addModifyListener(fModifyListener);
		}
	}

	/*
	 * @see DialogField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(isEnabled());
		}
	}

}
