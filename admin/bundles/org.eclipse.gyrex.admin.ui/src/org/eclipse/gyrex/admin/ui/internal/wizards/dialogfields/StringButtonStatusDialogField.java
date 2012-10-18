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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog field containing a label, text control, status label and a button
 * control. The status label can be either a image or text label, and can be usd
 * to give additional information about the current element chosen.
 */
public class StringButtonStatusDialogField extends StringButtonDialogField {

	private Label fStatusLabelControl;
	private Object fStatus; // String or ImageDescriptor

	private String fWidthHintString;
	private int fWidthHint;

	public StringButtonStatusDialogField(final IStringButtonAdapter adapter) {
		super(adapter);
		fStatus = null;
		fWidthHintString = null;
		fWidthHint = -1;
	}

	// ------ set status

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Text text = getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 3));
		final Label status = getStatusLabelControl(parent);
		status.setLayoutData(gridDataForStatusLabel(parent, 1));
		final Button button = getChangeControl(parent);
		button.setLayoutData(gridDataForButton(button, 1));

		return new Control[] { label, text, status, button };
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	@Override
	public int getNumberOfControls() {
		return 4;
	}

	/**
	 * Creates or returns the created status label widget.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 */
	public Label getStatusLabelControl(final Composite parent) {
		if (fStatusLabelControl == null) {
			assertCompositeNotNull(parent);
			fStatusLabelControl = new Label(parent, SWT.LEFT);
			fStatusLabelControl.setFont(parent.getFont());
			fStatusLabelControl.setEnabled(isEnabled());
			if (fStatus instanceof Image) {
				fStatusLabelControl.setImage((Image) fStatus);
			} else if (fStatus instanceof String) {
				fStatusLabelControl.setText((String) fStatus);
			} else {
				// must be null
			}
		}
		return fStatusLabelControl;
	}

	protected GridData gridDataForStatusLabel(final Control aControl, final int span) {
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalIndent = 0;
		if (fWidthHintString != null) {
			final GC gc = new GC(aControl);
			gc.setFont(JFaceResources.getDialogFont());
			gd.widthHint = gc.textExtent(fWidthHintString).x;
			gc.dispose();
		} else if (fWidthHint != -1) {
			gd.widthHint = fWidthHint;
		} else {
			gd.widthHint = SWT.DEFAULT;
		}
		return gd;
	}

	// ------- layout helpers

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (fStatus instanceof String) {
			setStatus((String) fStatus);
		} else {
			setStatus((Image) fStatus);
		}
	}

	/**
	 * Sets the status image. Caller is responsible to dispose image
	 */
	public void setStatus(final Image image) {
		if (isOkToUse(fStatusLabelControl)) {
			if (image == null) {
				fStatusLabelControl.setImage(null);
			} else {
				fStatusLabelControl.setImage(image);
			}
		}
		fStatus = image;
	}

	/**
	 * Sets the status string.
	 */
	public void setStatus(final String status) {
		if (isOkToUse(fStatusLabelControl)) {
			fStatusLabelControl.setText(status);
		}
		fStatus = status;
	}

	// ------- ui creation

	/**
	 * Sets the width hint of the status label.
	 */
	public void setStatusWidthHint(final int widthHint) {
		fWidthHint = widthHint;
		fWidthHintString = null;
	}

	// ------ enable / disable management

	/**
	 * Sets the staus string hint of the status label. The string is used to
	 * calculate the size of the status label.
	 */
	public void setStatusWidthHint(final String widthHintString) {
		fWidthHintString = widthHintString;
		fWidthHint = -1;
	}

	/*
	 * @see DialogField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fStatusLabelControl)) {
			fStatusLabelControl.setEnabled(isEnabled());
		}
	}
}
