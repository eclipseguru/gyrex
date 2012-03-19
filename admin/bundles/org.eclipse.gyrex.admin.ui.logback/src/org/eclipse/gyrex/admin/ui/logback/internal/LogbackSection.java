/**
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.configuration.IConfigurationPageContainer;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.logback.config.internal.LogbackConfigActivator;
import org.eclipse.gyrex.logback.config.internal.PreferenceBasedLogbackConfigStore;
import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.preferences.CloudScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.osgi.service.prefs.BackingStoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows assignments for a selected repository.
 */
public class LogbackSection extends ViewerWithButtonsSectionPart {

	private static final Logger LOG = LoggerFactory.getLogger(LogbackSection.class);

	private TreeViewer configTree;
	private final IConfigurationPageContainer configurationPageContainer;
	private LogbackConfig currentInput;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public LogbackSection(final Composite parent, final ConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		configurationPageContainer = page.getContainer();
		final Section section = getSection();
		section.setText("Configuration");
		section.setDescription("Browse and modify the logback configuration.");
		createContent(section);
	}

	void addAppenderButtonPressed() {
		final AddAppenderDialog addAppenderDialog = new AddAppenderDialog(configTree.getTree().getShell());
		if (addAppenderDialog.open() == Window.OK) {
			currentInput.getAppenders().add(addAppenderDialog.getAppender());
			markDirty();
			markStale();
		}
	}

	void addLoggerButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit(final boolean onSave) {
		if (onSave) {

			// no longer dirty
			super.commit(onSave);
			getManagedForm().dirtyStateChanged();
		}
	}

	@Override
	protected void createButtons(final Composite buttonsPanel) {
		createButton(buttonsPanel, "Add Appender...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addAppenderButtonPressed();
			}
		});
		createButton(buttonsPanel, "Add Logger...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addLoggerButtonPressed();
			}
		});
		createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		final Label separator = getToolkit().createLabel(buttonsPanel, "");
		FormLayoutDataFactory.applyDefaults(separator, 1);
	}

	@Override
	protected void createViewer(final Composite parent) {
		configTree = new TreeViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final Tree tree = configTree.getTree();
		getToolkit().adapt(tree, true, true);
		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		configTree.setContentProvider(new BaseWorkbenchContentProvider());
		configTree.setLabelProvider(new WorkbenchLabelProvider());
	}

	public ISelectionProvider getSelectionProvider() {
		return configTree;
	}

	@Override
	public void refresh() {
		if (null == currentInput) {
			final IEclipsePreferences node = CloudScope.INSTANCE.getNode(LogbackConfigActivator.SYMBOLIC_NAME);
			try {
				if (node.nodeExists("config")) {
					new PreferenceBasedLogbackConfigStore().loadConfig(node.node("config"));
				} else {
					currentInput = new LogbackConfig();
				}
			} catch (final BackingStoreException e) {
				LOG.error("Error loading config!", e);
				currentInput = new LogbackConfig();
			}
			configTree.setInput(currentInput);
		} else {
			configTree.refresh();
		}
		super.refresh();
	}

	void removeButtonPressed() {
		// TODO
		markDirty();
	}
}
