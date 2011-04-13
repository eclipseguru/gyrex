/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
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

import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.boot.internal.app.ServerApplication;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 *
 */
@SuppressWarnings("restriction")
public class NodeShortcutsSection extends SectionPart {

	public NodeShortcutsSection(final Composite parent, final GeneralPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		createContent(getSection(), page.getManagedForm().getToolkit());
	}

	private void createContent(final Section section, final FormToolkit toolkit) {
		section.setLayout(FormLayoutFactory.createClearTableWrapLayout(false, 1));

		section.setText("Shortcuts");
		section.setDescription("Some convenience shortcuts for the system.");

		final Composite client = toolkit.createComposite(section);
		client.setLayout(new TableWrapLayout());
		section.setClient(client);

//		final Hyperlink shutdownLink = toolkit.createHyperlink(client, "Shutdown local node", SWT.NULL);
//		shutdownLink.addHyperlinkListener(new HyperlinkAdapter() {
//			@Override
//			public void linkActivated(final HyperlinkEvent e) {
//				ServerApplication.signalShutdown(null);
//			}
//		});

		final Hyperlink restartLink = toolkit.createHyperlink(client, "Restart local node", SWT.NULL);
		restartLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e) {
				if (MessageDialog.openConfirm(SwtUtil.getShell(restartLink), "Restart Node", "The node will be restarted. Please confirm!")) {
					ServerApplication.restart();
				}
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);
	}

	@Override
	public void refresh() {
		super.refresh();
	}
}
