/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.adapter.ImageAdapter;
import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.TreeListDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.logback.config.internal.AppenderProviderRegistry;
import org.eclipse.gyrex.logback.config.internal.LogbackConfigActivator;
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.spi.AppenderProvider;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 * Page for selecting the appender type and entering appender name.
 */
public class AppenderWizardPage extends WizardPage {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final LabelProvider appenderTreeLabelProvider = new LabelProvider() {
		private static final long serialVersionUID = 1L;

		private LocalResourceManager resourceManager;

		private Image createImage(final ImageDescriptor descriptor) {
			if (descriptor == null)
				return null;

			if (resourceManager == null) {
				resourceManager = new LocalResourceManager(JFaceResources.getResources());
			}

			return resourceManager.createImage(descriptor);
		}

		@Override
		public void dispose() {
			super.dispose();
			if (resourceManager != null) {
				resourceManager.dispose();
				resourceManager = null;
			}
		}

		@Override
		public Image getImage(final Object element) {
			final ImageAdapter adapter = AdapterUtil.getAdapter(element, ImageAdapter.class);
			if (adapter != null)
				return createImage(adapter.getImageDescriptor(element));
			return super.getImage(element);
		};

		@Override
		public String getText(final Object element) {
			if (element instanceof AppenderType)
				return ((AppenderType) element).getName();
			return super.getText(element);
		};
	};

	private final ITreeListAdapter appenderTreeListAdapter = new ITreeListAdapter() {
		@Override
		public void customButtonPressed(final TreeListDialogField field, final int index) {
			// no-op
		}

		@Override
		public void doubleClicked(final TreeListDialogField field) {
			// no-op
		}

		@Override
		public Object[] getChildren(final TreeListDialogField field, final Object element) {
			return new Object[0];
		}

		@Override
		public Object getParent(final TreeListDialogField field, final Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(final TreeListDialogField field, final Object element) {
			return false;
		}

		@Override
		public void keyPressed(final TreeListDialogField field, final KeyEvent event) {
			// no-op
		}

		@Override
		public void selectionChanged(final TreeListDialogField field) {
			validate();
		}
	};

	private final TreeListDialogField appenderTypeField = new TreeListDialogField(appenderTreeListAdapter, null, appenderTreeLabelProvider, new PatternFilter());
	private final StringDialogField nameField = new StringDialogField();

	private final Appender appender;

	protected AppenderWizardPage(final Appender appender) {
		super("type");
		this.appender = appender;
		setTitle("Appender");
		setDescription("Configure the appender basics (such as name and type).");
		setPageComplete(false);
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		nameField.setLabelText("Name:");

		appenderTypeField.setLabelText("Type:");
		appenderTypeField.setViewerComparator(new ViewerComparator());

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		nameField.setDialogFieldListener(validateListener);
		appenderTypeField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { nameField, appenderTypeField }, false);
		LayoutUtil.setHorizontalGrabbing(nameField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(appenderTypeField.getTreeControl(null));

		final List<AppenderType> types = new ArrayList<>();
		final AppenderProviderRegistry registry = LogbackConfigActivator.getInstance().getAppenderProviderRegistry();
		for (final AppenderProvider provider : registry.getTracked().values()) {
			for (final String typeId : provider.getProvidedTypeIds()) {
				types.add(new AppenderType(typeId, registry.getName(typeId), provider));
			}
		}
		appenderTypeField.setElements(types);

		if (appender != null) {
			nameField.setText(appender.getName());
			for (final AppenderType type : types) {
				if (appender.getTypeId().equals(type.getId())) {
					appenderTypeField.selectElements(new StructuredSelection(type));
					break;
				}
			}
			appenderTypeField.setEnabled(false);
		}
	}

	@Override
	public String getName() {
		return StringUtils.trimToNull(nameField.getText());
	}

	private void setAppenderType(final AppenderType type) {
		final AddEditAppenderWizard wizard = (AddEditAppenderWizard) getWizard();
		if (type != null) {
			wizard.initializeCurrentAppenderConfigurationSession(type.getId(), type.getName(), type.getWizardAdapter());
		} else {
			wizard.clearCurrentAppenderConfigurationSession();
		}
	}

	void validate() {
		final String name = getName();
		if (name == null) {
			setMessage("Please enter an appender name.", INFORMATION);
			setPageComplete(false);
			return;
		} else if (!IdHelper.isValidId(name)) {
			setMessage("The entered appender name is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.", IMessageProvider.ERROR);
			setPageComplete(false);
			return;
		}

		final List<Object> selectedElements = appenderTypeField.getSelectedElements();
		if (selectedElements.size() != 1) {
			setMessage("Please select an appender to use.", INFORMATION);
			setPageComplete(false);
			return;
		}

		setAppenderType((AppenderType) selectedElements.get(0));

		setMessage(null);
		setPageComplete(true);
	}

}
