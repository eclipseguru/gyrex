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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A list with a button bar. Typical buttons are 'Add', 'Remove', 'Up' and
 * 'Down'. List model is independent of widget creation. DialogFields controls
 * are: Label, List and Composite containing buttons.
 */
public class ListDialogField extends DialogField {

	public static class ColumnsDescription {
		private static ColumnLayoutData[] createColumnWeightData(final int nColumns) {
			final ColumnLayoutData[] data = new ColumnLayoutData[nColumns];
			for (int i = 0; i < nColumns; i++) {
				data[i] = new ColumnWeightData(1);
			}
			return data;
		}

		private final ColumnLayoutData[] columns;
		private final String[] headers;

		private final boolean drawLines;

		public ColumnsDescription(final ColumnLayoutData[] columns, final String[] headers, final boolean drawLines) {
			this.columns = columns;
			this.headers = headers;
			this.drawLines = drawLines;
		}

		public ColumnsDescription(final int nColumns, final boolean drawLines) {
			this(createColumnWeightData(nColumns), null, drawLines);
		}

		public ColumnsDescription(final String[] headers, final boolean drawLines) {
			this(createColumnWeightData(headers.length), headers, drawLines);
		}
	}

	private class ListViewerAdapter implements IStructuredContentProvider, ISelectionChangedListener, IDoubleClickListener {

		// ------- ITableContentProvider Interface ------------

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		@Override
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
		 */
		@Override
		public void doubleClick(final DoubleClickEvent event) {
			doDoubleClick(event);
		}

		@Override
		public Object[] getElements(final Object obj) {
			return fElements.toArray();
		}

