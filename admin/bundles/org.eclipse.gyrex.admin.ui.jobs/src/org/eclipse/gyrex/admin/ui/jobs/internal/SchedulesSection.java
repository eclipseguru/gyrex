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

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.schedules.ISchedule;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.osgi.service.prefs.BackingStoreException;

public class SchedulesSection {

	private Composite schedulesPanel;
	private ListViewer schedulesList;
	private Button addButton;
	private Button removeButton;
	private ISelectionChangedListener updateButtonsListener;
	private Button enableButton;
	private Button disableButton;
	private Button showEntriesButton;
	private final AdminPage page;

	/**
	 * Creates a new instance.
	 * 
	 * @param backgroundTasksPage
	 */
	public SchedulesSection(final AdminPage page) {
		this.page = page;
	}

	public void activate() {

		if (schedulesList != null) {
			schedulesList.setInput(ISchedule.class);
			updateButtonsListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			schedulesList.addSelectionChangedListener(updateButtonsListener);
		} else {
		}

	}

	void addButtonPressed() {
		final AddScheduleDialog dialog = new AddScheduleDialog(SwtUtil.getShell(addButton));
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

	/**
	 * @param composite
	 */
	public void createSchedulesControls(final Composite parent) {

		schedulesPanel = new Composite(parent, SWT.NONE);
		schedulesPanel.setLayout(new GridLayout());
		schedulesPanel.setLayoutData(AdminUiUtil.createFillData());

		final Composite listAndButtonsPanel = new Composite(schedulesPanel, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		listAndButtonsPanel.setLayoutData(gd);
		listAndButtonsPanel.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		schedulesList = new ListViewer(listAndButtonsPanel, SWT.SINGLE | SWT.BORDER);
		final List list = schedulesList.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		schedulesList.setContentProvider(new ArrayContentProvider());
		schedulesList.setLabelProvider(new JobsLabelProvider());
		schedulesList.setContentProvider(new SchedulesContentProvider());

		final Composite buttons = new Composite(listAndButtonsPanel, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());

		addButton = createButton(buttons, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		enableButton = createButton(buttons, "Enable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				enableButtonPressed();
			}
		});

		disableButton = createButton(buttons, "Disable");
		disableButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				disableButtonPressed();
			}
		});

		removeButton = createButton(buttons, "Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

		showEntriesButton = createButton(buttons, "Show Schedule Entries");
		showEntriesButton.setEnabled(false);
		showEntriesButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				showEntriesButtonPressed();
			}
		});
	}

	void disableButtonPressed() {

		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null || !schedule.isEnabled()) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(schedulesPanel), "Disable selected Schedule", String.format("Do you really want to disable schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				try {
					schedule.setEnabled(false);
					ScheduleStore.flush(schedule.getStorageKey(), schedule);
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
			}
		});
	}

	void enableButtonPressed() {

		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null || schedule.isEnabled()) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(schedulesPanel), "Enable selected Schedule", String.format("Do you really want to enable schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				try {
					schedule.setEnabled(true);
					ScheduleStore.flush(schedule.getStorageKey(), schedule);
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
			}
		});
	}

	public Composite getComposite() {
		return schedulesPanel;
	}

	private ScheduleImpl getSelectedSchedule() {
		final IStructuredSelection selection = (IStructuredSelection) schedulesList.getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof ScheduleImpl) {
			return (ScheduleImpl) selection.getFirstElement();
		}

		return null;
	}

	public void refresh() {
		schedulesList.refresh();
		updateButtons();
	}

	void removeButtonPressed() {

		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(schedulesPanel), "Remove selected Schedule", String.format("Do you really want to delete schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				try {
					ScheduleStore.remove(schedule.getStorageKey(), schedule.getId());
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
			}
		});
	}

	/**
	 * 
	 */
	protected void showEntriesButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null) {
			return;
		}

		page.getAdminUi().openPage(ScheduleEntriesPage.ID, new String[] { schedule.getStorageKey() });

