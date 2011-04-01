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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.ArrayList;

import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 *
 */
public class JobsLogSection extends ViewerWithButtonsSectionPart {

	private Button removeButton;
	private ListViewer dataList;
	private final DataBindingContext bindingContext;
	private IObservableValue selectedValue;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public JobsLogSection(final Composite parent, final JobsConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), ExpandableComposite.SHORT_TITLE_BAR);
		bindingContext = page.getBindingContext();
		final Section section = getSection();
		section.setText("Logs");
		section.setDescription("View logs of recently executed jobs.");
		createContent(section);
	}

	@Override
	protected void createButtons(final Composite buttonsPanel) {
		removeButton = createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});
	}

	@Override
	protected void createViewer(final Composite parent) {
		dataList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = dataList.getList();
		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		dataList.setContentProvider(new ArrayContentProvider());
		dataList.setLabelProvider(new JobsLabelProvider());

		selectedValue = ViewersObservables.observeSingleSelection(dataList);
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	JobLog getSelectedValue() {
		return (JobLog) (null != selectedValue ? selectedValue.getValue() : null);
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(dataList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
	}

	@Override
	public void refresh() {
		final ArrayList<JobLog> logs = new ArrayList<JobLog>();
		logs.add(new JobLog("SAP Order Status Sync, 2 minutes ago", false, false));
		logs.add(new JobLog("SAP Order Status Sync, 10 minutes ago", true, false));
		logs.add(new JobLog("Friday Newsletter, 2011-04-01 08:00", false, false));
		logs.add(new JobLog("SAP Order Status Sync, 2011-04-01 06:00", true, false));
		logs.add(new JobLog("AMOS Full Product Load, 2011-04-01 02:00", false, true));
		logs.add(new JobLog("AMOS Full Product Load, 2011-03-31 02:00", false, true));
		dataList.setInput(logs);
		super.refresh();
	}

	void removeButtonPressed() {
//		final ISchedule repo = getSelectedValue();
//		if (repo == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Schedule", "Do you really want to delete the schedule?")) {
//			return;
//		}
//
//		getRepoManager().removeSchedule(repo.getId());
//		markStale();
	}

}
