/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Peter Grube - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.p2.internal.repositories.IRepositoryDefinitionManager;
import org.eclipse.gyrex.p2.internal.repositories.RepositoryDefinition;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class RepositoriesListDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private ListViewer reposViewer;
	private Button addButton;
	private Button removeButton;
	private Button editButton;

	private ISelectionChangedListener updateButtonsListener;

	private final IRepositoryDefinitionManager repoManager;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the shell parent
	 * @param repoManager
	 *            the repository manager to be used
	 */
	public RepositoriesListDialog(final Shell parent, final IRepositoryDefinitionManager repoManager) {
		super(parent);
		this.repoManager = repoManager;

		setTitle("Edit Repositorylist");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	/**
	 * Used the same way as {@link AdminPage#activate()} from {@link AdminPage}
	 * or {@link FilteredAdminPage} but called during
	 * {@link RepositoriesListDialog#createDialogArea(Composite)}
	 */
	private void activate() {
		if (reposViewer != null) {
			reposViewer.setInput(repoManager);
			updateButtonsListener = new ISelectionChangedListener() {
				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			reposViewer.addSelectionChangedListener(updateButtonsListener);
			reposViewer.getControl().getDisplay();
		}
	}

	private void addNewRepository() {
		final EditRepositoryDialog dialog = new EditRepositoryDialog(getParentShell(), null, repoManager);
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
				addNewRepository();
			}
		});
		removeButton = createButton(buttonsPanel, "Remove...");
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeRepository();
			}
		});

		editButton = createButton(buttonsPanel, "Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editSelectedRepository();
			}
		});
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		reposViewer = new ListViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = reposViewer.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		reposViewer.setContentProvider(new RepositoryContentProvider());
		reposViewer.setLabelProvider(new P2UiLabelProvider());
		reposViewer.addOpenListener(new IOpenListener() {

			@Override
			public void open(final OpenEvent event) {
				editSelectedRepository();
			}
		});

		final Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());
		createButtons(buttons);

		updateButtons();
		activate();

		return composite;
	}

	/**
	 * Used the same way as {@link AdminPage#deactivate()} from
	 * {@link AdminPage} or {@link FilteredAdminPage} but called in
	 * {@link SoftwareLandingPage#editRepositoriesList()}
	 */
	public void deactivate() {
		if (reposViewer != null) {
			if (updateButtonsListener != null) {
				reposViewer.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (!reposViewer.getList().isDisposed()) {
				reposViewer.setInput(null);
			}
		}
	}

	private void editSelectedRepository() {
		final EditRepositoryDialog dialog = new EditRepositoryDialog(getParentShell(), getSelectedRepo(), repoManager);
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

	private RepositoryDefinition getSelectedRepo() {
		final StructuredSelection selection = (StructuredSelection) reposViewer.getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof RepositoryDefinition) {
			return (RepositoryDefinition) selection.getFirstElement();
		}

		return null;
	}

	public void refresh() {
		reposViewer.refresh();
	}

	void removeRepository() {
		final RepositoryDefinition repo = getSelectedRepo();
		if (repo == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(removeButton), "Remove Repository", "Do you really want to delete the repository?", new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					repoManager.removeRepository(repo.getId());

					refresh();
				}
			}
		});
	}

	protected void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) reposViewer.getSelection()).size();
		if (selectedElementsCount == 0) {
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
		}
	}
}
