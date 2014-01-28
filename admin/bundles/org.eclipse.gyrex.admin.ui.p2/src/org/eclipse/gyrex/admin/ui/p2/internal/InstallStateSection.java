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
package org.eclipse.gyrex.admin.ui.p2.internal;

//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//
//import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
//import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
//import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
//import org.eclipse.gyrex.cloud.internal.zk.IZooKeeperLayout;
//import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;
//import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperMonitor;
//import org.eclipse.gyrex.p2.internal.installer.PackageInstallerJob;
//
//import org.eclipse.core.databinding.DataBindingContext;
//import org.eclipse.core.databinding.UpdateValueStrategy;
//import org.eclipse.core.databinding.observable.value.IObservableValue;
//import org.eclipse.core.runtime.IPath;
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
//import org.eclipse.swt.widgets.List;
//import org.eclipse.ui.forms.IManagedForm;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.Section;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.zookeeper.KeeperException.NoNodeException;
//
///**
// *
// */
//public class InstallStateSection extends ViewerWithButtonsSectionPart {
//
//	/** APPEND */
//	private static final IPath LOCKS_PATH = IZooKeeperLayout.PATH_LOCKS_DURABLE.append(PackageInstallerJob.ID_INSTALL_LOCK);
//	private Button cancelButton;
//	private ListViewer nodeList;
//	private final DataBindingContext bindingContext;
//	private IObservableValue selectedValue;
//
//	final ZooKeeperMonitor monitor = new ZooKeeperMonitor() {
//		@Override
//		protected void childrenChanged(final String path) {
//			if (!getSection().isDisposed()) {
//				markStale();
//			}
//		};
//	};
//
//	/**
//	 * Creates a new instance.
//	 *
//	 * @param parent
//	 * @param page
//	 */
//	public InstallStateSection(final Composite parent, final SoftwareLandingPage page) {
//		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
//		bindingContext = page.getBindingContext();
//		final Section section = getSection();
//		section.setText("Active Installation");
//		section.setDescription("Monitor the active install operations.");
//		createContent(section);
//	}
//
//	void cancelButtonPressed() {
//		final Node node = getSelectedNode();
//		if (node == null) {
//			return;
//		}
//
//		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Cancel Installation", "Do you really want to cancel the installation?")) {
//			return;
//		}
//
//		if (StringUtils.isNotBlank(node.getLockName())) {
//			try {
//				final ZooKeeperGate zk = ZooKeeperGate.get();
//				zk.deletePath(LOCKS_PATH.append(node.getLockName()));
//			} catch (final Exception e) {
//				// ignore
//			}
//		}
//		markStale();
//	}
//
//	@Override
//	protected void createButtons(final Composite buttonsPanel) {
//		cancelButton = createButton(buttonsPanel, "Cancel", new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				cancelButtonPressed();
//			}
//		});
//	}
//
//	@Override
//	protected void createViewer(final Composite parent) {
//		nodeList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//
//		final List list = nodeList.getList();
//		getToolkit().adapt(list, true, true);
//		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
//
//		nodeList.setContentProvider(new ArrayContentProvider());
//		nodeList.setLabelProvider(new P2UiLabelProvider());
//
//		selectedValue = ViewersObservables.observeSingleSelection(nodeList);
//	}
//
//	private Object[] getActiveNodesInstallingStuff() {
//		// this is a hack
//		// we need to think about some official api for this
//		try {
//			final ZooKeeperGate zk = ZooKeeperGate.get();
//			final Collection<String> locks = zk.readChildrenNames(LOCKS_PATH, monitor, null);
//			final ArrayList<Node> nodes = new ArrayList<Node>();
//			for (final Iterator stream = locks.iterator(); stream.hasNext();) {
//				final String lock = (String) stream.next();
//				final String record = zk.readRecord(LOCKS_PATH.append(lock), "", null);
//				if (StringUtils.isBlank(record)) {
//					continue;
//				}
//				final String[] segments = StringUtils.splitByWholeSeparator(record, "__");
//				if (segments.length > 2) {
//					nodes.add(new Node(segments[0], segments[1], lock));
//				}
//			}
//			return nodes.toArray();
//		} catch (final NoNodeException e) {
//			return new Object[0];
//		} catch (final IllegalStateException e) {
//			return new Object[0];
//		} catch (final InterruptedException e) {
//			return new Object[0];
//		} catch (final Exception e) {
//			return new Object[] { new Node(e.getClass().getSimpleName(), e.getMessage(), null) };
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
//	private Node getSelectedNode() {
//		return (Node) (null != selectedValue ? selectedValue.getValue() : null);
//	}
//
//	@Override
//	public void initialize(final IManagedForm form) {
//		super.initialize(form);
//
//		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
//		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
//		getBindingContext().bindValue(SWTObservables.observeEnabled(cancelButton), SWTObservables.observeSelection(nodeList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
//	}
//
//	@Override
//	public void refresh() {
//		nodeList.setInput(getActiveNodesInstallingStuff());
//		super.refresh();
//	}
//
//}
