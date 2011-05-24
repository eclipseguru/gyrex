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
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleManagerImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.schedules.ISchedule;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
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

import org.osgi.service.prefs.BackingStoreException;

/**
 *
 */
public class SchedulesSection extends ViewerWithButtonsSectionPart {

	private Button addButton;
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
	public SchedulesSection(final Composite parent, final JobsConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		bindingContext = page.getBindingContext();
		final Section section = getSection();
		section.setText("Schedules");
		section.setDescription("Manage the available schedules.");
		createContent(section);
	}

	void addButtonPressed() {
//		final AddRepositoryDialog dialog = new AddRepositoryDialog(SwtUtil.getShell(addButton), getRepoManager());
//		if (dialog.open() == Window.OK) {
//			markStale();
//		}
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

	private ISchedule getSelectedValue() {
		return (ISchedule) (null != selectedValue ? selectedValue.getValue() : null);
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
		try {
			final String[] schedulesStorageKeys = ScheduleStore.getSchedules();
			final ArrayList<ISchedule> schedules = new ArrayList<ISchedule>(schedulesStorageKeys.length);
			for (final String key : schedulesStorageKeys) {
				final ScheduleImpl schedule = ScheduleStore.load(key, ScheduleManagerImpl.getExternalId(key), false);
				if (null != schedule) {
					schedules.add(schedule);
				}
			}
			dataList.setInput(schedules);
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			dataList.setInput(new ISchedule[0]);
		}
		super.refresh();
	}

	void removeButtonPressed() {
		final ScheduleImpl schedule = (ScheduleImpl) getSelectedValue();
		if (schedule == null) {
			return;
		}

		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Schedule", "Do you really want to delete the schedule?")) {
			return;
		}

		try {
			ScheduleStore.remove(schedule.getStorageKey(), schedule.getId());
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
		markStale();
	}

}
