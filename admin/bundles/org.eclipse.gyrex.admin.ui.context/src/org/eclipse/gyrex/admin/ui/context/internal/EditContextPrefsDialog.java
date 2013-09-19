/**
 * Copyright (c) 2013 Andreas Mihm and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Mihm - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.context.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

/**
 * The Class represents RAP UI non blocking status dialog to maintain the
 * preferences of a context.
 */
public class EditContextPrefsDialog extends NonBlockingStatusDialog {

	/**
	 * Helper class to hold the preferences and provide them to the table view
	 */
	private class KeyValuePair {

		public String name;
		public String value;

		public KeyValuePair(final String name, final String value) {
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * Comparator to enable sorting of the table view
	 */
	private static final class KeyValuePairComparator extends ViewerComparator implements Comparator<KeyValuePair> {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private final boolean ascending;
		private final int property;

		public KeyValuePairComparator(final int property, final boolean ascending) {
			this.property = property;
			this.ascending = ascending;
		}

		@Override
		public int compare(final KeyValuePair pair1, final KeyValuePair pair22) {
			int result = 0;
			if (property == COL_NAME) {
				result = pair1.name.compareTo(pair22.name);
			} else if (property == COL_VALUE) {
				result = pair1.value.compareTo(pair22.value);
			}
			if (!ascending) {
				result = result * -1;
			}
			return result;
		}

		@Override
		public int compare(final Viewer viewer, final Object object1, final Object object2) {
			return compare((KeyValuePair) object1, (KeyValuePair) object2);
		}

		@Override
		public boolean isSorterProperty(final Object elem, final String property) {
			return true;
		}
	}

	/**
	 * ContentProvider class for binding the List of Preferences to the table
	 * view
	 */
	private static final class PrefsContentProvider implements IStructuredContentProvider {
		Object[] elements;

		@Override
		public void dispose() {
			// do nothing
		}

		@Override
		public Object[] getElements(final Object inputElement) {
			return elements;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			if (newInput == null) {
				elements = new Object[0];
			} else {
				final List<?> preferenceList = (List<?>) newInput;
				elements = preferenceList.toArray();
			}
		}
	}

	/**
	 * Label provider for the table view
	 */
	private class PrefsLabelProvider extends ColumnLabelProvider {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private final int columnIndex;

		public PrefsLabelProvider(final int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public String getText(final Object element) {
			final KeyValuePair pref = (KeyValuePair) element;
			String result = pref.toString();
			switch (columnIndex) {
				case COL_NAME:
					result = pref.name;
					break;
				case COL_VALUE:
					result = pref.value;
					break;
			}
			return result;
		}

	}

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private static final int COL_NAME = 0;

	private static final int COL_VALUE = 1;

	/**
	 * sort method for sorting the preference table view
	 * 
	 * @param viewer
	 * @param property
	 * @param ascending
	 */
	@SuppressWarnings("unchecked")
	private static void sort(final TableViewer viewer, final int property, final boolean ascending) {
		if ((viewer.getControl().getStyle() & SWT.VIRTUAL) != 0) {
			final List<KeyValuePair> input = (List<KeyValuePair>) viewer.getInput();
			Collections.sort(input, new KeyValuePairComparator(property, ascending));
			viewer.refresh();
		} else {
			viewer.setComparator(new KeyValuePairComparator(property, ascending));
		}
	}

	/**
	 * change sort direction
	 * 
	 * @param column
	 * @return
	 */
	private static int updateSortDirection(final TableColumn column) {
		final Table table = column.getParent();
		if (column == table.getSortColumn()) {
			if (table.getSortDirection() == SWT.UP) {
				table.setSortDirection(SWT.DOWN);
			} else {
				table.setSortDirection(SWT.UP);
			}
		} else {
			table.setSortColumn(column);
			table.setSortDirection(SWT.DOWN);
		}
		return table.getSortDirection();
	}

	/** The name field. */
	private final StringDialogField nameField = new StringDialogField();
	/** The value field. */
	private final StringDialogField valueField = new StringDialogField();

	private final StringDialogField qualifierField = new StringDialogField();
	/** The registry impl. */
	private final IRuntimeContextDefinitionManager registryImpl = null;

	/** input list for the preferences table */
	private List<KeyValuePair> prefSettings;
	/** input list for the qualifiers drop down */
	private List<Preferences> prefQualifiers;

	/** the table view for the preferences */
	private TableViewer viewer;

	private TableViewerColumn nameColumn;

	private TableViewerColumn valueColumn;
	private final Preferences prefRootNode;
	private Preferences currentPrefNode;
	private Button addOrUpdateButton;
	private Button deleteButton;
	private Button addQualifierButton;
	private Combo qualifierCombo;
	private Label label;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the parent UI element
	 * @param registryImpl
	 *            the registry impl
	 */
	public EditContextPrefsDialog(final Shell parent, final Preferences prefNode) {
		super(parent);
		prefRootNode = prefNode;
		currentPrefNode = prefNode;
		prefSettings = new ArrayList<KeyValuePair>();
		prefQualifiers = new ArrayList<Preferences>();
		initContent();
		//setTitle("View and Edit Context Preferences in " + prefNode.absolutePath());
		setTitle("View and Edit Context Preferences");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	/**
	 * Button action for adding or updating a preference
	 */
	protected void addOrUpdateButtonPressed() {

		final Preferences node;
		final String qualifier = qualifierField.getText();
		if (StringUtils.isEmpty(qualifier)) {
			currentPrefNode = prefRootNode;
		} else {
			currentPrefNode = prefRootNode.node(qualifier);
		}

		final String name = nameField.getText();
		String value = valueField.getText();
		if (StringUtils.isEmpty(name))
			return;
		if (StringUtils.isEmpty(value)) {
			value = "";
		}
		currentPrefNode.put(name, value);
		try {
			currentPrefNode.flush();
			fillQualifierCombo();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			throw new UnhandledException(e);
		}

		changePreferencesQualifier(qualifier);

		nameField.setText("");
		valueField.setText("");
	}

	/**
	 * action on changing the qualifiers drop down selection, which updates the
	 * preferences in the table view
	 * 
	 * @param text
	 */
	protected void changePreferencesQualifier(String text) {

		if (StringUtils.isEmpty(text)) {
			text = ".settings";
		}

		qualifierCombo.setText(text);
		try {
			Preferences node = null;
			if (text.equals(".settings")) {
				node = prefRootNode;
				qualifierField.setText("");
			} else {
				node = prefRootNode.node(text);
				qualifierField.setText(text);
			}
			if (node != null) {
				currentPrefNode = node;
				prefSettings = new ArrayList<KeyValuePair>();
				final String[] settingNames = node.keys();
				for (final String settingName : settingNames) {
					final KeyValuePair pair = new KeyValuePair(settingName, node.get(settingName, null));
					prefSettings.add(pair);
				}
				viewer.setInput(prefSettings);
			}
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			throw new UnhandledException(e);
		}

	}

	/**
	 * Creates the button.
	 * 
	 * @param buttons
	 *            the buttons
	 * @param buttonLabel
	 *            the button label
	 * @return the button
	 */
	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button b = new Button(buttons, SWT.NONE);
		b.setText(buttonLabel);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		qualifierField.setLabelText("Preference Qualifier/Group");
		nameField.setLabelText("Preference Name");
		valueField.setLabelText("Preference Value");

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY | SWT.WRAP);
		warning.setText("All preference settings in the context " + prefRootNode.absolutePath() + " are listed below. \n" + "You can add new preferences or delete or edit existing ones. Please note, that Preferences in a Gyrex Context can be grouped by qualifiers.\n" + "You can select the qualifier/group in the drop down list. To create a new qualifier, just add a preference and enter a new qualifier.");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.LEFT, false, false));
		warning.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(composite, SWT.NONE);
		label.setText("Select Preference Qualifier/Group");
		qualifierCombo = new Combo(composite, SWT.NONE);
		try {
			fillQualifierCombo();
			qualifierCombo.select(0);
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			throw new UnhandledException(e);
		}
		qualifierCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				changePreferencesQualifier(qualifierCombo.getText());
			}
		});

		viewer = new TableViewer(composite, SWT.NONE | SWT.BORDER);
		viewer.setContentProvider(new PrefsContentProvider());
		nameColumn = createNameColumn();
		valueColumn = createValueColumn();

		viewer.setInput(prefSettings);
		viewer.setItemCount(prefSettings.size());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final KeyValuePair selectedValue = getSelectedPreferenceFromTable();
				if (selectedValue == null)
					return;
				nameField.setText(selectedValue.name);
				valueField.setText(selectedValue.value);
				updateStatus(Status.OK_STATUS);
			}
		});
		viewer.getTable().setHeaderVisible(true);

		final GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableData.horizontalSpan = 3;
		viewer.getTable().setLayoutData(tableData);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		nameField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), qualifierField, nameField, valueField }, false);
		LayoutUtil.setHorizontalGrabbing(nameField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(valueField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		addOrUpdateButton = createButton(composite, "Add/Update Preference");
		addOrUpdateButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addOrUpdateButtonPressed();
			}
		});

		deleteButton = createButton(composite, "Delete Preference");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				deleteButtonPressed();
			}
		});

		return composite;
	}

	/**
	 * another long method for just creating a column, should be simplier
	 * 
	 * @return
	 */
	private TableViewerColumn createNameColumn() {
		final TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
		result.setLabelProvider(new PrefsLabelProvider(COL_NAME));
		final TableColumn column = result.getColumn();
		column.setText("Name");
		column.setWidth(370);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final int sortDirection = updateSortDirection((TableColumn) event.widget);
				sort(viewer, COL_NAME, sortDirection == SWT.DOWN);
			}
		});
		return result;
	}

	/**
	 * another long method for just creating a column, should be simplier
	 * 
	 * @return
	 */
	private TableViewerColumn createValueColumn() {
		final TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
		result.setLabelProvider(new PrefsLabelProvider(COL_VALUE));
		final TableColumn column = result.getColumn();
		column.setText("Value");
		column.setWidth(170);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final int sortDirection = updateSortDirection((TableColumn) event.widget);
				sort(viewer, COL_VALUE, sortDirection == SWT.DOWN);
			}
		});
		return result;
	}

	/**
	 * button action to delete a selected preference
	 */
	protected void deleteButtonPressed() {
		final KeyValuePair selectedValue = getSelectedPreferenceFromTable();
		if (selectedValue == null) {
			setError("Please select a preference!");
			return;
		}
		currentPrefNode.remove(selectedValue.name);
		try {
			currentPrefNode.flush();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			throw new UnhandledException(e);
		}
		prefSettings.remove(selectedValue);
		viewer.setInput(prefSettings);

	}

	/**
	 * re reads the qualifiers of the context and updates the qualifiers Drop
	 * down list
	 * 
	 * @throws BackingStoreException
	 */
	private void fillQualifierCombo() throws BackingStoreException {
		prefQualifiers = new ArrayList<Preferences>();

		// add the root preferences node to list
		prefQualifiers.add(prefRootNode);

		final String[] childrenNames = prefRootNode.childrenNames();
		for (final String childNode : childrenNames) {
			final Preferences node = prefRootNode.node(childNode);
			prefQualifiers.add(node);
		}

		qualifierCombo.setItems(toStringArray(prefQualifiers));
	}

	/**
	 * @return the selected Preferences row from table
	 */
	private KeyValuePair getSelectedPreferenceFromTable() {
		final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof KeyValuePair))
			return (KeyValuePair) selection.getFirstElement();

		return null;
	}

	/**
	 * init method to read the content required before creating the controls
	 */
	private void initContent() {

		try {

			final String[] settingNames = prefRootNode.keys();
			for (final String settingName : settingNames) {
				System.out.println(" ZK Pref " + settingName + ":" + prefRootNode.get(settingName, null));
				final KeyValuePair pair = new KeyValuePair(settingName, prefRootNode.get(settingName, null));
				prefSettings.add(pair);
			}
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			throw new UnhandledException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {

		super.okPressed();
	}

	/**
	 * Sets the error.
	 * 
	 * @param message
	 *            the new error
	 */
	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, ContextUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	/**
	 * Sets the info.
	 * 
	 * @param message
	 *            the new info
	 */
	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, ContextUiActivator.SYMBOLIC_NAME, message));
	}

	/**
	 * Sets the warning.
	 * 
	 * @param message
	 *            the new warning
	 */
	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, ContextUiActivator.SYMBOLIC_NAME, message));
	}

	/**
	 * @param prefQualifiers2
	 * @return
	 */
	private String[] toStringArray(final List<Preferences> prefQualifiers2) {
		final ArrayList<String> list = new ArrayList<String>();
		for (final Preferences preferences : prefQualifiers2) {
			list.add(preferences.name());
		}
		return list.toArray(new String[0]);
	}

	/**
	 * Validate entered values.
	 */
	void validate() {
		updateStatus(Status.OK_STATUS);
	}

}
