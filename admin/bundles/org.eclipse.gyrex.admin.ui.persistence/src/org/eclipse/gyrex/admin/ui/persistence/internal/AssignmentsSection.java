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
package org.eclipse.gyrex.admin.ui.persistence.internal;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.configuration.IConfigurationPageContainer;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.persistence.internal.storage.RepositoryRegistry;
import org.eclipse.gyrex.persistence.storage.lookup.DefaultRepositoryLookupStrategy;
import org.eclipse.gyrex.persistence.storage.registry.IRepositoryDefinition;
import org.eclipse.gyrex.persistence.storage.registry.IRepositoryRegistry;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Shows assignments for a selected repository.
 */
public class AssignmentsSection extends ViewerWithButtonsSectionPart implements ISelectionListener {

	private TreeViewer assignmentsTree;
	private final DataBindingContext bindingContext;
	private final IConfigurationPageContainer configurationPageContainer;
	private IRepositoryDefinition currentInput;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public AssignmentsSection(final Composite parent, final ConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		bindingContext = page.getBindingContext();
		configurationPageContainer = page.getContainer();
		final Section section = getSection();
		section.setText("Assignments");
		section.setDescription("Browse and modify the active assignments of a repository.");
		createContent(section);
	}

	void addButtonPressed() {
		// TODO
	}

	@Override
	protected void createButtons(final Composite buttonsPanel) {
		createButton(buttonsPanel, "Add...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addButtonPressed();
			}
		});
		createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		final Label separator = getToolkit().createLabel(buttonsPanel, "");
		FormLayoutDataFactory.applyDefaults(separator, 1);
	}

	@Override
	protected void createViewer(final Composite parent) {
		assignmentsTree = new TreeViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final Tree tree = assignmentsTree.getTree();
		getToolkit().adapt(tree, true, true);
		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		assignmentsTree.setContentProvider(new BaseWorkbenchContentProvider());
		assignmentsTree.setLabelProvider(new WorkbenchLabelProvider());
	}

	@Override
	public void dispose() {
		configurationPageContainer.getSite().getPage().removeSelectionListener(this);

		super.dispose();
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	RepositoryRegistry getRepositoryRegistry() {
		return (RepositoryRegistry) PersistenceUiActivator.getInstance().getService(IRepositoryRegistry.class);
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		configurationPageContainer.getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void refresh() {
		if (null != currentInput) {
			assignmentsTree.setInput(DefaultRepositoryLookupStrategy.getDefault().getContentTypeAssignments(currentInput.getRepositoryId()));
		} else {
			assignmentsTree.setInput(null);
		}
		super.refresh();
	}

	void removeButtonPressed() {
//		final Object item = getSelectedItem();
//		if (item == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Repository", "Do you really want to remove the package?")) {
//			return;
//		}
//
//		getRepositoryRegistry().removeRepository(item.getRepositoryId());
//		markStale();
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		// the interesting part is our container
		if (!configurationPageContainer.getSite().getId().equals(part.getSite().getId())) {
			return;
		}

		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		final Object firstElement = ((IStructuredSelection) selection).getFirstElement();
		if (!(firstElement instanceof IRepositoryDefinition)) {
			return;
		}

		currentInput = (IRepositoryDefinition) firstElement;
		markStale();
	}
}
