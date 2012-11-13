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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleManagerImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.server.Platform;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;

import org.osgi.service.prefs.BackingStoreException;

public class ScheduleEntriesPage extends AdminPage {

	public static final String ID = "schedule-entries";

	private Composite scheduleEntriesPanel;
	private ListViewer scheduleEntriesList;
	private Button addButton;
	private Button removeButton;
	private ISelectionChangedListener updateButtonsListener;
	private Button enableButton;
	private Button disableButton;
	private ScheduleImpl schedule;
	private Button backButton;

	private Composite pageComposite;

	private Link backLink;

	/**
	 * Creates a new instance.
	 */
	public ScheduleEntriesPage() {
		setTitle("Schedule Entries");
		setTitleToolTip("Configure the entries of a schedule for executing background tasks.");
	}

	@Override
	public void activate() {

		if (scheduleEntriesList != null) {
			scheduleEntriesList.setInput(schedule);
			updateButtonsListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			scheduleEntriesList.addSelectionChangedListener(updateButtonsListener);
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

	/**
	 * 
	 */
	protected void backButtonPressed() {
		getAdminUi().openPage(BackgroundTasksPage.ID, new String[] {});
	}

	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button b = new Button(buttons, SWT.NONE);
		b.setText(buttonLabel);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return b;
	}

	@Override
	public Control createControl(final Composite parent) {

		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(pageComposite);
			infobox.addHeading("Gyrex Schedule Entries");
			infobox.addParagraph("Are the itme, which define, when and in which time pattern a job should run. They are defined by the job id and a cron expression (based on the quartz syntax). You can also define an alternate timezone as the basis for the schedule trigger.");
			final GridData gd = AdminUiUtil.createHorzFillData();
			infobox.setLayoutData(gd);
		}

		backLink = new Link(pageComposite, SWT.WRAP | SWT.READ_ONLY);
		backLink.setText("Back to <a>schedules list</a>");
		backLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		backLink.moveAbove(null);
		backLink.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				backButtonPressed();
			}
		});

		final ScheduleImpl schedule = getSchedule();
		if (schedule != null) {
			setSchedule(schedule);
			createScheduleEntriesControls(pageComposite);
			return pageComposite;
		}
		return pageComposite;
	}

	/**
	 * @param composite
	 */
	public void createScheduleEntriesControls(final Composite parent) {

		scheduleEntriesPanel = new Composite(parent, SWT.NONE);
		scheduleEntriesPanel.setLayout(new GridLayout());
		scheduleEntriesPanel.setLayoutData(AdminUiUtil.createFillData());

//		backButton = createButton(scheduleEntriesPanel, "Back To Schedules");
//		backButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent event) {
//				backButtonPressed();
//			}
//		});

		final Composite listAndButtonsPanel = new Composite(scheduleEntriesPanel, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		listAndButtonsPanel.setLayoutData(gd);
		listAndButtonsPanel.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		scheduleEntriesList = new ListViewer(listAndButtonsPanel, SWT.SINGLE | SWT.BORDER);
		final List list = scheduleEntriesList.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		scheduleEntriesList.setContentProvider(new ArrayContentProvider());
		scheduleEntriesList.setLabelProvider(new JobsLabelProvider());
		scheduleEntriesList.setContentProvider(new ScheduleEntriesContentProvider());

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

	}

	void disableButtonPressed() {

		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(scheduleEntriesPanel), "Disable selected Schedule Entry ", String.format("Do you really want to disable schedule entry %s?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				//scheduleEntry.setEnabled(false);
				// TODO set disabled

				refresh();
			}
		});
	}

	void enableButtonPressed() {

		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(scheduleEntriesPanel), "Enable selected Schedule Entry ", String.format("Do you really want to enable schedule entry %s?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				//scheduleEntry.setEnabled(true);
				// TODO set enabled

				refresh();
			}
		});
	}

	/**
	 * Returns the schedule.
	 * 
	 * @return the schedule
	 */
	public ScheduleImpl getSchedule() {
		return schedule;
	}

	private ScheduleEntryImpl getSelectedScheduleEntry() {
		final IStructuredSelection selection = (IStructuredSelection) scheduleEntriesList.getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof ScheduleEntryImpl) {
			return (ScheduleEntryImpl) selection.getFirstElement();
		}

		return null;
	}

	public void refresh() {
		scheduleEntriesList.refresh();
		updateButtons();
	}

	void removeButtonPressed() {

		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(scheduleEntriesPanel), "Remove selected Schedule entry ", String.format("Do you really want to delete schedule entry %s?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				//ScheduleStore.(scheduleEntry.getStorageKey(), scheduleEntry.getId());
				// TODO remove schedule entry

				refresh();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.admin.ui.pages.AdminPage#setArguments(java.lang.String[])
	 */
	@Override
	public void setArguments(final String[] args) {
		super.setArguments(args);
		if (args.length > 1) {

			final String storageKey = args[1];
			try {
				final ScheduleImpl schedule = ScheduleStore.load(storageKey, ScheduleManagerImpl.getExternalId(storageKey), false);
				if (schedule != null) {
					setSchedule(schedule);
					setTitle("Schedule Entries of " + schedule.getId());
				}
			} catch (final BackingStoreException e) {
			}
		}

	}

	/**
	 * Sets the schedule.
	 * 
	 * @param schedule
	 *            the schedule to set
	 */
	public void setSchedule(final ScheduleImpl schedule) {
		this.schedule = schedule;
	}

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) scheduleEntriesList.getSelection()).size();
		if (selectedElementsCount == 0) {
			addButton.setEnabled(true);
			removeButton.setEnabled(false);
			enableButton.setEnabled(false);
			disableButton.setEnabled(false);
			return;
		}

		addButton.setEnabled(true);
		removeButton.setEnabled(selectedElementsCount == 1);

		final ScheduleEntryImpl selectedScheduleEntry = getSelectedScheduleEntry();
		if (selectedScheduleEntry != null) {
			if (selectedScheduleEntry.isEnabled()) {
				enableButton.setEnabled(false);
				disableButton.setEnabled(true);
			} else {
				enableButton.setEnabled(true);
				disableButton.setEnabled(false);
			}
		}
	}

}
