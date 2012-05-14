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

//
//import java.util.Collection;
//
//import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
//import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
//import org.eclipse.gyrex.admin.ui.logback.internal.LogbackConfigContentProvider.DefaultLogger;
//import org.eclipse.gyrex.admin.ui.pages.AdminPage;
//import org.eclipse.gyrex.logback.config.internal.LogbackConfigActivator;
//import org.eclipse.gyrex.logback.config.internal.PreferenceBasedLogbackConfigStore;
//import org.eclipse.gyrex.logback.config.internal.model.Appender;
//import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
//import org.eclipse.gyrex.logback.config.internal.model.Logger;
//import org.eclipse.gyrex.preferences.CloudScope;
//
//import org.eclipse.core.runtime.preferences.IEclipsePreferences;
//import org.eclipse.jface.layout.GridDataFactory;
//import org.eclipse.jface.viewers.IOpenListener;
//import org.eclipse.jface.viewers.ISelectionChangedListener;
//import org.eclipse.jface.viewers.ISelectionProvider;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.OpenEvent;
//import org.eclipse.jface.viewers.SelectionChangedEvent;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.jface.window.Window;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Tree;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.Section;
//
//import org.osgi.service.prefs.BackingStoreException;
//import org.osgi.service.prefs.Preferences;
//
//import org.slf4j.LoggerFactory;
//
///**
// * Shows assignments for a selected repository.
// */
//public class LogbackSection extends ViewerWithButtonsSectionPart {
//
//	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LogbackSection.class);
//
//	private TreeViewer configTree;
//	private LogbackConfig currentInput;
//
//	private Button editLoggerButton;
//	private Button removeButton;
//
//	private Object selectedElement;
//
//	/**
//	 * Creates a new instance.
//	 *
//	 * @param parent
//	 * @param page
//	 */
//	public LogbackSection(final Composite parent, final AdminPage page) {
//		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
//		final Section section = getSection();
//		section.setText("Configuration");
//		section.setDescription("Browse and modify the logback configuration.");
//		createContent(section);
//	}
//
//	void addAppenderButtonPressed() {
//		final AddAppenderDialog dialog = new AddAppenderDialog(configTree.getTree().getShell());
//		if (dialog.open() == Window.OK) {
//			currentInput.addAppender(dialog.getAppender());
//			markDirty();
//			configTree.refresh();
//		}
//	}
//
//	void addLoggerButtonPressed() {
//		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(configTree.getTree().getShell(), currentInput.getAppenders().values());
//		if (dialog.open() == Window.OK) {
//			currentInput.addLogger(dialog.getLogger());
//			markDirty();
//			configTree.refresh();
//		}
//	}
//
//	@Override
//	public void commit(final boolean onSave) {
//		if (onSave) {
//			final IEclipsePreferences node = CloudScope.INSTANCE.getNode(LogbackConfigActivator.SYMBOLIC_NAME);
//			try {
//				final Preferences configNode = node.node("config");
//				new PreferenceBasedLogbackConfigStore().saveConfig(currentInput, configNode);
//
//				// also touch last modified
//				node.putLong("lastModified", System.currentTimeMillis());
//				node.flush();
//			} catch (final BackingStoreException e) {
//				LOG.error("Error saving config!", e);
//				return;
//			}
//
//			// no longer dirty
//			super.commit(onSave);
//			getManagedForm().dirtyStateChanged();
//		}
//	}
//
//	@Override
//	protected void createButtons(final Composite buttonsPanel) {
//		createButton(buttonsPanel, "Add Appender...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				addAppenderButtonPressed();
//			}
//		});
//		createButton(buttonsPanel, "Add Logger...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				addLoggerButtonPressed();
//			}
//		});
//		editLoggerButton = createButton(buttonsPanel, "Edit Logger...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				editLoggerButtonPressed();
//			}
//		});
//		removeButton = createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				removeButtonPressed();
//			}
//		});
//
//		final Label separator = getToolkit().createLabel(buttonsPanel, "");
//		FormLayoutDataFactory.applyDefaults(separator, 1);
//
//		createButton(buttonsPanel, "Edit Default Logger...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				editDefaultLoggerButtonPressed();
//			}
//		});
//
//		updateButtons();
//	}
//
//	@Override
//	protected void createViewer(final Composite parent) {
//		configTree = new TreeViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final Tree tree = configTree.getTree();
//		getToolkit().adapt(tree, true, true);
//		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		configTree.setContentProvider(new LogbackConfigContentProvider());
//		configTree.setLabelProvider(new LogbackLabelProvider());
//		configTree.setComparator(new LogbackViewerComperator());
//
//		configTree.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			@Override
//			public void selectionChanged(final SelectionChangedEvent event) {
//				updateSelectedElement(((IStructuredSelection) event.getSelection()).getFirstElement());
//			}
//		});
//		configTree.addOpenListener(new IOpenListener() {
//
//			@Override
//			public void open(final OpenEvent event) {
//				updateSelectedElement(((IStructuredSelection) event.getSelection()).getFirstElement());
//				editSelectedElement();
//			}
//		});
//	}
//
//	void editDefaultLoggerButtonPressed() {
//		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(configTree.getTree().getShell(), currentInput.getDefaultLevel(), currentInput.getDefaultAppenders(), currentInput.getAppenders().values());
//		if (dialog.open() == Window.OK) {
//			currentInput.setDefaultLevel(dialog.getLogger().getLevel());
//			currentInput.setDefaultAppenders(dialog.getLogger().getAppenderReferences());
//			markDirty();
//			configTree.refresh();
//		}
//	}
//
//	void editLoggerButtonPressed() {
//		if (!(selectedElement instanceof Logger)) {
//			return;
//		}
//		final Logger logger = (Logger) selectedElement;
//		final String originalName = logger.getName();
//		final LoggerSettingsDialog dialog = new LoggerSettingsDialog(configTree.getTree().getShell(), originalName, logger.getLevel(), logger.isInheritOtherAppenders(), logger.getAppenderReferences(), currentInput.getAppenders().values());
//		if (dialog.open() == Window.OK) {
//			currentInput.getLoggers().remove(originalName);
//			currentInput.addLogger(dialog.getLogger());
//			markDirty();
//			configTree.refresh();
//		}
//	}
//
//	void editSelectedElement() {
//		if (selectedElement == null) {
//			return;
//		}
//
//		if (selectedElement instanceof Logger) {
//			editLoggerButtonPressed();
//		} else if (selectedElement instanceof DefaultLogger) {
//			editDefaultLoggerButtonPressed();
//		} else if (selectedElement instanceof Appender) {
//			// TODO
//		}
//	}
//
//	public ISelectionProvider getSelectionProvider() {
//		return configTree;
//	}
//
//	@Override
//	public void refresh() {
//		if (null == currentInput) {
//			final IEclipsePreferences node = CloudScope.INSTANCE.getNode(LogbackConfigActivator.SYMBOLIC_NAME);
//			try {
//				if (node.nodeExists("config")) {
//					final Preferences configNode = node.node("config");
//					configNode.sync();
//					currentInput = new PreferenceBasedLogbackConfigStore().loadConfig(configNode);
//				} else {
//					currentInput = new LogbackConfig();
//				}
//			} catch (final BackingStoreException e) {
//				LOG.error("Error loading config!", e);
//				currentInput = new LogbackConfig();
//			}
//			configTree.setInput(currentInput);
//		} else {
//			configTree.refresh();
//		}
//		super.refresh();
//	}
//
//	void removeButtonPressed() {
//		if (selectedElement == null) {
//			return;
//		}
//
//		if (selectedElement instanceof LoggerAppenderRef) {
//			final LoggerAppenderRef appenderRef = (LoggerAppenderRef) selectedElement;
//			appenderRef.getLogger().getAppenderReferences().remove(appenderRef.getAppenderRef());
//		} else if (selectedElement instanceof Logger) {
//			currentInput.getLoggers().remove(((Logger) selectedElement).getName());
//		} else if (selectedElement instanceof Appender) {
//			final String appenderName = ((Appender) selectedElement).getName();
//			final Collection<Logger> loggers = currentInput.getLoggers().values();
//			for (final Logger logger : loggers) {
//				logger.getAppenderReferences().remove(appenderName);
//			}
//			currentInput.getAppenders().remove(appenderName);
//		}
//		// TODO
//		markDirty();
//		configTree.refresh();
//	}
//
//	private void updateButtons() {
//		removeButton.setEnabled((null != selectedElement) && !(selectedElement instanceof DefaultLogger));
//		editLoggerButton.setEnabled(selectedElement instanceof Logger);
//	}
//
//	void updateSelectedElement(final Object firstElement) {
//		selectedElement = firstElement;
//		updateButtons();
//	}
//}
