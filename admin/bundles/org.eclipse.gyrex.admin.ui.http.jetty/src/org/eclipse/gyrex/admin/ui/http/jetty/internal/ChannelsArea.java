/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.http.jetty.admin.ChannelDescriptor;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * 
 */
public class ChannelsArea {

	static class ChannelsLabelProvider extends LabelProvider {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(final Object element) {
			if (element instanceof ChannelDescriptor) {
				final ChannelDescriptor channel = (ChannelDescriptor) element;
				return String.format("%s (%d)", channel.getId(), channel.getPort());
			}
			return super.getText(element);
		}
	}

	private ISelectionChangedListener updateButtonsListener;
	private Button addButton;
	private Button removeButton;
	private ListViewer channelsList;
	private Composite pageComposite;

	public void activate() {

		if (channelsList != null) {
			channelsList.setInput(getJettyManager());
			updateButtonsListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			channelsList.addSelectionChangedListener(updateButtonsListener);
		} else {
		}

	}

	void addButtonPressed() {
		final AddChannelDialog dialog = new AddChannelDialog(SwtUtil.getShell(addButton), getJettyManager());
		dialog.openNonBlocking(new DialogCallback() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});

	}

	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button b = new Button(buttons, SWT.NONE);
		b.setText(buttonLabel);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return b;
	}

	public void createChannelsControls(final Composite parent) {
		pageComposite = parent;

		final Infobox infobox = new Infobox(pageComposite);
		infobox.setLayoutData(AdminUiUtil.createHorzFillData());
		infobox.addHeading("Jetty connectors in Gyrex.");
		infobox.addParagraph("Connectors are the ports Jetty listens on. When creating a connector, the port number and a name for the channel is required. Optional to that you can define the encryption settings, if the connector should support SSL.");

		final Composite description = new Composite(pageComposite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		channelsList = new ListViewer(description, SWT.SINGLE | SWT.BORDER);
		final List list = channelsList.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		channelsList.setContentProvider(new ArrayContentProvider());
		channelsList.setLabelProvider(new ChannelsLabelProvider());
		channelsList.setContentProvider(new ChannelsContentProvider());

		final Composite buttons = new Composite(description, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());

		addButton = createButton(buttons, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		removeButton = createButton(buttons, "Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

	}

	public void deactivate() {

		// remove data inputs form controls
		if (channelsList != null) {
			if (updateButtonsListener != null) {
				channelsList.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (!channelsList.getList().isDisposed()) {
				channelsList.setInput(null);
			}
		}

	}

	private IJettyManager getJettyManager() {
		return JettyConfigActivator.getInstance().getJettyManager();
	}

	private ChannelDescriptor getSelectedChannel() {
		final IStructuredSelection selection = (IStructuredSelection) channelsList.getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof ChannelDescriptor))
			return (ChannelDescriptor) selection.getFirstElement();

		return null;
	}

	public void refresh() {
		channelsList.refresh();
		updateButtons();
	}

	void removeButtonPressed() {

		final ChannelDescriptor channel = getSelectedChannel();
		if (channel == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(pageComposite), "Remove selected Channel", String.format("Do you really want to delete channel %s?", channel.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				getJettyManager().removeChannel(channel.getId());
				refresh();
			}
		});
	}

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) channelsList.getSelection()).size();
		if (selectedElementsCount == 0) {
			addButton.setEnabled(true);
			removeButton.setEnabled(false);
			return;
		}

		addButton.setEnabled(selectedElementsCount == 1);
		removeButton.setEnabled(selectedElementsCount == 1);
	}

}
