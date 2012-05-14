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

//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
//import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
//import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
//import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
//import org.eclipse.gyrex.admin.ui.pages.AdminPage;
//import org.eclipse.gyrex.persistence.internal.storage.RepositoryRegistry;
//import org.eclipse.gyrex.persistence.storage.registry.IRepositoryDefinition;
//import org.eclipse.gyrex.persistence.storage.registry.IRepositoryRegistry;
//
//import org.eclipse.core.databinding.DataBindingContext;
//import org.eclipse.core.databinding.UpdateValueStrategy;
//import org.eclipse.core.databinding.observable.value.IObservableValue;
//import org.eclipse.jface.databinding.swt.SWTObservables;
//import org.eclipse.jface.databinding.viewers.ViewersObservables;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.layout.GridDataFactory;
//import org.eclipse.jface.viewers.ArrayContentProvider;
//import org.eclipse.jface.viewers.ISelectionProvider;
//import org.eclipse.jface.viewers.ListViewer;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.List;
//import org.eclipse.ui.forms.IManagedForm;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.Section;
//import org.eclipse.ui.model.WorkbenchLabelProvider;
//
//import org.apache.commons.lang.exception.ExceptionUtils;
//
///**
// *
// */
//public class RepositoriesSection extends ViewerWithButtonsSectionPart {
//
//	private Button removeButton;
//	private ListViewer repositoriesList;
//	private final DataBindingContext bindingContext;
//	private IObservableValue selectedRepositoryValue;
//
//	/**
//	 * Creates a new instance.
//	 *
//	 * @param parent
//	 * @param page
//	 */
//	public RepositoriesSection(final Composite parent, final AdminPage page) {
//		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
//		bindingContext = page.getBindingContext();
//		final Section section = getSection();
//		section.setText("Data Repositories");
//		section.setDescription("Define the available repositories.");
//		createContent(section);
//	}
//
//	void addButtonPressed() {
//		// TODO
//	}
//
//	@Override
//	protected void createButtons(final Composite buttonsPanel) {
//		createButton(buttonsPanel, "Add...", new SelectionAdapter() {
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
//
//		final Label separator = getToolkit().createLabel(buttonsPanel, "");
//		FormLayoutDataFactory.applyDefaults(separator, 1);
//	}
//
//	@Override
//	protected void createViewer(final Composite parent) {
//		repositoriesList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final List list = repositoriesList.getList();
//		getToolkit().adapt(list, true, true);
//		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		repositoriesList.setContentProvider(new ArrayContentProvider());
//		repositoriesList.setLabelProvider(new WorkbenchLabelProvider());
//
//		selectedRepositoryValue = ViewersObservables.observeSingleSelection(repositoriesList);
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
//	RepositoryRegistry getRepositoryRegistry() {
//		return (RepositoryRegistry) PersistenceUiActivator.getInstance().getService(IRepositoryRegistry.class);
//	}
//
//	private IRepositoryDefinition getSelectedRepository() {
//		return (IRepositoryDefinition) (null != selectedRepositoryValue ? selectedRepositoryValue.getValue() : null);
//	}
//
//	ISelectionProvider getSelectionProvider() {
//		return repositoriesList;
//	}
//
//	@Override
//	public void initialize(final IManagedForm form) {
//		super.initialize(form);
//
//		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
//		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
//		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(repositoriesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//	}
//
//	@Override
//	public void refresh() {
//		final Collection<String> repositoryIds = getRepositoryRegistry().getRepositoryIds();
//		final java.util.List<Object> input = new ArrayList<Object>(repositoryIds.size());
//		for (final String id : repositoryIds) {
//			try {
//				final IRepositoryDefinition definition = getRepositoryRegistry().getRepositoryDefinition(id);
//				if (null != definition) {
//					input.add(definition);
//				}
//			} catch (final Exception e) {
//				input.add(ExceptionUtils.getRootCauseMessage(e));
//			}
//		}
//		repositoriesList.setInput(input);
//		super.refresh();
//	}
//
//	void removeButtonPressed() {
//		final IRepositoryDefinition repo = getSelectedRepository();
//		if (repo == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Repository", "Do you really want to remove the package?")) {
//			return;
//		}
//
//		getRepositoryRegistry().removeRepository(repo.getRepositoryId());
//		markStale();
//	}
//}
