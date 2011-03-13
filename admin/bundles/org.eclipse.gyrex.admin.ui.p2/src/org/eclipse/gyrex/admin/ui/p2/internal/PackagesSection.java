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
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.p2.internal.P2Activator;
import org.eclipse.gyrex.p2.internal.packages.IPackageManager;
import org.eclipse.gyrex.p2.internal.packages.PackageDefinition;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
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

/**
 *
 */
public class PackagesSection extends ViewerWithButtonsSectionPart {

	private Button addButton;
	private Button removeButton;
	private Button provisionButton;
	private Button revokeButton;
	private ListViewer packagesList;
	private final DataBindingContext bindingContext;
	private IObservableValue selectedPackageValue;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public PackagesSection(final Composite parent, final SoftwareLandingPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		bindingContext = page.getBindingContext();
		final Section section = getSection();
		section.setText("Software Packages");
		section.setDescription("Define the installable packages.");
		createContent(section);
	}

	void addButtonPressed() {
		final AddPackageDialog dialog = new AddPackageDialog(SwtUtil.getShell(addButton), getPackageManager());
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

		provisionButton = createButton(buttonsPanel, "Provision", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				provisionButtonPressed();
			}
		});
		revokeButton = createButton(buttonsPanel, "Revoke", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				revokeButtonPressed();
			}
		});
	}

	@Override
	protected void createViewer(final Composite parent) {
		packagesList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = packagesList.getList();
		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		packagesList.setContentProvider(new ArrayContentProvider());
		packagesList.setLabelProvider(new P2UiLabelProvider());

		selectedPackageValue = ViewersObservables.observeSingleSelection(packagesList);
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	IPackageManager getPackageManager() {
		return P2Activator.getInstance().getPackageManager();
//		return P2UiActivator.getInstance().getService(IPackageManager.class);
	}

	private PackageDefinition getSelectedPackage() {
		return (PackageDefinition) (null != selectedPackageValue ? selectedPackageValue.getValue() : null);
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(packagesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
		getBindingContext().bindValue(SWTObservables.observeEnabled(provisionButton), SWTObservables.observeSelection(packagesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
		getBindingContext().bindValue(SWTObservables.observeEnabled(revokeButton), SWTObservables.observeSelection(packagesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
	}

	void provisionButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null) {
			return;
		}

		getPackageManager().markedForInstall(pkg);
		markStale();
	}

	@Override
	public void refresh() {
		packagesList.setInput(getPackageManager().getPackages());
		super.refresh();
	}

	void removeButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null) {
			return;
		}

		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Package", "Do you really want to delete the package?")) {
			return;
		}

		getPackageManager().removePackage(pkg.getId());
		markStale();
	}

	void revokeButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null) {
			return;
		}

		getPackageManager().markedForUninstall(pkg);
		markStale();
	}

}
