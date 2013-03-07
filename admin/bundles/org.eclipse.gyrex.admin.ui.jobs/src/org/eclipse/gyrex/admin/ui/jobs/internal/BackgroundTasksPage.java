/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.Locale;

import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.schedules.ISchedule;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;

public class BackgroundTasksPage extends AdminPageWithTree {

	public static final String ID = "background-tasks";

	private static final int COLUMN_ID = 0;
	private static final int COLUMN_TIMEZONE = 1;
	private static final int COLUMN_STATE = 2;

	private Button addButton;
	private Button removeButton;
	private Button enableButton;
	private Button disableButton;
	private Button showEntriesButton;

	public BackgroundTasksPage() {
		super(3);
		setTitle("Background Tasks");
		setTitleToolTip("Configure schedules for executing background tasks.");
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

	@Override
	protected void createButtons(final Composite parent) {
		addButton = createButton(parent, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		removeButton = createButton(parent, "Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

		createButtonSeparator(parent);

		enableButton = createButton(parent, "Enable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				enableButtonPressed();
			}
		});

		disableButton = createButton(parent, "Disable");
		disableButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				disableButtonPressed();
			}
		});

		createButtonSeparator(parent);

		showEntriesButton = createButton(parent, "Show Schedule Entries");
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

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new SchedulesContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(parent);
			infobox.addHeading("Schedules.");
			infobox.addParagraph("Background tasks in Gyrex are organized into schedules. A schedule is associated to a context and defines common properties (such as timezone) for all background tasks.");
			infobox.addParagraph("Gyrex schedule are bound to a context path e.g. an application context and they group all the schedules together, which belopng to this application context. Gyrex scheduler can be enabled and disabled to be able to switch the background tasks for a specific application context on and off.");
			return infobox;
		}

		return null;
	}

	void disableButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if ((schedule == null) || !schedule.isEnabled())
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Disable selected Schedule", String.format("Do you really want to disable schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					schedule.setEnabled(false);
					ScheduleStore.flush(schedule.getStorageKey(), schedule);
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
				updateButtons();
			}
		});
	}

	void enableButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if ((schedule == null) || schedule.isEnabled())
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Enable selected Schedule", String.format("Do you really want to enable schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					schedule.setEnabled(true);
					ScheduleStore.flush(schedule.getStorageKey(), schedule);
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
				updateButtons();
			}
		});
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case COLUMN_ID:
				return "Schedule";
			case COLUMN_TIMEZONE:
				return "Time Zone";
			case COLUMN_STATE:
				return "State";

			default:
				return StringUtils.EMPTY;
		}
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if ((element instanceof ContextDefinition) && (column == 0))
			return ((ContextDefinition) element).getPath().toString();

		if (element instanceof ISchedule) {
			final ISchedule schedule = (ISchedule) element;
			switch (column) {
				case COLUMN_ID:
					return schedule.getId();
				case COLUMN_TIMEZONE:
					return schedule.getTimeZone().getDisplayName(Locale.US);
				case COLUMN_STATE:
					return schedule.isEnabled() ? "enabled" : "disabled";

				default:
					break;
			}
		}
		return StringUtils.EMPTY;
	}

	private ScheduleImpl getSelectedSchedule() {
		final IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof ScheduleImpl))
			return (ScheduleImpl) selection.getFirstElement();

		return null;
	}

	@Override
	protected Object getViewerInput() {
		return JobsUiActivator.getInstance().getService(IRuntimeContextDefinitionManager.class);
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return column == COLUMN_ID;
	}

	void removeButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Remove selected Schedule", String.format("Do you really want to delete schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					ScheduleStore.remove(schedule.getStorageKey(), schedule.getId());
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
			}
		});
	}

	void showEntriesButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null)
			return;

		getAdminUi().openPage(ScheduleEntriesPage.ID, new String[] { schedule.getStorageKey() });
	}

	@Override
	protected void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) getTreeViewer().getSelection()).size();
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
