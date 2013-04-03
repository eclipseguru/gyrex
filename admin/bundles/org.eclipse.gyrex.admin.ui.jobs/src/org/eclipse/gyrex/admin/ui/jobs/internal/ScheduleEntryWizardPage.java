/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.widgets.ElementListSelectionDialog;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DescriptionDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.TreeListDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.jobs.internal.JobsActivator;
import org.eclipse.gyrex.jobs.internal.registry.JobProviderRegistry;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.provider.JobProvider;
import org.eclipse.gyrex.jobs.schedules.IScheduleEntry;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

public class ScheduleEntryWizardPage extends WizardPage {

	private static final Object[] NO_CHILDREN = new Object[0];
	private static final long serialVersionUID = 1L;

	static JobType findJobType(final String jobTypeId) {
		final JobProviderRegistry registry = JobsActivator.getInstance().getJobProviderRegistry();
		final JobProvider provider = registry.getProvider(jobTypeId);
		if (provider != null)
			return new JobType(jobTypeId, registry.getName(jobTypeId), provider);
		return null;
	}

	private final StringDialogField idField = new StringDialogField();

	private final StringButtonDialogField jobTypeField = new StringButtonDialogField(new IStringButtonAdapter() {

		@Override
		public void changeControlPressed(final DialogField field) {
			openJobTypeSelectionDialog();
		}
	}) {

		@Override
		protected Text createTextControl(final Composite parent) {
			return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		}

	};
	private final SelectionButtonDialogField scheduleCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final StringDialogField cronExpressionField = new StringDialogField();

