/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation, AGETO Service GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - adapted to Forms UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.forms;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;

public class FormLayoutDataFactory {

	public static GridData applyDefaults(final Button button, final int span) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).hint(getWidthHint(button), SWT.DEFAULT).span(span, 1).applyTo(button);
		return (GridData) button.getLayoutData();
	}

	public static GridData applyDefaults(final Composite composite, final int span) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(span, 1).applyTo(composite);
		return (GridData) composite.getLayoutData();
	}

	public static GridData applyDefaults(final FilteredList filteredList, final int span) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(span, 1).applyTo(filteredList);
		return (GridData) filteredList.getLayoutData();
	}

	public static GridData applyDefaults(final Label label, final int span) {
		// labels align BEGINNING and don't grab
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false).span(span, 1).applyTo(label);
		return (GridData) label.getLayoutData();
	}

	public static GridData applyDefaults(final org.eclipse.swt.widgets.List list, final int span) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(span, 1).applyTo(list);
		return (GridData) list.getLayoutData();
	}

	public static GridData applyDefaults(final Text text, final int span) {
		// text should grab what's available
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(span, 1).applyTo(text);
		return (GridData) text.getLayoutData();
	}

	public static int getWidthHint(final Button button) {
		button.setFont(JFaceResources.getDialogFont());
		final PixelConverter converter = new PixelConverter(button);
		final int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
}
