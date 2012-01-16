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
import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleManagerImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.schedules.ISchedule;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import org.osgi.service.prefs.BackingStoreException;

/**
 *
 */
public class SchedulesSection extends ViewerWithButtonsSectionPart {

	static class ViewContentProvider implements ITreeContentProvider {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		private static final Object[] NO_CHILDREN = new Object[0];

		public void dispose() {
		}

		@Override
		public Object[] getChildren(final Object parent) {
			return getElements(parent);
		}

		public Object[] getElements(final Object parent) {
			if (parent instanceof ISchedule) {
				return ((ISchedule) parent).getEntries().toArray();
			} else if (parent instanceof Collection<?>) {
				return ((Collection<?>) parent).toArray();
			} else {
				return NO_CHILDREN;
			}
		}

		@Override
		public Object getParent(final Object element) {
			// TODO: need parent of entry
//			if (element instanceof IScheduleEntry) {
//				return ((IScheduleEntry) element).getParent();
//			}
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof ISchedule) {
				return !((ISchedule) element).getEntries().isEmpty();
			} else {
				return false;
			}
		}

		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		}
	}

	private Button addButton;
	private Button removeButton;
	private TreeViewer dataTree;
	private final DataBindingContext bindingContext;

	private Object selectedValue;

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
		dataTree = new TreeViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final Tree tree = dataTree.getTree();
		getToolkit().adapt(tree, true, true);
		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		dataTree.setContentProvider(new ViewContentProvider());
		dataTree.setLabelProvider(new JobsLabelProvider());

		dataTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					setSelectedValue(((IStructuredSelection) selection).getFirstElement());
				}
			}
		});
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	private ISchedule getSelectedSchedule() {
		return (ISchedule) ((selectedValue instanceof ISchedule) ? selectedValue : null);
	}

	public ISelectionProvider getSelectionProvider() {
		return dataTree;
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);
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
			dataTree.setInput(schedules);
		} catch (final BackingStoreException e) {
			e.printStackTrace();
			dataTree.setInput(new ISchedule[0]);
		}
		super.refresh();
	}

	void removeButtonPressed() {
		final ScheduleImpl schedule = (ScheduleImpl) getSelectedSchedule();
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

	void setSelectedValue(final Object element) {
		if (element instanceof ISchedule) {
			selectedValue = element;
		} else {
			selectedValue = null;
		}

		removeButton.setEnabled(selectedValue != null);
	}

}