	private final DescriptionDialogField cronMakerLinkField = new DescriptionDialogField();
	private final SelectionButtonDialogField dependsCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final TreeListDialogField preceedingEntriesTree = new TreeListDialogField(new ITreeListAdapter() {

		@Override
		public void customButtonPressed(final TreeListDialogField field, final int index) {
			if (index == 0) {
				final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
				dialog.setTitle("Select Entry");
				dialog.setMessage("&Select an entry that will trigger a run:");
				final List<IScheduleEntry> entries = new ArrayList<>(getSchedule().getEntries());
				entries.remove(getEntry());
				if (!entries.isEmpty()) {
					dialog.setElements(entries.toArray());
					dialog.openNonBlocking(new DialogCallback() {
						private static final long serialVersionUID = 1L;

						@Override
						public void dialogClosed(final int returnCode) {
							if (returnCode == Window.OK) {
								final Object firstResult = dialog.getFirstResult();
								if (firstResult instanceof ScheduleEntryImpl) {
									field.addElement(firstResult);
								}
							}
						}
					});
				} else {
					NonBlockingMessageDialogs.openInformation(getShell(), "No Entries", "Sorry but there are no other entries in this schedule defined.\n\nIn order to build task chains you need to define at least two or more entries within the same schedule.", null);
				}
			}
		}

		@Override
		public void doubleClicked(final TreeListDialogField field) {
			// no-op
		}

		@Override
		public Object[] getChildren(final TreeListDialogField field, final Object element) {
			if (element instanceof ScheduleEntryImpl) {
				final ScheduleEntryImpl entry = (ScheduleEntryImpl) element;
				final Collection<String> precedingEntries = entry.getPrecedingEntries();
				if (!precedingEntries.isEmpty()) {
					final List<Object> result = new ArrayList<>(precedingEntries.size());
					for (final String entryId : precedingEntries) {
						try {
							result.add(getSchedule().getEntry(entryId));
						} catch (final Exception e) {
							result.add(entryId + " (" + e.getMessage() + ")");
						}
					}
					return result.toArray();
				}

			}
			return NO_CHILDREN;
		}

		@Override
		public Object getParent(final TreeListDialogField field, final Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(final TreeListDialogField field, final Object element) {
			return (element instanceof ScheduleEntryImpl) && !((ScheduleEntryImpl) element).getPrecedingEntries().isEmpty();
		}

		@Override
		public void keyPressed(final TreeListDialogField field, final KeyEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void selectionChanged(final TreeListDialogField field) {
			// TODO Auto-generated method stub

		}
	}, new String[] { "Add...", "Remove" }, new LabelProvider());

	{
		preceedingEntriesTree.setRemoveButtonIndex(1);
	}
	private JobType jobType;
	private final ScheduleImpl schedule;

	private final ScheduleEntryImpl entry;

	public ScheduleEntryWizardPage(final ScheduleImpl schedule, final ScheduleEntryImpl entry) {
		super(ScheduleEntryWizardPage.class.getSimpleName());
		setTitle(null != entry ? "Edit Schedule Task" : "Add Task to Schedule");
		setDescription(null != entry ? "Modify scheduling options of a task." : "Add a new task to a schedule.");
		setPageComplete(false);
		this.schedule = schedule;
		this.entry = entry;
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		idField.setLabelText("Entry Id:");
		jobTypeField.setLabelText("Task: ");
		jobTypeField.setButtonLabel("Browse...");

		scheduleCheckBox.setLabelText("Run at specific times (cron expression):");
		cronExpressionField.setLabelText("");
		cronMakerLinkField.setLabelText("");
		cronMakerLinkField.setText("<small>Tip: Use <a href=\"http://cronmaker.com/\" target=\"_blank\">CronMaker</a> to generate cron expressions (but drop the seconds).</small>");

		dependsCheckBox.setLabelText("Run whenever one of the folloing entries run successfully:");
		preceedingEntriesTree.setLabelText("");

		scheduleCheckBox.setAttachedDialogFields(cronExpressionField, cronMakerLinkField);
		dependsCheckBox.setAttachedDialogFields(preceedingEntriesTree);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		jobTypeField.setDialogFieldListener(validateListener);
		scheduleCheckBox.setDialogFieldListener(validateListener);
		cronExpressionField.setDialogFieldListener(validateListener);
		dependsCheckBox.setDialogFieldListener(validateListener);
		preceedingEntriesTree.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, jobTypeField, new Separator(), scheduleCheckBox, cronExpressionField, cronMakerLinkField, new Separator(), dependsCheckBox, preceedingEntriesTree }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(jobTypeField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(cronExpressionField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(cronMakerLinkField.getDescriptionControl(null));
		LayoutUtil.setHorizontalGrabbing(preceedingEntriesTree.getTreeControl(null));

		if (null != getEntry()) {
			idField.setEnabled(false);
			jobTypeField.setEnabled(false);
			idField.setText(getEntry().getId());
			setJobType(findJobType(getEntry().getJobTypeId()));
			if (null != getEntry().getCronExpression()) {
				scheduleCheckBox.setSelection(true);
				cronExpressionField.setText(getEntry().getCronExpression());
			}
			if (!getEntry().getPrecedingEntries().isEmpty()) {
				dependsCheckBox.setSelection(true);
				for (final String entryId : getEntry().getPrecedingEntries()) {
					try {
						preceedingEntriesTree.addElement(getSchedule().getEntry(entryId));
					} catch (final IllegalStateException e) {
						preceedingEntriesTree.addElement(entryId);
					}
				}
			}
		}

	}

	public String getCronExpression() {
		return cronExpressionField.getText();
	}

	public ScheduleEntryImpl getEntry() {
		return entry;
	}

	public String getEntryId() {
		return idField.getText();
	}

	public String getJobTypeId() {
		return jobType != null ? jobType.id : null;
	}

	public String[] getPreceedingEntryIds() {
		final List<Object> elements = preceedingEntriesTree.getElements();
		final String[] result = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			result[i] = ((ScheduleEntryImpl) elements.get(i)).getId();
		}
		return result;
	}

	public ScheduleImpl getSchedule() {
		return schedule;
	}

	ScheduleEntryWizard getScheduleEntryWizard() {
		return (ScheduleEntryWizard) getWizard();
	}

	public boolean isScheduleUsingCronExpression() {
		return scheduleCheckBox.isSelected();
	}

	public boolean isScheduleUsingPreceedingEntries() {
		return dependsCheckBox.isSelected();
	}

	void openJobTypeSelectionDialog() {
		final JobTypeSelectionDialog dialog = new JobTypeSelectionDialog(getShell());
		if (jobType != null) {
			dialog.setInitialPattern(jobType.getName());
		}
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					final JobType jobType = (JobType) dialog.getFirstResult();
					setJobType(jobType);
				}

			}
		});

	}

	public void setJobType(final JobType jobType) {
		if (jobType == this.jobType)
			return;

		this.jobType = jobType;

		if (jobType != null) {
			jobTypeField.setText(jobType.getName());
			getScheduleEntryWizard().initializeCurrentJobConfigurationSession(jobType.id, jobType.getWizardAdapter());
		} else {
			jobTypeField.setText(StringUtils.EMPTY);
			getScheduleEntryWizard().clearCurrentJobConfigurationSession();
		}

		validate();
	}

	void validate() {
		final String id = getEntryId();
		if (StringUtils.isNotBlank(id)) {
			if (!IdHelper.isValidId(id)) {
				setMessage("The entered entry id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.", IMessageProvider.ERROR);
				setPageComplete(false);
				return;
			}
			if ((null == getEntry()) && getSchedule().hasEntry(id)) {
				setMessage(String.format("Schedule '%s' already contains an entry with the specified id.", getSchedule().getId()), IMessageProvider.ERROR);
				setPageComplete(false);
				return;
			}
		}

		if (StringUtils.isBlank(id)) {
			setMessage("Please enter an entry identifier.", INFORMATION);
			setPageComplete(false);
			return;
		}

		if ((jobType == null) || StringUtils.isBlank(getJobTypeId())) {
			setMessage("Please select a task.", INFORMATION);
			setPageComplete(false);
			return;
		}

		if (isScheduleUsingCronExpression()) {
			final String cronExpression = getCronExpression();
			if (StringUtils.isNotBlank(cronExpression)) {
				try {
					ScheduleEntryImpl.validateCronExpression(cronExpression);
				} catch (final IllegalArgumentException e) {
					setMessage("The cron expression is invalid. " + e.getMessage(), IMessageProvider.ERROR);
					setPageComplete(false);
					return;
				}
			} else {
				setMessage("Please enter a cron expression.", INFORMATION);
				setPageComplete(false);
				return;
			}
		}

		if (isScheduleUsingPreceedingEntries()) {
			if (preceedingEntriesTree.getElements().isEmpty()) {
				setMessage("Please select an entry this task depends on.", INFORMATION);
				setPageComplete(false);
				return;
			}
		}

		setMessage(null);
		setPageComplete(true);
	}
}
