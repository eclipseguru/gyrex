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
 *     Peter Grube - rework new Admin UI
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.p2.internal.P2Activator;
import org.eclipse.gyrex.p2.internal.packages.IPackageManager;
import org.eclipse.gyrex.p2.internal.packages.PackageDefinition;
import org.eclipse.gyrex.p2.internal.repositories.IRepositoryDefinitionManager;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;

/**
 * Configuration page for Jobs.
 */
public class SoftwareLandingPage extends AdminPage {

	private Composite pageComposite;

	private Link manageRepos;

	private ISelectionChangedListener updateButtonsListener;
	private ListViewer packagesViewer;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button provisionButton;
	private Button revokeButton;

	/**
	 * Creates a new instance.
	 */
	public SoftwareLandingPage() {
		setTitle("Software Provisioning");
		setTitleToolTip("Install, update and remove software.");
	}

	@Override
	public void activate() {
		super.activate();

		if (packagesViewer != null) {
			packagesViewer.setInput(getPackageManager());
			updateButtonsListener = new ISelectionChangedListener() {
				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			packagesViewer.addSelectionChangedListener(updateButtonsListener);
			packagesViewer.getControl().getDisplay();
		}
	}

	void addButtonPressed() {
		final EditPackageDialog dialog = new EditPackageDialog(SwtUtil.getShell(addButton), getPackageManager(), null);
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

		editButton = createButton(buttonsPanel, "Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editSelectedPackage();
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

	@Override
	public Control createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(pageComposite);
			infobox.setLayoutData(AdminUiUtil.createHorzFillData());
			infobox.addHeading("Manage software packages");
			infobox.addParagraph("This page offers the possibillty to manage software packages. Create packages of features provided from p2 repositories.");
		}

		manageRepos = new Link(pageComposite, SWT.WRAP | SWT.READ_ONLY);
		manageRepos.setText("Add more software by updating  <a>repository list</a>");
		manageRepos.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		manageRepos.moveAbove(null);
		manageRepos.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editRepositoriesList();
			}
		});

		final Composite description = new Composite(pageComposite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		packagesViewer = new ListViewer(description, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = packagesViewer.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		packagesViewer.setContentProvider(new PackageContentProvider());
		packagesViewer.setLabelProvider(new P2UiLabelProvider());
		packagesViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(final OpenEvent event) {
				editSelectedPackage();
			}
		});

		final Composite buttons = new Composite(description, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());
		createButtons(buttons);

		updateButtons();

		return pageComposite;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (packagesViewer != null) {
			if (updateButtonsListener != null) {
				packagesViewer.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (!packagesViewer.getList().isDisposed()) {
				packagesViewer.setInput(null);
			}
		}
	}

	void editRepositoriesList() {
		final RepositoriesListDialog dialog = new RepositoriesListDialog(SwtUtil.getShell(manageRepos), getRepoManager());
		dialog.openNonBlocking(new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
				}
				dialog.deactivate();
			}
		});
	}

	void editSelectedPackage() {
		final EditPackageDialog dialog = new EditPackageDialog(SwtUtil.getShell(addButton), getPackageManager(), getSelectedPackage());
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

	IPackageManager getPackageManager() {
		return P2Activator.getInstance().getPackageManager();
//		return P2UiActivator.getInstance().getService(IPackageManager.class);
	}

	/**
	 * @return
	 */
	private IRepositoryDefinitionManager getRepoManager() {
		return P2Activator.getInstance().getRepositoryManager();
	}

	private PackageDefinition getSelectedPackage() {
		final StructuredSelection selection = (StructuredSelection) packagesViewer.getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof PackageDefinition))
			return (PackageDefinition) selection.getFirstElement();

		return null;
	}

	void provisionButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null)
			return;

		getPackageManager().markedForInstall(pkg);
		refresh();
	}

	void refresh() {
		packagesViewer.refresh();
	}

	void removeButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(removeButton), "Remove Package", "Do you really want to delete the package?", new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					getPackageManager().removePackage(pkg.getId());
					refresh();
				}
			}
		});
	}

	void revokeButtonPressed() {
		final PackageDefinition pkg = getSelectedPackage();
		if (pkg == null)
			return;

		getPackageManager().markedForUninstall(pkg);
		refresh();
	}

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) packagesViewer.getSelection()).size();
		if (selectedElementsCount == 0) {
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			provisionButton.setEnabled(false);
			revokeButton.setEnabled(false);
			return;
		}

		boolean markedforInstall = false;
		boolean markedforUninstall = false;

		markedforInstall |= getPackageManager().isMarkedForInstall(getSelectedPackage());
		markedforUninstall |= !getPackageManager().isMarkedForInstall(getSelectedPackage());

		if (markedforInstall && markedforUninstall)
			return;

		provisionButton.setEnabled(markedforUninstall);
		revokeButton.setEnabled(markedforInstall);
		removeButton.setEnabled(markedforUninstall);
		editButton.setEnabled(selectedElementsCount == 1);
	}
}
