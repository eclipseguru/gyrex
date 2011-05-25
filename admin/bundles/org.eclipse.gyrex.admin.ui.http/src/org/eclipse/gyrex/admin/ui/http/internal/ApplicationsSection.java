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
import java.util.TreeSet;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.http.application.manager.IApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.osgi.service.prefs.BackingStoreException;

/**
 *
 */
@SuppressWarnings("restriction")
public class ApplicationsSection extends ViewerWithButtonsSectionPart {

	private final ConfigurationPage page;

	private Button addButton;
	private Button removeButton;

	private Button activateButton;
	private Button deactivateButton;

	private ListViewer appsList;
	protected IViewerObservableValue selectedValue;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public ApplicationsSection(final Composite parent, final ConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		this.page = page;
		final Section section = getSection();
		section.setText("Available Applications");
		section.setDescription("Define the available web applications.");
		createContent(section);
	}

	void activateButtonPressed() {
		final ApplicationRegistration app = getSelectedValue();
		if (app == null) {
			return;
		}

		getApplicationManager().activate(app.getApplicationId());
		markStale();
	}

	void addButtonPressed() {
		final AddApplicationDialog dialog = new AddApplicationDialog(SwtUtil.getShell(addButton), getApplicationManager());
		if (dialog.open() == Window.OK) {
			markStale();
		}
	}

	@Override
	protected void createButtons(final Composite buttonsPanel) {
		addButton = createButton(buttonsPanel, "Add...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addButtonPressed();
			}
		});
		removeButton = createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		final Label separator = getToolkit().createLabel(buttonsPanel, "");
		FormLayoutDataFactory.applyDefaults(separator, 1);

		activateButton = createButton(buttonsPanel, "Activate", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				activateButtonPressed();
			}
		});
		deactivateButton = createButton(buttonsPanel, "Deactivate", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				deactivateButtonPressed();
			}
		});
	}

	@Override
	protected void createViewer(final Composite parent) {
		appsList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = appsList.getList();
		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		appsList.setContentProvider(new ArrayContentProvider());
		appsList.setLabelProvider(new WorkbenchLabelProvider());

		selectedValue = ViewersObservables.observeSingleSelection(appsList);
	}

	void deactivateButtonPressed() {
		final ApplicationRegistration app = getSelectedValue();
		if (app == null) {
			return;
		}

		getApplicationManager().deactivate(app.getApplicationId());
		markStale();
	}

	private ApplicationManager getApplicationManager() {
		return (ApplicationManager) HttpUiActivator.getInstance().getService(IApplicationManager.class);
	}

	private DataBindingContext getBindingContext() {
		return page.getBindingContext();
	}

	private ApplicationRegistration getSelectedValue() {
		return (ApplicationRegistration) (null != selectedValue ? selectedValue.getValue() : null);
	}

	public ISelectionProvider getSelectionProvider() {
		return appsList;
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final ISWTObservableValue listSelection = SWTObservables.observeSelection(appsList.getControl());
		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
		final UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER);
		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), listSelection, targetToModel, modelToTarget);
		getBindingContext().bindValue(SWTObservables.observeEnabled(activateButton), listSelection, targetToModel, modelToTarget);
		getBindingContext().bindValue(SWTObservables.observeEnabled(deactivateButton), listSelection, targetToModel, modelToTarget);
	}

	@Override
	public void refresh() {
		try {
			final Collection<String> registeredApplications = new TreeSet<String>(getApplicationManager().getRegisteredApplications());
			final java.util.List<ApplicationRegistration> apps = new ArrayList<ApplicationRegistration>();
			for (final String appId : registeredApplications) {
				apps.add(getApplicationManager().getApplicationRegistration(appId));
			}
			appsList.setInput(apps);
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
		super.refresh();
	}

	void removeButtonPressed() {
		final ApplicationRegistration app = getSelectedValue();
		if (app == null) {
			return;
		}

		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Context", "Do you really want to delete the application?")) {
			return;
		}

		getApplicationManager().unregister(app.getApplicationId());
		markStale();
	}
}
