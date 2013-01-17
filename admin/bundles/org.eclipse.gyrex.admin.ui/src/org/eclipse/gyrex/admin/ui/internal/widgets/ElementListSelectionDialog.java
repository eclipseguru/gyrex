/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - Fork for Gyrex Admin UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A class to select elements out of a list of elements.
 * 
 * @since .1
 */
@SuppressWarnings("serial")
public class ElementListSelectionDialog extends AbstractElementListSelectionDialog {

	private Object[] fElements;

	/**
	 * Creates a list selection dialog.
	 * 
	 * @param parent
	 *            the parent widget.
	 * @param renderer
	 *            the label renderer.
	 */
	public ElementListSelectionDialog(final Shell parent, final ILabelProvider renderer) {
		super(parent, renderer);
	}

	/*
	 * @see SelectionStatusDialog#computeResult()
	 */
	@Override
	protected void computeResult() {
		setResult(Arrays.asList(getSelectedElements()));
	}

	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite contents = (Composite) super.createDialogArea(parent);

		createMessageArea(contents);
		createFilterText(contents);
		createFilteredList(contents);

		setListElements(fElements);

		setSelection(getInitialElementSelections().toArray());

		return contents;
	}

	/**
	 * Sets the elements of the list.
	 * 
	 * @param elements
	 *            the elements of the list.
	 */
	public void setElements(final Object[] elements) {
		fElements = elements;
	}
}
