/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeDescriptor;
import org.eclipse.gyrex.cloud.admin.INodeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public abstract class NodeListSection extends ViewerWithButtonsSectionPart {

	static class NodesLabelProvider extends LabelProvider {
		@Override
		public String getText(final Object element) {
			if (element instanceof INodeDescriptor) {
				final INodeDescriptor node = (INodeDescriptor) element;
				if (StringUtils.isNotBlank(node.getName())) {
					return node.getName();
				}
				return node.getId();
			}
			return super.getText(element);
		}
	}

	private Button editButton;
	protected ListViewer nodesList;
	protected IViewerObservableValue selectedNodeValue;

	private final INodeListener nodeListener = new INodeListener() {

		@Override
		public void nodesChanged() {
			markStale();
		}
	};
	private ICloudManager cloudManager;
	private final DataBindingContext bindingContext;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public NodeListSection(final Composite parent, final FormToolkit toolkit, final int style, final DataBindingContext bindingContext) {
		super(parent, toolkit, style);
		this.bindingContext = bindingContext;
	}

	@Override
	protected void createButtons(final Composite parent) {
		editButton = createButton(parent, "Edit...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				editButtonPressed();
			}
		});
	}

	@Override
	protected void createViewer(final Composite parent) {
		nodesList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = nodesList.getList();
		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).grab(true, true).create());

		nodesList.setContentProvider(new ArrayContentProvider());
		nodesList.setLabelProvider(new NodesLabelProvider());

		selectedNodeValue = ViewersObservables.observeSingleSelection(nodesList);
	}

	void editButtonPressed() {
		final INodeDescriptor descriptor = (INodeDescriptor) (null != selectedNodeValue ? selectedNodeValue.getValue() : null);
		if (descriptor == null) {
			return;
		}

		final EditNodeDialog dialog = new EditNodeDialog(SwtUtil.getShell(editButton), getCloudManager(), descriptor);
		if (dialog.open() == Window.OK) {
			markStale();
		}
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	protected ICloudManager getCloudManager() {
		return cloudManager;
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		if (editButton != null) {
			final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
			modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
			getBindingContext().bindValue(SWTObservables.observeEnabled(editButton), SWTObservables.observeSelection(nodesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
		}
	}

	protected abstract Collection loadNodes(final ICloudManager cloudManager);

	@Override
	public void refresh() {
		if (null != cloudManager) {
			nodesList.setInput(loadNodes(cloudManager));
		} else {
			nodesList.setInput(null);
		}
		super.refresh();
	}

	@Override
	public boolean setFormInput(final Object input) {
		if (input instanceof ICloudManager) {
			if (null != cloudManager) {
				cloudManager.removeNodeListener(nodeListener);
			}
			cloudManager = (ICloudManager) input;
			cloudManager.addNodeListener(nodeListener);
		} else {
			if (null != cloudManager) {
				cloudManager.removeNodeListener(nodeListener);
			}
			cloudManager = null;
		}
		return super.setFormInput(input);
	}

}
