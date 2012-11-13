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
package org.eclipse.gyrex.admin.ui.internal.pages.overview;

import java.lang.reflect.Method;
import java.net.InetAddress;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.pages.OverviewPageItem;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.boot.internal.app.ServerApplication;

import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.apache.commons.lang.exception.ExceptionUtils;

public class NodeShortcuts extends OverviewPageItem {

	@Override
	public Control createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, true));
		composite.setLayoutData(AdminUiUtil.createHorzFillData());

		AdminUiUtil.createHeading(composite, "Shortcuts", 1);

		final Label desc = new Label(composite, SWT.WRAP);
		desc.setText("Some convenience shortcuts for the system.");
		desc.setLayoutData(AdminUiUtil.createHorzFillData());

		final Link restartLink = new Link(composite, SWT.NONE);
		restartLink.setText("<a>Restart local node</a>");
		restartLink.setLayoutData(AdminUiUtil.createHorzFillData());
		restartLink.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				NonBlockingMessageDialogs.openConfirm(SwtUtil.getShell(restartLink), "Restart Node", String.format("Node %s will be restarted. Please confirm!", getNodeId()), new DialogCallback() {
					/** serialVersionUID */
					private static final long serialVersionUID = 1L;

					@Override
					public void dialogClosed(final int returnCode) {
						if (returnCode == Window.OK) {
							ServerApplication.restart();
						}
					}
				});
			}
		});

		return composite;
	}

	String getNodeId() {
		try {
			// we use reflection to not depend on the cloud API at all.
			final BundleContext bundleContext = AdminUiActivator.getInstance().getBundle().getBundleContext();
			final ServiceReference<?> serviceReference = bundleContext.getServiceReference("org.eclipse.gyrex.cloud.environment.INodeEnvironment");
			final Object service = bundleContext.getService(serviceReference);
			if (service != null) {
				try {
					final Method method = service.getClass().getMethod("getNodeId");
					return (String) method.invoke(service);
				} finally {
					bundleContext.ungetService(serviceReference);
				}
			}
			return String.format("running on host %s", InetAddress.getLocalHost().getHostName());
		} catch (final Exception e) {
			return String.format("(%s)", ExceptionUtils.getRootCauseMessage(e));
		}
	}

}