		// ------- ISelectionChangedListener Interface ------------

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// will never happen
		}

		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			doListSelected(event);
		}

	}

	protected TableViewer fTable;
	protected Control fTableControl;
	protected ILabelProvider fLabelProvider;
	protected ListViewerAdapter fListViewerAdapter;
	protected List<Object> fElements;

	protected ViewerComparator fViewerComparator;
	protected String[] fButtonLabels;

	private Button[] fButtonControls;

	private boolean[] fButtonsEnabled;
	private int fRemoveButtonIndex;
	private int fUpButtonIndex;

	private int fDownButtonIndex;

	private Label fLastSeparator;
	private Composite fButtonsControl;

	private ISelection fSelectionWhenEnabled;

	private final IListAdapter fListAdapter;

	private final Object fParentElement;

	private ColumnsDescription fTableColumns;

	/**
	 * Creates the <code>ListDialogField</code>.
	 * 
	 * @param adapter
	 *            A listener for button invocation, selection changes. Can be
	 *            <code>null</code>.
	 * @param buttonLabels
	 *            The labels of all buttons: <code>null</code> is a valid array
	 *            entry and marks a separator.
	 * @param lprovider
	 *            The label provider to render the table entries
	 */
	public ListDialogField(final IListAdapter adapter, final String[] buttonLabels, final ILabelProvider lprovider) {
		super();
		fListAdapter = adapter;

		fLabelProvider = lprovider;
		fListViewerAdapter = new ListViewerAdapter();
		fParentElement = this;

		fElements = new ArrayList<Object>(10);

		fButtonLabels = buttonLabels;
		if (fButtonLabels != null) {
			final int nButtons = fButtonLabels.length;
			fButtonsEnabled = new boolean[nButtons];
			for (int i = 0; i < nButtons; i++) {
				fButtonsEnabled[i] = true;
			}
		}

		fTable = null;
		fTableControl = null;
		fButtonsControl = null;
		fTableColumns = null;

		fRemoveButtonIndex = -1;
		fUpButtonIndex = -1;
		fDownButtonIndex = -1;
	}

	/**
	 * Adds an element at the end of the list.
	 */
	public boolean addElement(final Object element) {
		return addElement(element, fElements.size());
	}

	/**
	 * Adds an element at a position.
	 */
	public boolean addElement(final Object element, final int index) {
		if (fElements.contains(element)) {
			return false;
		}
		fElements.add(index, element);
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
			fTable.setSelection(new StructuredSelection(element));
		}

		dialogFieldChanged();
		return true;
	}

	/**
	 * Adds elements at the end of the list.
	 */
	public boolean addElements(final List elements) {
		return addElements(elements, fElements.size());
	}

	/**
	 * Adds elements at the given index
	 */
	public boolean addElements(final List elements, final int index) {

		final int nElements = elements.size();

		if (nElements > 0 && index >= 0 && index <= fElements.size()) {
			// filter duplicated
			final ArrayList<Object> elementsToAdd = new ArrayList<Object>(nElements);

			for (int i = 0; i < nElements; i++) {
				final Object elem = elements.get(i);
				if (!fElements.contains(elem)) {
					elementsToAdd.add(elem);
				}
			}
			if (!elementsToAdd.isEmpty()) {
				fElements.addAll(index, elementsToAdd);
				if (isOkToUse(fTableControl)) {
					if (index == fElements.size()) {
						fTable.add(elementsToAdd.toArray());
					} else {
						for (int i = elementsToAdd.size() - 1; i >= 0; i--) {
							fTable.insert(elementsToAdd.get(i), index);
						}
					}
					fTable.setSelection(new StructuredSelection(elementsToAdd));
				}
				dialogFieldChanged();
				return true;
			}
		}
		return false;
	}

	// ------ adapter communication

	private void buttonPressed(final int index) {
		if (!managedButtonPressed(index) && fListAdapter != null) {
			fListAdapter.customButtonPressed(this, index);
		}
	}

	public boolean canMoveDown() {
		if (isOkToUse(fTableControl)) {
			final int[] indc = fTable.getTable().getSelectionIndices();
			int k = fElements.size() - 1;
			for (int i = indc.length - 1; i >= 0; i--, k--) {
				if (indc[i] != k) {
					return true;
				}
			}
		}
		return false;
	}

	// ------ layout helpers

	public boolean canMoveUp() {
		if (isOkToUse(fTableControl)) {
			final int[] indc = fTable.getTable().getSelectionIndices();
			for (int i = 0; i < indc.length; i++) {
				if (indc[i] != i) {
					return true;
				}
			}
		}
		return false;
	}

	protected Button createButton(final Composite parent, final String label, final SelectionListener listener) {
		final Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		button.addSelectionListener(listener);
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.widthHint = SwtUtil.getButtonWidthHint(button);

		button.setLayoutData(gd);

		return button;
	}

	private Label createSeparator(final Composite parent) {
		final Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setFont(parent.getFont());
		separator.setVisible(false);
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.verticalIndent = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	// ------- focus methods

	protected TableViewer createTableViewer(final Composite parent) {
		final Table table = new Table(parent, getListStyle());
		table.setFont(parent.getFont());
		return new TableViewer(table);
	}

	// ------ UI creation

	/*
	 * @see DialogField#dialogFieldChanged
	 */
	@Override
	public void dialogFieldChanged() {
		super.dialogFieldChanged();
		updateButtonState();
	}

	private void doButtonSelected(final SelectionEvent e) {
		if (fButtonControls != null) {
			for (int i = 0; i < fButtonControls.length; i++) {
				if (e.widget == fButtonControls[i]) {
					buttonPressed(i);
					return;
				}
			}
		}
	}

	protected void doDoubleClick(final DoubleClickEvent event) {
		if (fListAdapter != null) {
			fListAdapter.doubleClicked(this);
		}
	}

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		final PixelConverter converter = new PixelConverter(parent);

		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		GridData gd = gridDataForLabel(1);
		gd.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		final Control list = getListControl(parent);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = nColumns - 2;
		gd.widthHint = converter.convertWidthInCharsToPixels(50);
		gd.heightHint = converter.convertHeightInCharsToPixels(6);

		list.setLayoutData(gd);

		final Composite buttons = getButtonBox(parent);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 1;
		buttons.setLayoutData(gd);

		return new Control[] { label, list, buttons };
	}

	protected void doListSelected(final SelectionChangedEvent event) {
		updateButtonState();
		if (fListAdapter != null) {
			fListAdapter.selectionChanged(this);
		}
	}

	private void down() {
		moveDown(getSelectedElements());
	}

	public void editElement(final Object element) {
		if (isOkToUse(fTableControl)) {
			fTable.refresh(element);
			fTable.editElement(element, 0);
		}
	}

	/**
	 * Notifies clients that the element has changed.
	 */
	public void elementChanged(final Object element) throws IllegalArgumentException {
		if (fElements.contains(element)) {
			if (isOkToUse(fTableControl)) {
				fTable.update(element, null);
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Sets a button enabled or disabled.
	 */
	public void enableButton(final int index, final boolean enable) {
		if (fButtonsEnabled != null && index < fButtonsEnabled.length) {
			fButtonsEnabled[index] = enable;
			updateButtonState();
		}
	}

	// ------ enable / disable management

	/**
	 * Returns the composite containing the buttons. When called the first time,
	 * the control will be created.
	 * 
	 * @param parent
	 *            The parent composite when called the first time, or
	 *            <code>null</code> after.
	 */
	public Composite getButtonBox(final Composite parent) {
		if (fButtonsControl == null) {
			assertCompositeNotNull(parent);

			final SelectionListener listener = new SelectionListener() {
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetDefaultSelected(final SelectionEvent e) {
					doButtonSelected(e);
				}

				@Override
				public void widgetSelected(final SelectionEvent e) {
					doButtonSelected(e);
				}
			};

			final Composite contents = new Composite(parent, SWT.NONE);
			contents.setFont(parent.getFont());
			final GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			contents.setLayout(layout);

			if (fButtonLabels != null) {
				fButtonControls = new Button[fButtonLabels.length];
				for (int i = 0; i < fButtonLabels.length; i++) {
					final String currLabel = fButtonLabels[i];
					if (currLabel != null) {
						fButtonControls[i] = createButton(contents, currLabel, listener);
						fButtonControls[i].setEnabled(isEnabled() && fButtonsEnabled[i]);
					} else {
						fButtonControls[i] = null;
						createSeparator(contents);
					}
				}
			}

			fLastSeparator = createSeparator(contents);

			updateButtonState();
			fButtonsControl = contents;
		}

		return fButtonsControl;
	}

	/**
	 * Gets the elements shown at the given index.
	 */
	public Object getElement(final int index) {
		return fElements.get(index);
	}

	/**
	 * Gets the elements shown in the list. The list returned is a copy, so it
	 * can be modified by the user.
	 */
	public List<Object> getElements() {
		return new ArrayList<Object>(fElements);
	}

	/**
	 * Gets the index of an element in the list or -1 if element is not in list.
	 */
	public int getIndexOfElement(final Object elem) {
		return fElements.indexOf(elem);
	}

	/**
	 * Returns the list control. When called the first time, the control will be
	 * created.
	 * 
	 * @param parent
	 *            The parent composite when called the first time, or
	 *            <code>null</code> after.
	 */
	public Control getListControl(final Composite parent) {
		if (fTableControl == null) {
			assertCompositeNotNull(parent);

			if (fTableColumns == null) {
				fTable = createTableViewer(parent);
				final Table tableControl = fTable.getTable();

				fTableControl = tableControl;
				tableControl.setLayout(new TableLayout());
			} else {
				final TableColumnLayout layout = new TableColumnLayout();
				final Composite composite = new Composite(parent, SWT.NONE);
				composite.setFont(parent.getFont());
				composite.setLayout(layout);
				fTableControl = composite;

				fTable = createTableViewer(composite);
				final Table tableControl = fTable.getTable();

				tableControl.setHeaderVisible(fTableColumns.headers != null);
				tableControl.setLinesVisible(fTableColumns.drawLines);
				final ColumnLayoutData[] columns = fTableColumns.columns;
				for (int i = 0; i < columns.length; i++) {
					final TableColumn column = new TableColumn(tableControl, SWT.NONE);
					layout.setColumnData(column, columns[i]);
					//tableLayout.addColumnData(columns[i]);
					if (fTableColumns.headers != null) {
						column.setText(fTableColumns.headers[i]);
					}
				}
			}

			fTable.getTable().addKeyListener(new KeyAdapter() {
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public void keyPressed(final KeyEvent e) {
					handleKeyPressed(e);
				}
			});

			//fTableControl.setLayout(tableLayout);

			fTable.setContentProvider(fListViewerAdapter);
			fTable.setLabelProvider(fLabelProvider);
			fTable.addSelectionChangedListener(fListViewerAdapter);
			fTable.addDoubleClickListener(fListViewerAdapter);

			fTable.setInput(fParentElement);

			if (fViewerComparator != null) {
				fTable.setComparator(fViewerComparator);
			}

			fTableControl.setEnabled(isEnabled());
			if (fSelectionWhenEnabled != null) {
				selectElements(fSelectionWhenEnabled);
			}
		}
		return fTableControl;
	}

	/*
	 * Subclasses may override to specify a different style.
	 */
	protected int getListStyle() {
		int style = SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;
		if (fTableColumns != null) {
			style |= SWT.FULL_SELECTION;
		}
		return style;
	}

	// ------ model access

	protected boolean getManagedButtonState(final ISelection sel, final int index) {
		if (index == fRemoveButtonIndex) {
			return !sel.isEmpty();
		} else if (index == fUpButtonIndex) {
			return !sel.isEmpty() && canMoveUp();
		} else if (index == fDownButtonIndex) {
			return !sel.isEmpty() && canMoveDown();
		}
		return true;
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	@Override
	public int getNumberOfControls() {
		return 3;
	}

	/**
	 * Returns the selected elements.
	 */
	public List<Object> getSelectedElements() {
		final List<Object> result = new ArrayList<Object>();
		if (isOkToUse(fTableControl)) {
			final ISelection selection = fTable.getSelection();
			if (selection instanceof IStructuredSelection) {
				final Iterator iter = ((IStructuredSelection) selection).iterator();
				while (iter.hasNext()) {
					result.add(iter.next());
				}
			}
		}
		return result;
	}

	/**
	 * Gets the number of elements
	 */
	public int getSize() {
		return fElements.size();
	}

	/**
	 * Returns the internally used table viewer.
	 */
	public TableViewer getTableViewer() {
		return fTable;
	}

	/**
	 * Handles key events in the table viewer. Specifically when the delete key
	 * is pressed.
	 */
	protected void handleKeyPressed(final KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (fRemoveButtonIndex != -1 && isButtonEnabled(fTable.getSelection(), fRemoveButtonIndex)) {
				managedButtonPressed(fRemoveButtonIndex);
			}
		}
	}

	private boolean isButtonEnabled(final ISelection sel, final int index) {
		final boolean extraState = getManagedButtonState(sel, index);
		return isEnabled() && extraState && fButtonsEnabled[index];
	}

	/**
	 * Checks if the button pressed is handled internally
	 * 
	 * @return Returns true if button has been handled.
	 */
	protected boolean managedButtonPressed(final int index) {
		if (index == fRemoveButtonIndex) {
			remove();
		} else if (index == fUpButtonIndex) {
			up();
			if (!fButtonControls[index].isEnabled() && fDownButtonIndex != -1) {
				fButtonControls[fDownButtonIndex].setFocus();
			}
		} else if (index == fDownButtonIndex) {
			down();
			if (!fButtonControls[index].isEnabled() && fUpButtonIndex != -1) {
				fButtonControls[fUpButtonIndex].setFocus();
			}
		} else {
			return false;
		}
		return true;
	}

	private void moveDown(final List toMoveDown) {
		if (toMoveDown.size() > 0) {
			setElements(reverse(moveUp(reverse(fElements), toMoveDown)));
			fTable.reveal(toMoveDown.get(toMoveDown.size() - 1));
		}
	}

	private void moveUp(final List toMoveUp) {
		if (toMoveUp.size() > 0) {
			setElements(moveUp(fElements, toMoveUp));
			fTable.reveal(toMoveUp.get(0));
		}
	}

	private List<?> moveUp(final List elements, final List move) {
		final int nElements = elements.size();
		final List<Object> res = new ArrayList<Object>(nElements);
		Object floating = null;
		for (int i = 0; i < nElements; i++) {
			final Object curr = elements.get(i);
			if (move.contains(curr)) {
				res.add(curr);
			} else {
				if (floating != null) {
					res.add(floating);
				}
				floating = curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		return res;
	}

	public void postSetSelection(final ISelection selection) {
		if (isOkToUse(fTableControl)) {
			final Display d = fTableControl.getDisplay();
			d.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (isOkToUse(fTableControl)) {
						selectElements(selection);
					}
				}
			});
		}
	}

	/**
	 * Refreshes the table.
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
		}
	}

	private void remove() {
		removeElements(getSelectedElements());
	}

	/**
	 * Adds an element at a position.
	 */
	public void removeAllElements() {
		if (fElements.size() > 0) {
			fElements.clear();
			if (isOkToUse(fTableControl)) {
				fTable.refresh();
			}
			dialogFieldChanged();
		}
	}

	/**
	 * Removes an element from the list.
	 */
	public void removeElement(final Object element) throws IllegalArgumentException {
		if (fElements.remove(element)) {
			if (isOkToUse(fTableControl)) {
				fTable.remove(element);
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Removes elements from the list.
	 */
	public void removeElements(final List elements) {
		if (elements.size() > 0) {
			fElements.removeAll(elements);
			if (isOkToUse(fTableControl)) {
				fTable.remove(elements.toArray());
			}
			dialogFieldChanged();
		}
	}

	/**
	 * Replaces an element.
	 */
	public void replaceElement(final Object oldElement, final Object newElement) throws IllegalArgumentException {
		final int idx = fElements.indexOf(oldElement);
		if (idx != -1) {
			fElements.set(idx, newElement);
			if (isOkToUse(fTableControl)) {
				final List<Object> selected = getSelectedElements();
				if (selected.remove(oldElement)) {
					selected.add(newElement);
				}
				fTable.refresh();
				selectElements(new StructuredSelection(selected));
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private List<?> reverse(final List p) {
		final List<Object> reverse = new ArrayList<Object>(p.size());
		for (int i = p.size() - 1; i >= 0; i--) {
			reverse.add(p.get(i));
		}
		return reverse;
	}

	// ------- list maintenance

	public void selectElements(final ISelection selection) {
		fSelectionWhenEnabled = selection;
		if (isOkToUse(fTableControl)) {
			fTable.setSelection(selection, true);
		}
	}

	public void selectFirstElement() {
		Object element = null;
		if (fViewerComparator != null) {
			final Object[] arr = fElements.toArray();
			fViewerComparator.sort(fTable, arr);
			if (arr.length > 0) {
				element = arr[0];
			}
		} else {
			if (fElements.size() > 0) {
				element = fElements.get(0);
			}
		}
		if (element != null) {
			selectElements(new StructuredSelection(element));
		}
	}

	/**
	 * Sets the minimal width of the buttons. Must be called after widget
	 * creation.
	 */
	public void setButtonsMinWidth(final int minWidth) {
		if (fLastSeparator != null) {
			((GridData) fLastSeparator.getLayoutData()).widthHint = minWidth;
		}
	}

	/**
	 * Sets the index of the 'down' button in the button label array passed in
	 * the constructor. The behavior of the button marked as the 'down' button
	 * will then be handled internally. (enable state, button invocation
	 * behavior)
	 */
	public void setDownButtonIndex(final int downButtonIndex) {
		Assert.isTrue(downButtonIndex < fButtonLabels.length);
		fDownButtonIndex = downButtonIndex;
	}

	/**
	 * Sets the elements shown in the list.
	 */
	public void setElements(final Collection<?> elements) {
		fElements = new ArrayList<Object>(elements);
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
		}
		dialogFieldChanged();
	}

	/*
	 * @see DialogField#setFocus
	 */
	@Override
	public boolean setFocus() {
		if (fTable != null && isOkToUse(fTable.getControl())) {
			fTable.getControl().setFocus();
		}
		return true;
	}

	/**
	 * Sets the index of the 'remove' button in the button label array passed in
	 * the constructor. The behavior of the button marked as the 'remove' button
	 * will then be handled internally. (enable state, button invocation
	 * behavior)
	 */
	public void setRemoveButtonIndex(final int removeButtonIndex) {
		Assert.isTrue(removeButtonIndex < fButtonLabels.length);
		fRemoveButtonIndex = removeButtonIndex;
	}

	public void setTableColumns(final ColumnsDescription column) {
		fTableColumns = column;
	}

	/**
	 * Sets the index of the 'up' button in the button label array passed in the
	 * constructor. The behavior of the button marked as the 'up' button will
	 * then be handled internally. (enable state, button invocation behavior)
	 */
	public void setUpButtonIndex(final int upButtonIndex) {
		Assert.isTrue(upButtonIndex < fButtonLabels.length);
		fUpButtonIndex = upButtonIndex;
	}

	/**
	 * Sets the viewer comparator.
	 * 
	 * @param viewerComparator
	 *            The viewer comparator to set
	 */
	public void setViewerComparator(final ViewerComparator viewerComparator) {
		fViewerComparator = viewerComparator;
	}

	private void up() {
		moveUp(getSelectedElements());
	}

	/*
	 * Updates the enable state of the all buttons
	 */
	protected void updateButtonState() {
		if (fButtonControls != null && isOkToUse(fTableControl) && fTableControl.isEnabled()) {
			final ISelection sel = fTable.getSelection();
			for (int i = 0; i < fButtonControls.length; i++) {
				final Button button = fButtonControls[i];
				if (isOkToUse(button)) {
					button.setEnabled(isButtonEnabled(sel, i));
				}
			}
		}
	}

	/*
	 * @see DialogField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();

		final boolean enabled = isEnabled();
		if (isOkToUse(fTableControl)) {
			if (!enabled) {
				if (fSelectionWhenEnabled == null) {
					fSelectionWhenEnabled = fTable.getSelection();
					selectElements(null);
				}
			} else if (fSelectionWhenEnabled != null) {
				selectElements(fSelectionWhenEnabled);
				fSelectionWhenEnabled = null;
			}
			fTableControl.setEnabled(enabled);
		}
		updateButtonState();
	}

}
