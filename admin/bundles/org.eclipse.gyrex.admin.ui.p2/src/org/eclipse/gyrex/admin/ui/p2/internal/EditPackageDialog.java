/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Peter Grube - rework new Admin UI
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.p2.internal.packages.IPackageManager;
import org.eclipse.gyrex.p2.internal.packages.InstallableUnitReference;
import org.eclipse.gyrex.p2.internal.packages.PackageDefinition;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import org.apache.commons.lang.StringUtils;

public class EditPackageDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final ILabelProvider labelProvider = new P2UiLabelProvider();

	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField nodeFilterField = new StringDialogField();
	private final ListDialogField componentsField = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			if (index == 0) {
				addComponentButtonPressed();
			}
		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			// nothing

		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			// nothing
		}
	}, new String[] { "Add...", "Remove" }, labelProvider);

	private final IPackageManager packageManager;
	private final PackageDefinition packageToEdit;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param packageManager
	 *            the manager to be used
	 * @param packageToEdit
	 *            the selected package to be edited, can be <code>null</code>
	 *            for creating a new package
	 */
	public EditPackageDialog(final Shell parent, final IPackageManager packageManager, final PackageDefinition packageToEdit) {
		super(parent);
		this.packageManager = packageManager;
		this.packageToEdit = packageToEdit;
		setTitle(null == packageToEdit ? "Add Software Package" : "Edit Software Package");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	void addComponentButtonPressed() {
		// query for everything that provides an OSGi bundle and features
		final IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("properties[$0] == true || providedCapabilities.exists(p | p.namespace == 'osgi.bundle')", new Object[] { MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP }); //$NON-NLS-1$

		// create the query for features
//		final IQuery<IInstallableUnit> query = QueryUtil.createIUGroupQuery();

		final FilteredIUSelectionDialog dialog = new FilteredIUSelectionDialog(getShell(), query);
		dialog.openNonBlocking(new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					final Object[] result = dialog.getResult();
					if (result != null) {
						for (int i = 0; i < result.length; i++) {
							if (result[i] instanceof IInstallableUnit) {
								final IInstallableUnit iu = (IInstallableUnit) result[i];
								final InstallableUnitReference unit = new InstallableUnitReference();
								unit.setId(iu.getId());
								unit.setVersion(iu.getVersion());
								componentsField.addElement(unit);
							}
						}
					}
				}
			}
		});
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		nodeFilterField.setLabelText("Node Filter");

		componentsField.setRemoveButtonIndex(1);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		nodeFilterField.setDialogFieldListener(validateListener);

		final Text info = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		if (packageToEdit == null) {
			info.setText("Software packages are identified in the system using a unique id. Please define one below. An optional node filter allows to restrict the installation of packages to only those cluster nodes matching the filter. \n\nComponents to install should be added in the list below.");
		} else {
			info.setText("The optional node filter allows to restrict the installation of packages to only those cluster nodes matching the filter. Components to install should be added in the list below.");
		}
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		gd.widthHint = convertHorizontalDLUsToPixels(380);
		info.setLayoutData(gd);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, nodeFilterField, new Separator(), componentsField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(componentsField.getListControl(null));

		if (packageToEdit != null) {
			idField.setText(StringUtils.trimToEmpty(packageToEdit.getId()));
			idField.getTextControl(null).setEditable(false);
			nodeFilterField.setText(StringUtils.trimToEmpty(packageToEdit.getNodeFilter()));
			componentsField.setElements(packageToEdit.getComponentsToInstall());
		}

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(info, masterLayout.numColumns);

		return composite;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK())
			return;

		try {
			final PackageDefinition packageDefinition = new PackageDefinition();
			packageDefinition.setId(idField.getText());
			packageDefinition.setNodeFilter(StringUtils.trimToNull(nodeFilterField.getText()));
			final List components = componentsField.getElements();
			for (final Object component : components) {
				packageDefinition.addComponentToInstall((InstallableUnitReference) component);
			}
			packageManager.savePackage(packageDefinition);
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, P2UiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, P2UiActivator.SYMBOLIC_NAME, message));
	}

	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, P2UiActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isNotBlank(id)) {
			if (!IdHelper.isValidId(id)) {
				setError("The entered connector id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
				return;
			}
			if ((packageToEdit == null) && (null != packageManager.getPackage(id))) {
				setError("A package with the specified id already exists!");
				return;
			}
		}

		final String nodeFilter = nodeFilterField.getText();
		if (StringUtils.isNotBlank(nodeFilter)) {
			try {
				FrameworkUtil.createFilter(nodeFilter);
			} catch (final InvalidSyntaxException e) {
				setError("The entered node filter is invalid. Please use valid LDAP filter syntax. " + e.getMessage());
				return;
			}
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a connector id.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
