/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Peter Grube        - rework to Admin UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.wizards.NonBlockingWizardDialog;
import org.eclipse.gyrex.logback.config.internal.LogbackConfigActivator;
import org.eclipse.gyrex.logback.config.internal.PreferenceBasedLogbackConfigStore;
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.FileAppender;
import org.eclipse.gyrex.logback.config.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.model.Logger;
import org.eclipse.gyrex.preferences.CloudScope;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.osgi.service.prefs.Preferences;

import org.apache.commons.lang.text.StrBuilder;

public class LogbackConfigurationPage extends AdminPageWithTree {

	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_LEVEL = 1;

	private Button addAppenderButton;
	private Button addLoggerButton;
	private Button editLoggerButton;
	private Button removeButton;
	private Button editDefaultLoggerButton;
	private Button saveConfigButton;

	private LogbackConfig currentInput;

	public LogbackConfigurationPage() {
		super(2);
		setTitle("Logback Configuration");
		setTitleToolTip("Configure and assign Logback appenders and loggers.");
	}

	@Override
	public void activate() {
		super.activate();
	}

	void addLoggerButtonPressed() {
		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(getShell(), currentInput.getAppenders().values());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					currentInput.addLogger(dialog.getLogger());
					getTreeViewer().refresh();
				}
			}
		});
	}

	@Override
	protected void createButtons(final Composite buttonsPanel) {
		addAppenderButton = createButton(buttonsPanel, "Add Appender...");
		addAppenderButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				openAppenderWizard(null);
			}
		});

		addLoggerButton = createButton(buttonsPanel, "Add Logger...");
		addLoggerButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				addLoggerButtonPressed();
			}
		});

		editLoggerButton = createButton(buttonsPanel, "Edit Logger...");
		editLoggerButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editLoggerButtonPressed();
			}
		});

		removeButton = createButton(buttonsPanel, "Remove...");
		removeButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		createButtonSeparator(buttonsPanel);

		editDefaultLoggerButton = createButton(buttonsPanel, "Edit Default Logger...");
		editDefaultLoggerButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editDefaultLoggerButtonPressed();
			}
		});

		createButtonSeparator(buttonsPanel);

		saveConfigButton = createButton(buttonsPanel, "Save Config ...");
		saveConfigButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				saveConfigTree(true);
			}
		});

	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new LogbackConfigContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(parent);
			infobox.setLayoutData(AdminUiUtil.createHorzFillData());
			infobox.addHeading("Logging in Gyrex");
			infobox.addParagraph("Gyrex makes use of <a href=\"http://logback.qos.ch/\">Logback</a> to capture logging events from the most popular logging APIs. Logback can be configured using a configuration file. This page provides an alternate way using Gyrex cloud preferences. The log configuration is stored in a central place.");
			return infobox;
		}
		return null;
	}

	void editDefaultLoggerButtonPressed() {
		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(getShell(), currentInput.getDefaultLevel(), currentInput.getDefaultAppenders(), currentInput.getAppenders().values());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					currentInput.setDefaultLevel(dialog.getLogger().getLevel());
					currentInput.setDefaultAppenders(dialog.getLogger().getAppenderReferences());
					getTreeViewer().refresh();
				}
			}
		});
	}

	void editLoggerButtonPressed() {
		final Object selectedElement = getFirstSelectedElement();
		if (!(selectedElement instanceof Logger))
			return;
		final Logger logger = (Logger) selectedElement;
		final String originalName = logger.getName();
		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(getShell(), originalName, logger.getLevel(), logger.isInheritOtherAppenders(), logger.getAppenderReferences(), currentInput.getAppenders().values());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					currentInput.getLoggers().remove(originalName);
					currentInput.addLogger(dialog.getLogger());
					getTreeViewer().refresh();
				}
			}
		});
	}

	private String getAppenderName(final Appender element) {
		if (element instanceof FileAppender) {
			final FileAppender fileAppender = (FileAppender) element;
			final StrBuilder text = new StrBuilder();
			text.append(fileAppender.getName());
			text.append(String.format(" (-> %s)", fileAppender.getFileName()));
			if (null != fileAppender.getThreshold()) {
				text.append(String.format(" [%s]", fileAppender.getThreshold()));
			}
			if (fileAppender.isSeparateLogOutputsPerMdcProperty()) {
				text.append(String.format(" [mdc:%s]", fileAppender.getSiftingMdcPropertyName()));
			}
			return text.toString();
		}
		return element.getName();
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case COLUMN_NAME:
				return "Name";
			case COLUMN_LEVEL:
				return "Level";

			default:
				return null;
		}
	}

	@Override
	protected int getElementCategory(final Object element, final int column) {
		if (column == COLUMN_NAME) {
			if (element instanceof DefaultLogger)
				return 30;
			else if (element instanceof AppendersGroup)
				return 10;
			else if (element instanceof LoggersGroup)
				return 20;
		}
		return 100;
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (column == COLUMN_NAME) {
			final LabelAdapter labelAdapter = AdapterUtil.getAdapter(element, LabelAdapter.class);
			if (labelAdapter != null)
				return labelAdapter.getLabel(element);
			else if (element instanceof Appender)
				return getAppenderName((Appender) element);
			else if (element instanceof Logger)
				return ((Logger) element).getName();
			else if (element instanceof AppenderReference)
				return ((AppenderReference) element).getAppenderRef();
			else if (element instanceof DefaultLogger)
				return "Default Logger";
			else if (element instanceof AppendersGroup)
				return "Appenders";
			else if (element instanceof LoggersGroup)
				return "Loggers";
		} else if (column == COLUMN_LEVEL) {
			if (element instanceof Logger)
				return String.valueOf(((Logger) element).getLevel());
			else if (element instanceof DefaultLogger)
				return String.valueOf(((DefaultLogger) element).getLevel());
		}
		return null;
	}

	private Object getFirstSelectedElement() {
		return ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
	}

	private Shell getShell() {
		return getTreeViewer().getTree().getShell();
	}

	@Override
	protected Object getViewerInput() {
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
			} catch (final Exception | LinkageError | AssertionError e) {
				Policy.getStatusHandler().show(new Status(IStatus.ERROR, LogbackUiActivator.SYMBOLIC_NAME, "Error loading Logback configuration.", e), "Error");
				currentInput = new LogbackConfig();
			}
		}
		return currentInput;
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return column == COLUMN_NAME;
	}

	void openAppenderWizard(final Appender appenderToEdit) {
		final NonBlockingWizardDialog dialog = new NonBlockingWizardDialog(SwtUtil.getShell(getTreeViewer().getTree()), new AddEditAppenderWizard(currentInput, appenderToEdit));
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					if (appenderToEdit == null) {

					}
					getTreeViewer().refresh(appenderToEdit);
				}
			}

		});
	}

	@Override
	protected void openSelectedElement() {
		final Object selectedElement = getFirstSelectedElement();
		if (selectedElement == null)
			return;

		if (selectedElement instanceof Logger) {
			editLoggerButtonPressed();
		} else if (selectedElement instanceof DefaultLogger) {
			editDefaultLoggerButtonPressed();
		} else if (selectedElement instanceof Appender) {
			openAppenderWizard((Appender) selectedElement);
		}
	}

	void removeButtonPressed() {
		final Object selectedElement = getFirstSelectedElement();
		if (selectedElement == null)
			return;

		if (selectedElement instanceof AppenderReference) {
			final AppenderReference appenderRef = (AppenderReference) selectedElement;
			final Object parent = appenderRef.getParent();
			if (parent instanceof Logger) {
				((Logger) parent).getAppenderReferences().remove(appenderRef.getAppenderRef());
			} else if (parent instanceof DefaultLogger) {
				((DefaultLogger) parent).getAppenderReferences().remove(appenderRef.getAppenderRef());
			}
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
		getTreeViewer().refresh();
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
			} catch (final Exception | LinkageError | AssertionError e) {
				Policy.getStatusHandler().show(new Status(IStatus.ERROR, LogbackUiActivator.SYMBOLIC_NAME, "Error saving Logback configuration.", e), "Error");
				return;
			}
		}
	}

	@Override
	protected void updateButtons() {
		final Object selectedElement = getFirstSelectedElement();
		removeButton.setEnabled((null != selectedElement) && !(selectedElement instanceof DefaultLogger));
		editLoggerButton.setEnabled(selectedElement instanceof Logger);
	}

}
