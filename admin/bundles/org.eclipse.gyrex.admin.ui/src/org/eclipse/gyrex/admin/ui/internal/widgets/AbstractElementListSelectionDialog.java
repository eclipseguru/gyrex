/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.events.KeyEvent;
//import org.eclipse.swt.events.KeyListener;

/**
 * An abstract class to select elements out of a list of elements.
 * 
 * @since 1.1
 */
@SuppressWarnings("serial")
public abstract class AbstractElementListSelectionDialog extends SelectionStatusDialog {

	private final ILabelProvider fRenderer;

	private boolean fIgnoreCase = true;

	private boolean fIsMultipleSelection = false;

	private boolean fMatchEmptyString = true;

	private boolean fAllowDuplicates = true;

	private Label fMessage;

	protected FilteredList fFilteredList;

	private Text fFilterText;

	private ISelectionStatusValidator fValidator;

	private String fFilter = null;

	private String fEmptyListMessage = ""; //$NON-NLS-1$

	private String fEmptySelectionMessage = ""; //$NON-NLS-1$

	private int fWidth = 60;

	private int fHeight = 18;

	private Object[] fSelection = new Object[0];

	/**
	 * Constructs a list selection dialog.
	 * 
	 * @param parent
	 *            The parent for the list.
	 * @param renderer
	 *            ILabelProvider for the list
	 */
	protected AbstractElementListSelectionDialog(final Shell parent, final ILabelProvider renderer) {
		super(parent);
		fRenderer = renderer;
	}

	private void access$superCreate() {
		super.create();
	}

	/*
	 * @see Dialog#cancelPressed
	 */
	@Override
	protected void cancelPressed() {
		setResult(null);
		super.cancelPressed();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	@Override
	public void create() {

		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				access$superCreate();

				Assert.isNotNull(fFilteredList);

				if (fFilteredList.isEmpty()) {
					handleEmptyList();
				} else {
					validateCurrentSelection();
					fFilterText.selectAll();
					fFilterText.setFocus();
				}
			}
		});

	}

	/**
	 * Creates a filtered list.
	 * 
	 * @param parent
	 *            the parent composite.
	 * @return returns the filtered list widget.
	 */
	protected FilteredList createFilteredList(final Composite parent) {
		final int flags = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | (fIsMultipleSelection ? SWT.MULTI : SWT.SINGLE);

		final FilteredList list = new FilteredList(parent, flags, fRenderer, fIgnoreCase, fAllowDuplicates, fMatchEmptyString);

		final GridData data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(fWidth);
		data.heightHint = convertHeightInCharsToPixels(fHeight);
		data.grabExcessVerticalSpace = true;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		list.setLayoutData(data);
		list.setFont(parent.getFont());
		list.setFilter((fFilter == null ? "" : fFilter)); //$NON-NLS-1$

		list.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				handleDefaultSelected();
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				handleWidgetSelected();
			}
		});

		fFilteredList = list;

		return list;
	}

	protected Text createFilterText(final Composite parent) {
		final Text text = new Text(parent, SWT.BORDER);

		final GridData data = new GridData();
		data.grabExcessVerticalSpace = false;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		text.setLayoutData(data);
		text.setFont(parent.getFont());

		text.setText((fFilter == null ? "" : fFilter)); //$NON-NLS-1$

		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event e) {
				fFilteredList.setFilter(fFilterText.getText());
			}
		};
		text.addListener(SWT.Modify, listener);

