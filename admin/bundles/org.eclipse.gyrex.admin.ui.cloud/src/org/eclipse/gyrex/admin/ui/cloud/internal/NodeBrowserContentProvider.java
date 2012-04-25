/*******************************************************************************
 * Copyright (c) 2012 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal;

import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeListener;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

public final class NodeBrowserContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private final INodeListener nodeListener = new INodeListener() {
		@Override
		public void nodesChanged() {
			final Viewer viewer = NodeBrowserContentProvider.this.viewer;
			final Control control = viewer.getControl();
			if ((null != control) && !control.isDisposed()) {
				control.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!control.isDisposed()) {
							viewer.refresh();
						}
					}
				});
			}
		}
	};

	Viewer viewer;

	private void attach(final Object input) {
		if (input instanceof ICloudManager) {
			((ICloudManager) input).addNodeListener(nodeListener);
		}
	}

	private void detach(final Object input) {
		if (input instanceof ICloudManager) {
			((ICloudManager) input).removeNodeListener(nodeListener);
		}
	}

	@Override
	public void dispose() {
		if (null != viewer) {
			detach(viewer.getInput());
			viewer = null;
		}
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		return EMPTY_ARRAY;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ICloudManager) {

		}
		return EMPTY_ARRAY;
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		if ((null != this.viewer) && (this.viewer != viewer)) {
			throw new IllegalStateException("please use separate provider instance for different viewers");
		}
		this.viewer = viewer;
		detach(oldInput);
		attach(newInput);
	}
}