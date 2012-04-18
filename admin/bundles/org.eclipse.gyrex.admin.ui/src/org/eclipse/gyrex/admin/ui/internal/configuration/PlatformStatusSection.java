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
package org.eclipse.gyrex.admin.ui.internal.configuration;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.pages.OverviewPage;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 */
public class PlatformStatusSection extends SectionPart {

	private static void addStatus(final StringBuilder text, final IStatus status) {
		if (status.isOK()) {
			return;
		}

		if (status.isMultiStatus()) {
			for (final IStatus childStatus : status.getChildren()) {
				addStatus(text, childStatus);
			}
			return;
		}

		text.append("<li style=\"image\" value=\"");
		switch (status.getSeverity()) {
			case IStatus.CANCEL:
			case IStatus.ERROR:
				text.append(ISharedImages.IMG_OBJS_ERROR_TSK);
				break;
			case IStatus.WARNING:
				text.append(ISharedImages.IMG_OBJS_WARN_TSK);
				break;
			case IStatus.INFO:
				text.append(ISharedImages.IMG_OBJS_INFO_TSK);
				break;

			default:
				break;
		}
		text.append("\">").append(status.getMessage()).append("<br/>");
		text.append("(").append(status.getPlugin()).append(", code ").append(status.getCode()).append(")");
		text.append("</li>");
	}

	private ServiceTracker<IStatus, IStatus> statusTracker;
	private FormText statusText;

	private final CopyOnWriteArrayList<IStatus> statusList = new CopyOnWriteArrayList<IStatus>();

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public PlatformStatusSection(final Composite parent, final OverviewPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		createContent(getSection(), page.getManagedForm().getToolkit());
	}

	private void createContent(final Section section, final FormToolkit toolkit) {
//		GridLayoutFactory.fillDefaults().applyTo(section);
//		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.TOP).applyTo(section);

		section.setText("System Status");
		section.setDescription("This section describes the status of the system.");

		final Composite client = toolkit.createComposite(section);
		client.setLayout(new TableWrapLayout());
		section.setClient(client);

		statusText = toolkit.createFormText(client, false);

		statusText.setImage(ISharedImages.IMG_OBJS_ERROR_TSK, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
		statusText.setImage(ISharedImages.IMG_OBJS_WARN_TSK, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
		statusText.setImage(ISharedImages.IMG_OBJS_INFO_TSK, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	@Override
	public void dispose() {
		super.dispose();
		statusTracker.close();
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final BundleContext context = AdminUiActivator.getInstance().getBundle().getBundleContext();
		statusTracker = new ServiceTracker<IStatus, IStatus>(context, IStatus.class, null) {

			@Override
			public IStatus addingService(final ServiceReference<IStatus> reference) {
				final IStatus status = super.addingService(reference);
				if (status != null) {
					statusList.add(status);
					markStale();
				}
				return status;
			}

			@Override
			public void modifiedService(final ServiceReference<IStatus> reference, final IStatus service) {
				markStale();
			}

			@Override
			public void removedService(final ServiceReference<IStatus> reference, final IStatus service) {
				statusList.remove(service);
				markStale();
				super.removedService(reference, service);
			}

		};
		statusTracker.open();
	}

	@Override
	public void refresh() {
		// create multi status
		final MultiStatus systemStatus = new MultiStatus(AdminUiActivator.SYMBOLIC_NAME, 0, "System Status", null);
		for (final IStatus status : statusList) {
			systemStatus.add(status);
		}

		// create status text
		final StringBuilder text = new StringBuilder();
		text.append("<text>");
		if (systemStatus.isOK()) {
			text.append("Your sytem is running fine.");
		} else {
			addStatus(text, systemStatus);
		}
		text.append("</text>");

		// set new text
		statusText.setText(text.toString(), true, true);

		// call super
		super.refresh();
	}
}
