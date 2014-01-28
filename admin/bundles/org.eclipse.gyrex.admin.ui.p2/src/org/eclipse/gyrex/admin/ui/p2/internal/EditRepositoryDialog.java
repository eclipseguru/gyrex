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
 *     Peter Grube	- rework new Admin UI
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.p2.internal.repositories.IRepositoryDefinitionManager;
import org.eclipse.gyrex.p2.internal.repositories.RepositoryDefinition;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

public class EditRepositoryDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField uriField = new StringDialogField();
	private final StringDialogField nodeFilterField = new StringDialogField();

	private final IRepositoryDefinitionManager repoManager;
	private final RepositoryDefinition repoDefinition;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the shell parent
	 * @param repoManager
	 *            the repository manager to be used
	 */
	public EditRepositoryDialog(final Shell parent, final RepositoryDefinition repoDefinition, final IRepositoryDefinitionManager repoManager) {
		super(parent);
		this.repoManager = repoManager;
		this.repoDefinition = repoDefinition;
		setTitle("New Software Repository");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		uriField.setLabelText("URL");
		nodeFilterField.setLabelText("Node Filter");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		nodeFilterField.setDialogFieldListener(validateListener);

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, uriField, nodeFilterField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(uriField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(nodeFilterField.getTextControl(null));

		if (null != repoDefinition) {
			idField.setEnabled(false);
			idField.setText(repoDefinition.getId());

			uriField.setText(repoDefinition.getLocation().toString());
			nodeFilterField.setText(StringUtils.trimToEmpty(repoDefinition.getNodeFilter()));
		}

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			final RepositoryDefinition repository = new RepositoryDefinition();
			repository.setId(idField.getText());
			repository.setLocation(new URI(uriField.getText()));
			repository.setNodeFilter(StringUtils.trimToNull(nodeFilterField.getText()));
			repoManager.saveRepository(repository);
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
		if (StringUtils.isNotBlank(id) && !IdHelper.isValidId(id)) {
			setError("The entered connector id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		final String uri = uriField.getText();
		if (StringUtils.isNotBlank(uri)) {
			try {
				new URI(uri);
			} catch (final URISyntaxException e) {
				setError("The entered URL. Please use valid URI syntax. " + e.getMessage());
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
			setInfo("Please enter a repository id.");
			return;
		}

		if (StringUtils.isBlank(uri)) {
			setInfo("Please enter a repository URL.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
