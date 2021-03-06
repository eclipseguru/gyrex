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
 */
package org.eclipse.gyrex.admin.ui.context.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;
import org.eclipse.gyrex.context.internal.ContextActivator;
import org.eclipse.gyrex.context.internal.configuration.ContextConfiguration;
import org.eclipse.gyrex.context.internal.preferences.GyrexContextPreferencesImpl;
import org.eclipse.gyrex.rap.helper.SwtUtil;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The Class ContextsSection.
 */
public class ContextsSection {

	/**
	 * 
	 */
	private class ContextDefinitionComparator implements Comparator<ContextDefinition> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final ContextDefinition arg0, final ContextDefinition arg1) {
			return arg0.getPath().toString().compareTo(arg1.getPath().toString());
		}

	}

	/** The page composite. */
	private Composite pageComposite;

	/** The add button. */
	private Button addButton;

	/** The remove button. */
	private Button removeButton;

	/** The editProperties button. */
	private Button editPropertiesButton;

	/** The contexts list. */
	private ListViewer contextsList;

	/** The selected value. */
	protected IViewerObservableValue selectedValue;

	/**
	 * Adds the button pressed.
	 */
	void addButtonPressed() {
		final AddContextDialog dialog = new AddContextDialog(SwtUtil.getShell(addButton), getContextRegistry());
		dialog.openNonBlocking(new DialogCallback() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});
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

	/**
	 * Creates the context section control.
	 * 
	 * @param parent
	 *            the parent
	 */
	public void createContextSectionControl(final Composite parent) {
		pageComposite = parent;
		final Infobox infobox = new Infobox(pageComposite);
		infobox.setLayoutData(AdminUiUtil.createHorzFillData());
		infobox.addHeading("Contexts section in Gyrex.");
		infobox.addParagraph("Gyrex provides a contextual runtime, which means it holds all its configuration information in the Context Tree, where configuration settings can be inherited and stored at the different context tree nodes. The context tree can reflect tenants or other configuration assets you build in your Gyrex application.<br/><br/>Every context is defined by name and path. ");

		final Composite description = new Composite(pageComposite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		contextsList = new ListViewer(description, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		final List list = contextsList.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		contextsList.setContentProvider(new ArrayContentProvider());
		contextsList.setLabelProvider(new ContextUiLabelProvider());

		final java.util.List<ContextDefinition> definedContexts = new ArrayList<ContextDefinition>();

		for (final ContextDefinition contextDefinition : getContextRegistry().getDefinedContexts()) {
			definedContexts.add(contextDefinition);
		}

		Collections.sort(definedContexts, new ContextDefinitionComparator());

		contextsList.setInput(definedContexts);

		selectedValue = ViewersObservables.observeSingleSelection(contextsList);

		final Composite buttons = new Composite(description, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());

		addButton = createButton(buttons, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		removeButton = createButton(buttons, "Remove");
		removeButton.setEnabled(true);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

		editPropertiesButton = createButton(buttons, "Edit Preferences");
		editPropertiesButton.setEnabled(true);
		editPropertiesButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				editPropertiesButtonPressed();
			}
		});

	}

	/**
	 * Click Action for edit properties button
	 */
	void editPropertiesButtonPressed() {

		final ContextDefinition contextDefinition = getSelectedContext();
		if (contextDefinition == null)
			return;

		final String path = GyrexContextPreferencesImpl.getPreferencesPathToSettings(contextDefinition.getPath(), null);
		final IEclipsePreferences prefRootNode = ContextConfiguration.getRootNodeForContextPreferences();
		try {
			if (!prefRootNode.nodeExists(path)) {
				//return;
			}
			final Preferences prefNode = prefRootNode.node(path);

			final EditContextPrefsDialog dialog = new EditContextPrefsDialog(SwtUtil.getShell(editPropertiesButton), prefNode);
			dialog.openNonBlocking(new DialogCallback() {

				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public void dialogClosed(final int returnCode) {
					if (returnCode == Window.OK) {
						refresh();
					}
				}
			});

		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the context registry.
	 * 
	 * @return the context registry
	 */
	private IRuntimeContextDefinitionManager getContextRegistry() {
		return ContextActivator.getInstance().getContextRegistryImpl();
	}

	/**
	 * Gets the selected context.
	 * 
	 * @return the selected context
	 */
	private ContextDefinition getSelectedContext() {
		return (ContextDefinition) (null != selectedValue ? selectedValue.getValue() : null);
	}

	/**
	 * Gets the selection provider.
	 * 
	 * @return the selection provider
	 */
	public ISelectionProvider getSelectionProvider() {
		return contextsList;
	}

	/**
	 * Refresh.
	 */
	public void refresh() {
		contextsList.setInput(getContextRegistry().getDefinedContexts());
	}

	/**
	 * Removes the button pressed.
	 */
	void removeButtonPressed() {
		final ContextDefinition contextDefinition = getSelectedContext();
		if (contextDefinition == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(pageComposite), "Remove Context", String.format("Do you really want to delete the context %s?", contextDefinition.getPath() + contextDefinition.getName()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					getContextRegistry().removeDefinition(contextDefinition.getPath());
				} catch (final Exception e) {
					NonBlockingMessageDialogs.openError(SwtUtil.getShell(pageComposite), "Error", e.getMessage(), new DialogCallback() {
						/** serialVersionUID */
						private static final long serialVersionUID = 1L;

						@Override
						public void dialogClosed(final int returnCode) {
							refresh();
						}
					});
				}
				refresh();
			}
		});
	}
}
