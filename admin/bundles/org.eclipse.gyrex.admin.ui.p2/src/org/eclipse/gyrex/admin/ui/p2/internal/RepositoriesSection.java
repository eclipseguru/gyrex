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
package org.eclipse.gyrex.admin.ui.p2.internal;

//
//import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
//import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
//import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
//import org.eclipse.gyrex.p2.internal.P2Activator;
//import org.eclipse.gyrex.p2.internal.repositories.IRepositoryDefinitionManager;
//import org.eclipse.gyrex.p2.internal.repositories.RepositoryDefinition;
//
//import org.eclipse.core.databinding.DataBindingContext;
//import org.eclipse.core.databinding.UpdateValueStrategy;
//import org.eclipse.core.databinding.observable.value.IObservableValue;
//import org.eclipse.jface.databinding.swt.SWTObservables;
//import org.eclipse.jface.databinding.viewers.ViewersObservables;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.layout.GridDataFactory;
//import org.eclipse.jface.viewers.ArrayContentProvider;
//import org.eclipse.jface.viewers.ListViewer;
//import org.eclipse.jface.window.Window;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.List;
//import org.eclipse.ui.forms.IManagedForm;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.Section;
//
///**
// *
// */
//public class RepositoriesSection extends ViewerWithButtonsSectionPart {
//
//	private Button addButton;
//	private Button removeButton;
//	private ListViewer reposList;
//	private final DataBindingContext bindingContext;
//	private IObservableValue selectedRepoValue;
//
//	/**
//	 * Creates a new instance.
//	 *
//	 * @param parent
//	 * @param page
//	 */
//	public RepositoriesSection(final Composite parent, final SoftwareLandingPage page) {
//		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
//		bindingContext = page.getBindingContext();
//		final Section section = getSection();
//		section.setText("Software Repository");
//		section.setDescription("Define the available repositories.");
//		createContent(section);
//	}
//
//	void addButtonPressed() {
//		final AddRepositoryDialog dialog = new AddRepositoryDialog(SwtUtil.getShell(addButton), getRepoManager());
//		if (dialog.open() == Window.OK) {
//			markStale();
//		}
//	}
//
//	@Override
//	protected void createButtons(final Composite buttonsPanel) {
//		addButton = createButton(buttonsPanel, "Add...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				addButtonPressed();
//			}
//		});
//		removeButton = createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				removeButtonPressed();
//			}
//		});
//	}
//
//	@Override
//	protected void createViewer(final Composite parent) {
//		reposList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final List list = reposList.getList();
//		getToolkit().adapt(list, true, true);
//		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		reposList.setContentProvider(new ArrayContentProvider());
//		reposList.setLabelProvider(new P2UiLabelProvider());
//
//		selectedRepoValue = ViewersObservables.observeSingleSelection(reposList);
//	}
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
//	IRepositoryDefinitionManager getRepoManager() {
//		return P2Activator.getInstance().getRepositoryManager();
//	}
//
//	private RepositoryDefinition getSelectedRepo() {
//		return (RepositoryDefinition) (null != selectedRepoValue ? selectedRepoValue.getValue() : null);
//	}
//
//	@Override
//	public void initialize(final IManagedForm form) {
//		super.initialize(form);
//
//		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
//		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
//		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(reposList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//	}
//
//	@Override
//	public void refresh() {
//		reposList.setInput(getRepoManager().getRepositories());
//		super.refresh();
//	}
//
//	void removeButtonPressed() {
//		final RepositoryDefinition repo = getSelectedRepo();
//		if (repo == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Repository", "Do you really want to delete the repository?")) {
//			return;
//		}
//
//		getRepoManager().removeRepository(repo.getId());
//		markStale();
//	}
//
//}
