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
package org.eclipse.gyrex.admin.ui.internal.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.common.internal.services.IServiceProxyChangeListener;
import org.eclipse.gyrex.common.internal.services.ServiceProxy;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * System overview.
 */
@SuppressWarnings("restriction")
public class OverviewPage extends AdminPage {

	private Composite overviewPageComposite;
	private IServiceProxy<OverviewPageItem> itemServiceProxy;
	private List<Control> itemControls;
	private IServiceProxyChangeListener layoutOnServiceChange;

	public OverviewPage() {
		setTitle("Gyrex System Overview");
		setTitleToolTip("Welcome to Gyrex");
	}

	@Override
	public void activate() {
		// call super
		super.activate();

		if (null == itemServiceProxy) {
			itemServiceProxy = AdminUiActivator.getInstance().getServiceHelper().trackService(OverviewPageItem.class);
		}

		// create items
		final List<OverviewPageItem> items = itemServiceProxy.getServices();
		itemControls = new ArrayList<Control>(items.size());
		for (final OverviewPageItem item : items) {
			itemControls.add(item.createControl(overviewPageComposite));
		}

		// re-layout on service changes (items must dispose themselves if the leave)
		final Display display = overviewPageComposite.getDisplay();
		layoutOnServiceChange = new IServiceProxyChangeListener() {
			@Override
			public boolean serviceChanged(final IServiceProxy<?> proxy) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						doLayout();
					}
				});
				return false;
			}
		};
		((ServiceProxy<OverviewPageItem>) itemServiceProxy).addChangeListener(layoutOnServiceChange);

		doLayout();
	}

	@Override
	public Control createControl(final Composite parent) {
		overviewPageComposite = new Composite(parent, SWT.NONE);
		overviewPageComposite.setLayout(AdminUiUtil.createMainLayout(3));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(overviewPageComposite);
			final GridData gd = AdminUiUtil.createHorzFillData();
			gd.horizontalSpan = 3;
			infobox.setLayoutData(gd);
			infobox.addHeading("Welcome to Gyrex.");
			infobox.addParagraph("Your system is running and ready for administration. If you like what you see here, please follow our <a href=\"http://www.eclipse.org/gyrex/\">blog</a>.");
			infobox.addParagraph("This box only appears in development mode. A Gyrex node can opperate in different modes which influence the default configuration of the system. Development mode is the default mode and provides an environment that reduces the amount of setup work for developers. For example, a service such as Apache ZooKeeper which typically runs on different nodes in a production system, is started automatically within the Gyrex Java VM in development mode.");
		}

		return overviewPageComposite;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (null != itemServiceProxy && null != layoutOnServiceChange) {
			((ServiceProxy<OverviewPageItem>) itemServiceProxy).removeChangeListener(layoutOnServiceChange);
			layoutOnServiceChange = null;
		}
	}

	void doLayout() {
		if (!overviewPageComposite.isDisposed()) {
			overviewPageComposite.layout();
		}
	}
}