// RAP [rh] missing Key events
//        text.addKeyListener(new KeyListener() {
//            public void keyPressed(KeyEvent e) {
//                if (e.keyCode == SWT.ARROW_DOWN) {
//					fFilteredList.setFocus();
//				}
//            }
//
//            public void keyReleased(KeyEvent e) {
//            }
//        });

		fFilterText = text;

		return text;
	}

	/**
	 * Creates the message text widget and sets layout data.
	 * 
	 * @param composite
	 *            the parent composite of the message area.
	 */
	@Override
	protected Label createMessageArea(final Composite composite) {
		final Label label = super.createMessageArea(composite);

		final GridData data = new GridData();
		data.grabExcessVerticalSpace = false;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(data);

		fMessage = label;

		return label;
	}

	/**
	 * Returns the current filter pattern.
	 * 
	 * @return returns the current filter pattern or
	 *         <code>null<code> if filter was not set.
	 */
	public String getFilter() {
		if (fFilteredList == null)
			return fFilter;
		return fFilteredList.getFilter();
	}

	/**
	 * Returns all elements which are folded together to one entry in the list.
	 * 
	 * @param index
	 *            the index selecting the entry in the list.
	 * @return returns an array of elements folded together.
	 */
	public Object[] getFoldedElements(final int index) {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getFoldedElements(index);
	}

	/**
	 * Returns an array of the currently selected elements. To be called within
	 * or after open().
	 * 
	 * @return returns an array of the currently selected elements.
	 */
	protected Object[] getSelectedElements() {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getSelection();
	}

	/**
	 * Returns an index referring the first current selection. To be called
	 * within open().
	 * 
	 * @return returns the indices of the current selection.
	 */
	protected int getSelectionIndex() {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getSelectionIndex();
	}

	/**
	 * Returns the indices referring the current selection. To be called within
	 * open().
	 * 
	 * @return returns the indices of the current selection.
	 */
	protected int[] getSelectionIndices() {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getSelectionIndices();
	}

	/**
	 * Gets the optional validator used to check if the selection is valid. The
	 * validator is invoked whenever the selection changes.
	 * 
	 * @return the validator to validate the selection, or <code>null</code> if
	 *         no validator has been set.
	 * @since 1.4
	 */
	protected ISelectionStatusValidator getValidator() {
		return fValidator;
	}

	/**
	 * Handles default selection (double click). By default, the OK button is
	 * pressed.
	 */
	protected void handleDefaultSelected() {
		if (validateCurrentSelection()) {
			buttonPressed(IDialogConstants.OK_ID);
		}
	}

	/**
	 * Handles empty list by disabling widgets.
	 */
	protected void handleEmptyList() {
		fMessage.setEnabled(false);
		fFilterText.setEnabled(false);
		fFilteredList.setEnabled(false);
		updateOkState();
	}

	/**
	 * Handles a selection changed event. By default, the current selection is
	 * validated.
	 */
	protected void handleSelectionChanged() {
		validateCurrentSelection();
	}

	// 3515
	private void handleWidgetSelected() {
		final Object[] newSelection = fFilteredList.getSelection();

		if (newSelection.length != fSelection.length) {
			fSelection = newSelection;
			handleSelectionChanged();
		} else {
			for (int i = 0; i != newSelection.length; i++) {
				if (!newSelection[i].equals(fSelection[i])) {
					fSelection = newSelection;
					handleSelectionChanged();
					break;
				}
			}
		}
	}

	/**
	 * Returns if sorting, filtering and folding is case sensitive.
	 * 
	 * @return boolean
	 */
	public boolean isCaseIgnored() {
		return fIgnoreCase;
	}

	/**
	 * Specifies whether duplicate entries are displayed or not.
	 * 
	 * @param allowDuplicates
	 */
	public void setAllowDuplicates(final boolean allowDuplicates) {
		fAllowDuplicates = allowDuplicates;
	}

	/**
	 * Sets the message to be displayed if the list is empty.
	 * 
	 * @param message
	 *            the message to be displayed.
	 */
	public void setEmptyListMessage(final String message) {
		fEmptyListMessage = message;
	}

	/**
	 * Sets the message to be displayed if the selection is empty.
	 * 
	 * @param message
	 *            the message to be displayed.
	 */
	public void setEmptySelectionMessage(final String message) {
		fEmptySelectionMessage = message;
	}

	/**
	 * Sets the filter pattern.
	 * 
	 * @param filter
	 *            the filter pattern.
	 */
	public void setFilter(final String filter) {
		if (fFilterText == null) {
			fFilter = filter;
		} else {
			fFilterText.setText(filter);
		}
	}

	/**
	 * Specifies if sorting, filtering and folding is case sensitive.
	 * 
	 * @param ignoreCase
	 */
	public void setIgnoreCase(final boolean ignoreCase) {
		fIgnoreCase = ignoreCase;
	}

	/**
	 * Sets the elements of the list (widget). To be called within open().
	 * 
	 * @param elements
	 *            the elements of the list.
	 */
	protected void setListElements(final Object[] elements) {
		Assert.isNotNull(fFilteredList);
		fFilteredList.setElements(elements);
	}

	/**
	 * Specifies whether everything or nothing should be filtered on empty
	 * filter string.
	 * 
	 * @param matchEmptyString
	 *            boolean
	 */
	public void setMatchEmptyString(final boolean matchEmptyString) {
		fMatchEmptyString = matchEmptyString;
	}

	/**
	 * Specifies if multiple selection is allowed.
	 * 
	 * @param multipleSelection
	 */
	public void setMultipleSelection(final boolean multipleSelection) {
		fIsMultipleSelection = multipleSelection;
	}

	/**
	 * Sets the selection referenced by an array of elements. Empty or null
	 * array removes selection. To be called within open().
	 * 
	 * @param selection
	 *            the indices of the selection.
	 */
	protected void setSelection(final Object[] selection) {
		Assert.isNotNull(fFilteredList);
		fFilteredList.setSelection(selection);
	}

	/**
	 * Sets the list size in unit of characters.
	 * 
	 * @param width
	 *            the width of the list.
	 * @param height
	 *            the height of the list.
	 */
	public void setSize(final int width, final int height) {
		fWidth = width;
		fHeight = height;
	}

	/**
	 * Sets an optional validator to check if the selection is valid. The
	 * validator is invoked whenever the selection changes.
	 * 
	 * @param validator
	 *            the validator to validate the selection.
	 */
	public void setValidator(final ISelectionStatusValidator validator) {
		fValidator = validator;
	}

	/**
	 * Update the enablement of the OK button based on whether or not there is a
	 * selection.
	 */
	protected void updateOkState() {
		final Button okButton = getOkButton();
		if (okButton != null) {
			okButton.setEnabled(getSelectedElements().length != 0);
		}
	}

	/**
	 * Validates the current selection and updates the status line accordingly.
	 * 
	 * @return boolean <code>true</code> if the current selection is valid.
	 */
	protected boolean validateCurrentSelection() {
		Assert.isNotNull(fFilteredList);

		IStatus status;
		final Object[] elements = getSelectedElements();

		if (elements.length > 0) {
			if (fValidator != null) {
				status = fValidator.validate(elements);
			} else {
				status = new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, "", //$NON-NLS-1$
						null);
			}
		} else {
			if (fFilteredList.isEmpty()) {
				status = new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, IStatus.ERROR, fEmptyListMessage, null);
			} else {
				status = new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, IStatus.ERROR, fEmptySelectionMessage, null);
			}
		}

		updateStatus(status);

		return status.isOK();
	}
}
