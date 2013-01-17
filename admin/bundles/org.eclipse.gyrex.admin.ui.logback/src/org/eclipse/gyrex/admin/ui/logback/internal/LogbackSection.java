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
 *     Peter Grube        - rework to Admin UI
 */
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.widgets.FilteredTree;
import org.eclipse.gyrex.admin.ui.logback.internal.LogbackConfigContentProvider.DefaultLogger;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.logback.config.internal.LogbackConfigActivator;
import org.eclipse.gyrex.logback.config.internal.PreferenceBasedLogbackConfigStore;
import org.eclipse.gyrex.logback.config.internal.model.Appender;
import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.internal.model.Logger;
import org.eclipse.gyrex.preferences.CloudScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import org.slf4j.LoggerFactory;

/**
 * Shows assignments for a selected repository.
 */
public class LogbackSection {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LogbackSection.class);

	private TreeViewer configTree;
	private LogbackConfig currentInput;

	private Button editLoggerButton;
	private Button editDefaultLoggerButton;
	private Button removeButton;
	private Button addAppenderButton;
	private Button addLoggerButton;
	private Button saveConfigButton;

	private Object selectedElement;

	private final Composite parent;

	public LogbackSection(final Composite parent, final FilteredAdminPage page) {
		this.parent = parent;
		createContent(this.parent);
		loadConfigTree();
	}

	void addAppenderButtonPressed() {
		final EditAppenderDialog dialog = new EditAppenderDialog(configTree.getTree().getShell());
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					loadConfigTree();
					currentInput.addAppender(dialog.getAppender());
					configTree.refresh();
				}
			}

		});
	}

	void addLoggerButtonPressed() {
		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(configTree.getTree().getShell(), currentInput.getAppenders().values());
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					currentInput.addLogger(dialog.getLogger());
					configTree.refresh();
				}
			}
		});
	}

	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button b = new Button(buttons, SWT.NONE);
		b.setText(buttonLabel);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return b;
	}

	protected void createButtons(final Composite buttonsPanel) {
		addAppenderButton = createButton(buttonsPanel, "Add Appender...");
		addAppenderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addAppenderButtonPressed();
			}
		});

		addLoggerButton = createButton(buttonsPanel, "Add Logger...");
		addLoggerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addLoggerButtonPressed();
			}
		});

		editLoggerButton = createButton(buttonsPanel, "Edit Logger...");
		editLoggerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				editLoggerButtonPressed();
			}
		});

		removeButton = createButton(buttonsPanel, "Remove...");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		createButtonSeparator(buttonsPanel);

		editDefaultLoggerButton = createButton(buttonsPanel, "Edit Default Logger...");
		editDefaultLoggerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				editDefaultLoggerButtonPressed();
			}
		});

		createButtonSeparator(buttonsPanel);

		saveConfigButton = createButton(buttonsPanel, "Save Config ...");
		saveConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				saveConfigTree(true);
			}
		});

		updateButtons();
	}

	private Label createButtonSeparator(final Composite parent) {
		final Label separator = new Label(parent, SWT.NONE);
		separator.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		return separator;
	}

	private FilteredTree createContent(final Composite parent) {

		final FilteredTree filteredTree = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, new LogbackPatternFilter(), true);

		configTree = filteredTree.getViewer();

		final Tree tree = configTree.getTree();
		final TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 40));
		layout.addColumnData(new ColumnWeightData(30, 20));
		layout.addColumnData(new ColumnWeightData(20, 20));
		tree.setLayout(layout);

		configTree.setContentProvider(new LogbackConfigContentProvider());
		configTree.setLabelProvider(new LogbackLabelProvider());
		configTree.setComparator(new LogbackViewerComperator());

		configTree.addSelectionChangedListener(new ISelectionChangedListener() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				updateSelectedElement(((IStructuredSelection) event.getSelection()).getFirstElement());
			}
		});
		configTree.addOpenListener(new IOpenListener() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
			 */
			@Override
			public void open(final OpenEvent event) {
				updateSelectedElement(((IStructuredSelection) event.getSelection()).getFirstElement());
				editSelectedElement();
			}
		});

		final Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());
		createButtons(buttons);

		return filteredTree;
	}

	void editAppenderButtonPressed() {
		final EditAppenderDialog dialog = new EditAppenderDialog(configTree.getTree().getShell(), getSelectedAppenderElement());
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					loadConfigTree();
					currentInput.addAppender(dialog.getAppender());
					configTree.refresh();
				}
			}
		});
	}

	void editDefaultLoggerButtonPressed() {
		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(configTree.getTree().getShell(), currentInput.getDefaultLevel(), currentInput.getDefaultAppenders(), currentInput.getAppenders().values());
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					currentInput.setDefaultLevel(dialog.getLogger().getLevel());
					currentInput.setDefaultAppenders(dialog.getLogger().getAppenderReferences());
					configTree.refresh();
				}
			}
		});
	}

	void editLoggerButtonPressed() {
		if (!(selectedElement instanceof Logger))
			return;
		final Logger logger = (Logger) selectedElement;
		final String originalName = logger.getName();
		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(configTree.getTree().getShell(), originalName, logger.getLevel(), logger.isInheritOtherAppenders(), logger.getAppenderReferences(), currentInput.getAppenders().values());
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					currentInput.getLoggers().remove(originalName);
					currentInput.addLogger(dialog.getLogger());
					configTree.refresh();
				}
			}
		});
	}

	void editSelectedElement() {
		if (selectedElement == null)
			return;

		if (selectedElement instanceof Logger) {
			editLoggerButtonPressed();
		} else if (selectedElement instanceof DefaultLogger) {
			editDefaultLoggerButtonPressed();
		} else if (selectedElement instanceof Appender) {
			editAppenderButtonPressed();
		}
	}

	private Appender getSelectedAppenderElement() {
		return (Appender) selectedElement;
	}

	public ISelectionProvider getSelectionProvider() {
		return configTree;
	}

	public void loadConfigTree() {
		if (null == currentInput) {
			final IEclipsePreferences node = CloudScope.INSTANCE.getNode(LogbackConfigActivator.SYMBOLIC_NAME);
			try {
				if (node.nodeExists("config")) {
					final Preferences configNode = node.node("config");
					configNode.sync();
					currentInput = new PreferenceBasedLogbackConfigStore().loadConfig(configNode);
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
	}

	void removeButtonPressed() {
		if (selectedElement == null)
			return;

		if (selectedElement instanceof LoggerAppenderRef) {
			final LoggerAppenderRef appenderRef = (LoggerAppenderRef) selectedElement;
			appenderRef.getLogger().getAppenderReferences().remove(appenderRef.getAppenderRef());
		} else if (selectedElement instanceof Logger) {
			currentInput.getLoggers().remove(((Logger) selectedElement).getName());
		} else if (selectedElement instanceof Appender) {
			final String appenderName = ((Appender) selectedElement).getName();
			final Collection<Logger> loggers = currentInput.getLoggers().values();
			for (final Logger logger : loggers) {
				logger.getAppenderReferences().remove(appenderName);
			}
			currentInput.getAppenders().remove(appenderName);
		}
		configTree.refresh();
	}

	public void saveConfigTree(final boolean onSave) {
		if (onSave) {
			final IEclipsePreferences node = CloudScope.INSTANCE.getNode(LogbackConfigActivator.SYMBOLIC_NAME);
			try {
				final Preferences configNode = node.node("config");
				new PreferenceBasedLogbackConfigStore().saveConfig(currentInput, configNode);

				// also touch last modified
				node.putLong("lastModified", System.currentTimeMillis());
				node.flush();
			} catch (final BackingStoreException e) {
				LOG.error("Error saving config!", e);
				return;
			}
		}
	}

	private void updateButtons() {
		removeButton.setEnabled((null != selectedElement) && !(selectedElement instanceof DefaultLogger));
		editLoggerButton.setEnabled(selectedElement instanceof Logger);
	}

	void updateSelectedElement(final Object firstElement) {
		selectedElement = firstElement;
		updateButtons();
	}
}