//		scheduleEntriesSection = new ScheduleEntriesSection(this);
//		scheduleEntriesSection.setSchedule(schedule);
//		scheduleEntriesSection.createScheduleEntriesControls(stackComposite);
//		scheduleEntriesSection.activate();
//		show(scheduleEntriesSection.getComposite());
	}

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) schedulesList.getSelection()).size();
		if (selectedElementsCount == 0) {
			addButton.setEnabled(true);
			removeButton.setEnabled(false);
			enableButton.setEnabled(false);
			disableButton.setEnabled(false);
			showEntriesButton.setEnabled(false);
			return;
		}

		addButton.setEnabled(true);
		removeButton.setEnabled(selectedElementsCount == 1);
		showEntriesButton.setEnabled(selectedElementsCount == 1);

		final ScheduleImpl selectedSchedule = getSelectedSchedule();
		if (selectedSchedule != null) {
			if (selectedSchedule.isEnabled()) {
				enableButton.setEnabled(false);
				disableButton.setEnabled(true);
			} else {
				enableButton.setEnabled(true);
				disableButton.setEnabled(false);
			}
		}
	}

}

//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
//import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
//import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
//import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
//import org.eclipse.gyrex.jobs.internal.schedules.ScheduleManagerImpl;
//import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
//import org.eclipse.gyrex.jobs.schedules.ISchedule;
//import org.eclipse.gyrex.jobs.schedules.IScheduleEntry;
//
//import org.eclipse.core.databinding.DataBindingContext;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.layout.GridDataFactory;
//import org.eclipse.jface.util.Policy;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.ISelectionChangedListener;
//import org.eclipse.jface.viewers.ISelectionProvider;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.ITreeContentProvider;
//import org.eclipse.jface.viewers.SelectionChangedEvent;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Tree;
//import org.eclipse.ui.forms.IManagedForm;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.Section;
//
//import org.osgi.service.prefs.BackingStoreException;
//
///**
// *
// */
//public class SchedulesSection extends ViewerWithButtonsSectionPart {
//
//	static class ViewContentProvider implements ITreeContentProvider {
//
//		/** serialVersionUID */
//		private static final long serialVersionUID = 1L;
//
//		private static final Object[] NO_CHILDREN = new Object[0];
//
//		public void dispose() {
//		}
//
//		@Override
//		public Object[] getChildren(final Object parent) {
//			return getElements(parent);
//		}
//
//		public Object[] getElements(final Object parent) {
//			if (parent instanceof ISchedule) {
//				return ((ISchedule) parent).getEntries().toArray();
//			} else if (parent instanceof Collection<?>) {
//				return ((Collection<?>) parent).toArray();
//			} else {
//				return NO_CHILDREN;
//			}
//		}
//
//		@Override
//		public Object getParent(final Object element) {
//			// TODO: need parent of entry
////			if (element instanceof IScheduleEntry) {
////				return ((IScheduleEntry) element).getParent();
////			}
//			return null;
//		}
//
//		@Override
//		public boolean hasChildren(final Object element) {
//			if (element instanceof ISchedule) {
//				return !((ISchedule) element).getEntries().isEmpty();
//			} else {
//				return false;
//			}
//		}
//
//		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
//		}
//	}
//
//	private TreeViewer dataTree;
//	private final DataBindingContext bindingContext;
//	private Object selectedValue;
//
//	private Button addButton;
//	private Button removeButton;
//	private Button enableButton;
//	private Button disableButton;
//
//	/**
//	 * Creates a new instance.
//	 *
//	 * @param parent
//	 * @param page
//	 */
//	public SchedulesSection(final Composite parent, final JobsConfigurationPage page) {
//		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
//		bindingContext = page.getBindingContext();
//		final Section section = getSection();
//		section.setText("Schedules");
//		section.setDescription("Manage the available schedules.");
//		createContent(section);
//	}
//
//	void addButtonPressed() {
////		final AddRepositoryDialog dialog = new AddRepositoryDialog(SwtUtil.getShell(addButton), getRepoManager());
////		if (dialog.open() == Window.OK) {
////			markStale();
////		}
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
//
//		enableButton = createButton(buttonsPanel, "Enable", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				enableButtonPressed();
//			}
//		});
//		disableButton = createButton(buttonsPanel, "Disable", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				disableButtonPressed();
//			}
//		});
//	}
//
//	@Override
//	protected void createViewer(final Composite parent) {
//		dataTree = new TreeViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final Tree tree = dataTree.getTree();
//		getToolkit().adapt(tree, true, true);
//		tree.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		dataTree.setContentProvider(new ViewContentProvider());
//		dataTree.setLabelProvider(new JobsLabelProvider());
//
//		dataTree.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override
//			public void selectionChanged(final SelectionChangedEvent event) {
//				final ISelection selection = event.getSelection();
//				if (selection instanceof IStructuredSelection) {
//					setSelectedValue(((IStructuredSelection) selection).getFirstElement());
//				}
//			}
//		});
//	}
//
//	void disableButtonPressed() {
//		if (selectedValue instanceof ScheduleImpl) {
//			final ScheduleImpl schedule = (ScheduleImpl) selectedValue;
//			schedule.setEnabled(false);
//			try {
//				schedule.save();
//			} catch (final BackingStoreException e) {
//				Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, e.getMessage(), e), "Error Disabling Schedule");
//			}
//			markStale();
//		} else if (selectedValue instanceof ScheduleEntryImpl) {
//			final ScheduleEntryImpl entry = (ScheduleEntryImpl) selectedValue;
//			entry.setEnabled(false);
//			try {
//				entry.getSchedule().save();
//			} catch (final BackingStoreException e) {
//				Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, e.getMessage(), e), "Error Disabling Schedule Entry");
//			}
//			markStale();
//		}
//	}
//
//	void enableButtonPressed() {
//		if (selectedValue instanceof ScheduleImpl) {
//			final ScheduleImpl schedule = (ScheduleImpl) selectedValue;
//			schedule.setEnabled(true);
//			try {
//				schedule.save();
//			} catch (final BackingStoreException e) {
//				Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, e.getMessage(), e), "Error Enabling Schedule");
//			}
//			markStale();
//		} else if (selectedValue instanceof ScheduleEntryImpl) {
//			final ScheduleEntryImpl entry = (ScheduleEntryImpl) selectedValue;
//			entry.setEnabled(true);
//			try {
//				entry.getSchedule().save();
//			} catch (final BackingStoreException e) {
//				Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, e.getMessage(), e), "Error Enabling Schedule Entry");
//			}
//			markStale();
//		}
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
//	private ISchedule getSelectedSchedule() {
//		return (ISchedule) ((selectedValue instanceof ISchedule) ? selectedValue : null);
//	}
//
//	public ISelectionProvider getSelectionProvider() {
//		return dataTree;
//	}
//
//	@Override
//	public void initialize(final IManagedForm form) {
//		super.initialize(form);
//	}
//
//	@Override
//	public void refresh() {
//		try {
//			final String[] schedulesStorageKeys = ScheduleStore.getSchedules();
//			final ArrayList<ISchedule> schedules = new ArrayList<ISchedule>(schedulesStorageKeys.length);
//			for (final String key : schedulesStorageKeys) {
//				final ScheduleImpl schedule = ScheduleStore.load(key, ScheduleManagerImpl.getExternalId(key), false);
//				if (null != schedule) {
//					schedules.add(schedule);
//				}
//			}
//			dataTree.setInput(schedules);
//
//			// update selection
//			dataTree.setSelection(dataTree.getSelection());
//		} catch (final BackingStoreException e) {
//			e.printStackTrace();
//			dataTree.setInput(new ISchedule[0]);
//		}
//		super.refresh();
//	}
//
//	void removeButtonPressed() {
//		final ScheduleImpl schedule = (ScheduleImpl) getSelectedSchedule();
//		if (schedule == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Schedule", "Do you really want to delete the schedule?")) {
//			return;
//		}
//
//		try {
//			ScheduleStore.remove(schedule.getStorageKey(), schedule.getId());
//		} catch (final BackingStoreException e) {
//			e.printStackTrace();
//		}
//		markStale();
//	}
//
//	void setSelectedValue(final Object element) {
//		if (element instanceof ISchedule) {
//			selectedValue = element;
//		} else {
//			selectedValue = null;
//		}
//
//		addButton.setEnabled(false);
//		removeButton.setEnabled(selectedValue != null);
//
//		enableButton.setEnabled(((selectedValue instanceof ISchedule) && !((ISchedule) selectedValue).isEnabled()) || ((selectedValue instanceof IScheduleEntry) && !((IScheduleEntry) selectedValue).isEnabled()));
//		disableButton.setEnabled(((selectedValue instanceof ISchedule) && ((ISchedule) selectedValue).isEnabled()) || ((selectedValue instanceof IScheduleEntry) && ((IScheduleEntry) selectedValue).isEnabled()));
//	}
//
//}
