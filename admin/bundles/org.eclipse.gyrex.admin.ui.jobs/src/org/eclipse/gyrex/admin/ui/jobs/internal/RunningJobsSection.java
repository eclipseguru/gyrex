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

//
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
//import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
//import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
//import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperMonitor;
//import org.eclipse.gyrex.jobs.internal.manager.JobHungDetectionHelper;
//import org.eclipse.gyrex.jobs.internal.manager.JobImpl;
//import org.eclipse.gyrex.jobs.internal.manager.JobManagerImpl;
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
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.List;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.forms.IManagedForm;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.Section;
//
//import org.apache.commons.lang.exception.ExceptionUtils;
//import org.apache.zookeeper.WatchedEvent;
//import org.apache.zookeeper.data.Stat;
//
///**
// *
// */
//public class RunningJobsSection extends ViewerWithButtonsSectionPart {
//
//	private Button cancelButton;
//	private ListViewer dataList;
//	private final DataBindingContext bindingContext;
//
//	private IObservableValue selectedValue;
//
//	// FIXME: need a better way to post updates to a RAP application
//	Display display = PlatformUI.getWorkbench().getDisplay();
//	private final ZooKeeperMonitor monitor = new ZooKeeperMonitor() {
//		@Override
//		public void process(final WatchedEvent event) {
//			display.asyncExec(new Runnable() {
//				@Override
//				public void run() {
//					markStale();
//				}
//			});
//		};
//	};
//
//	/**
//	 * Creates a new instance.
//	 *
//	 * @param parent
//	 * @param page
//	 */
//	public RunningJobsSection(final Composite parent, final JobsConfigurationPage page) {
//		super(parent, page.getManagedForm().getToolkit(), ExpandableComposite.SHORT_TITLE_BAR);
//		bindingContext = page.getBindingContext();
//		final Section section = getSection();
//		section.setText("Running");
//		section.setDescription("Currently running jobs.");
//		createContent(section);
//	}
//
//	void cancelButtonPressed() {
//		final RunningJob job = getSelectedValue();
//		if (job == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Cancel Jobe", String.format("Do you really want to cancel job '%s'?", job.id))) {
//			return;
//		}
//
//		final JobImpl jobImpl = JobManagerImpl.getJob(job.id, job.storageKey);
//		JobManagerImpl.cancel(jobImpl, "Cancelled manually from Gyrex Admin");
//
//		job.aborting = true;
//		markStale();
//	}
//
//	@Override
//	protected void createButtons(final Composite buttonsPanel) {
//		cancelButton = createButton(buttonsPanel, "Cancel...", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				cancelButtonPressed();
//			}
//		});
//	}
//
//	@Override
//	protected void createViewer(final Composite parent) {
//		dataList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final List list = dataList.getList();
//		getToolkit().adapt(list, true, true);
//		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		dataList.setContentProvider(new ArrayContentProvider());
//		dataList.setLabelProvider(new JobsLabelProvider());
//
//		selectedValue = ViewersObservables.observeSingleSelection(dataList);
//
//		setDataInput();
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
//	private RunningJob getSelectedValue() {
//		final Object value = null != selectedValue ? selectedValue.getValue() : null;
//		if (value instanceof RunningJob) {
//			return (RunningJob) value;
//		}
//		return null;
//	}
//
//	@Override
//	public void initialize(final IManagedForm form) {
//		super.initialize(form);
//
//		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
//		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
//		getBindingContext().bindValue(SWTObservables.observeEnabled(cancelButton), SWTObservables.observeSelection(dataList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//	}
//
//	@Override
//	public void refresh() {
//		setDataInput();
//
//		super.refresh();
//	}
//
//	private void setDataInput() {
//		try {
//			final java.util.List<RunningJob> input = new ArrayList<RunningJob>();
//			final java.util.List<String> storageKeys = JobHungDetectionHelper.getActiveJobs(monitor);
//			for (final String storageKey : storageKeys) {
//				final Stat stat = new Stat();
//				final String nodeId = JobHungDetectionHelper.getProcessingNodeId(storageKey, stat);
//				final String id = JobManagerImpl.getExternalId(storageKey);
//				input.add(new RunningJob(storageKey, id, nodeId, stat));
//			}
//			dataList.setInput(input);
//		} catch (final IllegalStateException e) {
//			dataList.setInput(Arrays.asList(ExceptionUtils.getRootCauseMessage(e)));
//		}
//	}
//}
