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
 *     Peter Grube - rework new Admin UI
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.p2.internal.P2Activator;
import org.eclipse.gyrex.p2.internal.packages.IPackageManager;
import org.eclipse.gyrex.p2.internal.packages.PackageDefinition;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class PackagesSection {

	private final Composite parent;

	private Button addButton;
	private Button removeButton;
	private Button provisionButton;
	private Button revokeButton;
	private ListViewer packagesList;
	private IObservableValue selectedPackageValue;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	@Deprecated
	public PackagesSection(final Composite parent, final AdminPage page) {
		this.parent = parent;
//		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
//		bindingContext = page.getBindingContext();

//		final Section section = getSection();
//		section.setText("Software Packages");
//		section.setDescription("Define the installable packages.");
		createContent(this.parent);
	}

//
	void addButtonPressed() {
		final EditPackageDialog dialog = new EditPackageDialog(SwtUtil.getShell(addButton), getPackageManager(), null);
		dialog.openNonBlocking(new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
//				markStale();
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
		addButton = createButton(buttonsPanel, "Add...");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				addButtonPressed();
			}
		});

		removeButton = createButton(buttonsPanel, "Remove...");
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		createButtonsSeparator(buttonsPanel);

		provisionButton = createButton(buttonsPanel, "Provision");
		provisionButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				provisionButtonPressed();
			}
		});

		revokeButton = createButton(buttonsPanel, "Revoke");
		revokeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				revokeButtonPressed();
			}
		});
	}

	private Label createButtonsSeparator(final Composite parent) {
		final Label separator = new Label(parent, SWT.NONE);
		separator.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		return separator;
	}

	void createContent(final Composite parent) {
		packagesList = new ListViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = packagesList.getList();
//		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		packagesList.setContentProvider(new ArrayContentProvider());
		packagesList.setLabelProvider(new P2UiLabelProvider());

		selectedPackageValue = ViewersObservables.observeSingleSelection(packagesList);

		final Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());
		createButtons(buttons);

		updateButtons();
	}

//	@Override
	protected void createViewer(final Composite parent) {
//		packagesList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final List list = packagesList.getList();
//		getToolkit().adapt(list, true, true);
//		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		packagesList.setContentProvider(new ArrayContentProvider());
//		packagesList.setLabelProvider(new P2UiLabelProvider());
//
		selectedPackageValue = ViewersObservables.observeSingleSelection(packagesList);
	}

//
//	/**
//	 * Returns the bindingContext.
//	 *
//	 * @return the bindingContext
//	 */
//	public DataBindingContext getBindingContext() {
//		return bindingContext;
//	}

//
	IPackageManager getPackageManager() {
		return P2Activator.getInstance().getPackageManager();
//		return P2UiActivator.getInstance().getService(IPackageManager.class);
	}

	private PackageDefinition getSelectedPackage() {
		return (PackageDefinition) (null != selectedPackageValue ? selectedPackageValue.getValue() : null);
	}

//
//	@Override
//	public void initialize(final IManagedForm form) {
////		super.initialize(form);
//
//		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
////		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
//		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(packagesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//		getBindingContext().bindValue(SWTObservables.observeEnabled(provisionButton), SWTObservables.observeSelection(packagesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//		getBindingContext().bindValue(SWTObservables.observeEnabled(revokeButton), SWTObservables.observeSelection(packagesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//	}

	void provisionButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null) {
			return;
		}

		getPackageManager().markedForInstall(pkg);

		updateButtons();
//		markStale();
	}

	public void refresh() {
		packagesList.setInput(getPackageManager().getPackages());
		updateButtons();
//		super.refresh();
	}

	void removeButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(removeButton), "Remove Package", "Do you really want to delete the package?", new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				// TODO Auto-generated method stub
				if (returnCode == Window.OK) {
					getPackageManager().removePackage(pkg.getId());
					refresh();
				}
			}
		});
	}

	void revokeButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null) {
			return;
		}

		getPackageManager().markedForUninstall(pkg);

		updateButtons();
//		markStale();
	}

	void updateButtons() {

		if (getSelectedPackage() == null) {
			removeButton.setEnabled(false);
			provisionButton.setEnabled(false);
			revokeButton.setEnabled(false);
		} else {
			if (getPackageManager().isMarkedForInstall(getSelectedPackage())) {
				provisionButton.setEnabled(false);
				revokeButton.setEnabled(true);
			}

			if (getPackageManager().isMarkedForUninstall(getSelectedPackage())) {
				revokeButton.setEnabled(false);
				provisionButton.setEnabled(true);
			}
		}
	}

}
