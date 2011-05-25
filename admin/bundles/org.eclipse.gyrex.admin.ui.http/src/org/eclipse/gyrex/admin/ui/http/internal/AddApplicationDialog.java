/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ComboDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.internal.ContextActivator;
import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.http.internal.HttpActivator;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("restriction")
public class AddApplicationDialog extends StatusDialog {

	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField pathField = new StringDialogField();
	private final ComboDialogField providerField = new ComboDialogField(SWT.DROP_DOWN);
	private final Map<String, String> providerItemToIdMap = new HashMap<String, String>();

	private final ApplicationManager applicationManager;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 */
	public AddApplicationDialog(final Shell parent, final ApplicationManager applicationManager) {
		super(parent);
		this.applicationManager = applicationManager;
		setTitle("New Application");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		pathField.setLabelText("Context");
		providerField.setLabelText("Provider");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		pathField.setDialogFieldListener(validateListener);
		providerField.setDialogFieldListener(validateListener);

		providerItemToIdMap.clear();
		final TreeSet<String> providerItems = new TreeSet<String>();
		final Collection<ApplicationProviderRegistration> providers = HttpActivator.getInstance().getProviderRegistry().getRegisteredProviders().values();
		for (final ApplicationProviderRegistration registration : providers) {
			final String label = HttpUiAdapterFactory.WORKBENCH_ADAPTER.getLabel(registration);
			providerItemToIdMap.put(label, registration.getProviderId());
			providerItems.add(label);
		}
		providerField.setItems(providerItems.toArray(new String[providerItems.size()]));

		pathField.setContentProposalProcessor(new IContentProposalProvider() {
			@Override
			public IContentProposal[] getProposals(final String contents, final int position) {
				final List<IContentProposal> resultList = new ArrayList<IContentProposal>();

				final String patternString = StringUtils.trimToNull(StringUtils.substring(contents, 0, position));

				final Collection<ContextDefinition> contexts = ContextActivator.getInstance().getContextRegistryImpl().getDefinedContexts();
				for (final ContextDefinition contextDefinition : contexts) {
					if ((null == patternString) || StringUtils.contains(contextDefinition.getPath().toString(), patternString)) {
						resultList.add(new ContentProposal(contextDefinition.getPath().toString(), contextDefinition.toString()));
					}
				}

				return resultList.toArray(new IContentProposal[resultList.size()]);
			}
		});

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, providerField, pathField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(providerField.getComboControl(null));
		LayoutUtil.setHorizontalGrabbing(pathField.getTextControl(null));

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
			final String id = idField.getText();
			final String providerId = providerItemToIdMap.get(providerField.getText());
			final String contextPath = pathField.getText();
			final IRuntimeContext context = HttpUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(new Path(contextPath).makeAbsolute().addTrailingSeparator());
			applicationManager.register(id, providerId, context, null);
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, HttpUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, HttpUiActivator.SYMBOLIC_NAME, message));
	}

	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, HttpUiActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isBlank(id)) {
			setInfo("Please enter an id.");
			return;
		}
		if (!IdHelper.isValidId(id)) {
			setError("The entered id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		final String providerLabel = providerField.getText();
		if (StringUtils.isBlank(providerLabel)) {
			setInfo("Please select a provider.");
			return;
		}

		final String path = pathField.getText();
		if (StringUtils.isBlank(path)) {
			setInfo("Please enter a context path.");
			return;
		}
		if (!Path.EMPTY.isValidPath(path)) {
			setError("The entered context path is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_' and '/' as separator.");
			return;
		}

		final IRuntimeContextRegistry registry = HttpUiActivator.getInstance().getService(IRuntimeContextRegistry.class);
		final IRuntimeContext context = registry.get(new Path(path));
		if (null == context) {
			setError("The context is not defined!");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
