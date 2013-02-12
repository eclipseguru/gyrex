/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
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

import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGateListener;

import org.eclipse.swt.widgets.Display;

public abstract class ZooKeeperBasedAdminPage extends AdminPageWithTree {

	private ZooKeeperGateListener listener;

	public ZooKeeperBasedAdminPage(final int numberOfColumns) {
		super(numberOfColumns);
	}

	@Override
	public void activate() {
		final Display display;
		if (getTreeViewer() != null) {
			display = getTreeViewer().getControl().getDisplay();
		} else {
			display = null;
		}

		if ((listener == null) && (display != null) && !display.isDisposed()) {
			listener = new ZooKeeperGateListener() {

				private void asyncRefresh() {
					if (!display.isDisposed()) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								refresh();
							}
						});
					}
				}

				@Override
				public void gateDown(final ZooKeeperGate gate) {
					asyncRefresh();
				}

				@Override
				public void gateRecovering(final ZooKeeperGate gate) {
					asyncRefresh();
				}

				@Override
				public void gateUp(final ZooKeeperGate gate) {
					asyncRefresh();
				}
			};
			ZooKeeperGate.addConnectionMonitor(listener);
		}

		super.activate();
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if (listener != null) {
			ZooKeeperGate.removeConnectionMonitor(listener);
			listener = null;
		}
	}

}