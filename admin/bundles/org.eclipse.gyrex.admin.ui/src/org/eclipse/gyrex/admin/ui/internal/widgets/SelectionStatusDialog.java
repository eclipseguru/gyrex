/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *     font should be activated and used by other components.
 *     Gunnar Wagenknecht - fork for Gyrex Admin UI 
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract base class for dialogs with a status bar and ok/cancel buttons.
 * The status message must be passed over as StatusInfo object and can be an
 * error, warning or ok. The OK button is enabled or disabled depending on the
 * status.
 * 
 * @since 1.1
 */
public abstract class SelectionStatusDialog extends SelectionDialog {

	/**
	 * A message line displaying a status.
	 */
	private class MessageLine extends CLabel {

		private final Color fNormalMsgAreaBackground;

		/**
		 * Creates a new message line as a child of the given parent.
		 * 
		 * @param parent
		 */
		public MessageLine(final Composite parent) {
			this(parent, SWT.LEFT);
		}

		/**
		 * Creates a new message line as a child of the parent and with the
		 * given SWT stylebits.
		 * 
		 * @param parent
		 * @param style
		 */
		public MessageLine(final Composite parent, final int style) {
			super(parent, style);
			fNormalMsgAreaBackground = getBackground();
		}

		/**
		 * Find an image assocated with the status.
		 * 
		 * @param status
		 * @return Image
		 */
		private Image findImage(final IStatus status) {
			if (status.isOK()) {
				return null;
			} else if (status.matches(IStatus.ERROR)) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
			} else if (status.matches(IStatus.WARNING)) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
			} else if (status.matches(IStatus.INFO)) {
				return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
			}
			return null;
		}

		/**
		 * Sets the message and image to the given status.
		 * 
		 * @param status
		 *            IStatus or <code>null</code>. <code>null</code> will set
		 *            the empty text and no image.
		 */
		public void setErrorStatus(final IStatus status) {
			if ((status != null) && !status.isOK()) {
				final String message = status.getMessage();
				if ((message != null) && (message.length() > 0)) {
					setText(message);
					// unqualified call of setImage is too ambiguous for
					// Foundation 1.0 compiler
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=140576
					MessageLine.this.setImage(findImage(status));
					setBackground(JFaceColors.getErrorBackground(getDisplay()));
					return;
				}
			}
			setText(""); //$NON-NLS-1$	
			// unqualified call of setImage is too ambiguous for Foundation 1.0
			// compiler
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=140576
			MessageLine.this.setImage(null);
			setBackground(fNormalMsgAreaBackground);
		}
	}

	private MessageLine fStatusLine;
	private IStatus fLastStatus;
	private Image fImage;
	private boolean fStatusLineAboveButtons = false;

	/**
	 * Creates an instance of a <code>SelectionStatusDialog</code>.
	 * 
	 * @param parent
	 */
	public SelectionStatusDialog(final Shell parent) {
		super(parent);
	}

	/**
	 * Compute the result and return it.
	 */
	protected abstract void computeResult();

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		if (fImage != null) {
			shell.setImage(fImage);
		}
	}

	@Override
	public void create() {
		super.create();
		if (fLastStatus != null) {
			updateStatus(fLastStatus);
		}
	}

	@Override
	protected Control createButtonBar(final Composite parent) {
		final Font font = parent.getFont();
		final Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout();
		if (!fStatusLineAboveButtons) {
			layout.numColumns = 2;
		}
		layout.marginHeight = 0;
		layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(font);

		if (!fStatusLineAboveButtons && isHelpAvailable()) {
			createHelpControl(composite);
		}
		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		final GridData statusData = new GridData(GridData.FILL_HORIZONTAL);
		fStatusLine.setErrorStatus(null);
		fStatusLine.setFont(font);
		if (fStatusLineAboveButtons && isHelpAvailable()) {
			statusData.horizontalSpan = 2;
			createHelpControl(composite);
		}
		fStatusLine.setLayoutData(statusData);

		/*
		 * Create the rest of the button bar, but tell it not to
		 * create a help button (we've already created it).
		 */
		final boolean helpAvailable = isHelpAvailable();
		setHelpAvailable(false);
		super.createButtonBar(composite);
		setHelpAvailable(helpAvailable);
		return composite;
	}

	/**
	 * Returns the first element from the list of results. Returns
	 * <code>null</code> if no element has been selected.
	 * 
	 * @return the first result element if one exists. Otherwise
	 *         <code>null</code> is returned.
	 */
	public Object getFirstResult() {
		final Object[] result = getResult();
		if ((result == null) || (result.length == 0)) {
			return null;
		}
		return result[0];
	}

	@Override
	protected void okPressed() {
		computeResult();
		super.okPressed();
	}

	/**
	 * Sets the image for this dialog.
	 * 
	 * @param image
	 *            the image.
	 */
	public void setImage(final Image image) {
		fImage = image;
	}

	/**
	 * Sets a result element at the given position.
	 * 
	 * @param position
	 * @param element
	 */
	protected void setResult(final int position, final Object element) {
		final Object[] result = getResult();
		result[position] = element;
		setResult(Arrays.asList(result));
	}

	/**
	 * Controls whether status line appears to the left of the buttons (default)
	 * or above them.
	 * 
	 * @param aboveButtons
	 *            if <code>true</code> status line is placed above buttons; if
	 *            <code>false</code> to the right
	 */
	public void setStatusLineAboveButtons(final boolean aboveButtons) {
		fStatusLineAboveButtons = aboveButtons;
	}

	/**
	 * Update the status of the ok button to reflect the given status.
	 * Subclasses may override this method to update additional buttons.
	 * 
	 * @param status
	 */
	protected void updateButtonsEnableState(final IStatus status) {
		final Button okButton = getOkButton();
		if ((okButton != null) && !okButton.isDisposed()) {
			okButton.setEnabled(!status.matches(IStatus.ERROR));
		}
	}

	/**
	 * Update the dialog's status line to reflect the given status. It is safe
	 * to call this method before the dialog has been opened.
	 * 
	 * @param status
	 */
	protected void updateStatus(final IStatus status) {
		fLastStatus = status;
		if ((fStatusLine != null) && !fStatusLine.isDisposed()) {
			updateButtonsEnableState(status);
			fStatusLine.setErrorStatus(status);
		}
	}

}
