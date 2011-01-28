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
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.http.jetty.admin.ChannelDescriptor;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 *
 */
public class ChannelsSection extends ViewerWithButtonsSectionPart {

	static class ChannelsLabelProvider extends LabelProvider {
		@Override
		public String getText(final Object element) {
			if (element instanceof ChannelDescriptor) {
				final ChannelDescriptor channel = (ChannelDescriptor) element;
				return String.format("%s (%d)", channel.getId(), channel.getPort());
			}
			return super.getText(element);
		}
	}

	private Button addButton;
	private Button removeButton;
	private ListViewer channelsList;
	private final DataBindingContext bindingContext;
	private IObservableValue selectedChannelValue;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public ChannelsSection(final Composite parent, final JettyConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		bindingContext = page.getBindingContext();
		final Section section = getSection();
		section.setText("Connectors");
		section.setDescription("Configure the HTTP connectors that should be available for handling requests.");
		createContent(section);
	}

	void addButtonPressed() {
		final AddChannelDialog dialog = new AddChannelDialog(SwtUtil.getShell(addButton), getJettyManager());
		if (dialog.open() == Window.OK) {
			markStale();
		}
	}

	@Override
	public void commit(final boolean onSave) {
		super.commit(onSave);
	}

	/**
	 * Called by {@link #createButtonPanel(FormToolkit, Composite)} in order to
	 * create the buttons.
	 * 
	 * @param toolkit
	 * @param buttonsPanel
	 */
	@Override
	protected void createButtons(final Composite buttonsPanel) {
		addButton = createButton(buttonsPanel, "Add...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addButtonPressed();
			}
		});
		removeButton = createButton(buttonsPanel, "Remove...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});
	}

	/**
	 * Creates the main viewer.
	 * 
	 * @param toolkit
	 * @param client
	 */
	@Override
	protected void createViewer(final Composite client) {
		channelsList = new ListViewer(client, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = channelsList.getList();
		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		channelsList.setContentProvider(new ArrayContentProvider());
		channelsList.setLabelProvider(new ChannelsLabelProvider());

		selectedChannelValue = ViewersObservables.observeSingleSelection(channelsList);
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	private IJettyManager getJettyManager() {
		return (IJettyManager) getManagedForm().getInput();
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(channelsList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
	}

	@Override
	public void refresh() {
		final Object input = getManagedForm().getInput();
		if (input instanceof IJettyManager) {
			final IJettyManager jettyManager = (IJettyManager) input;
			final Collection<ChannelDescriptor> channels = jettyManager.getChannels();
			channelsList.setInput(channels);
		}
		super.refresh();
	}

	void removeButtonPressed() {
		final ChannelDescriptor channel = (ChannelDescriptor) (null != selectedChannelValue ? selectedChannelValue.getValue() : null);
		if (channel == null) {
			return;
		}

		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Channel", "Do you really want to delete the channel?")) {
			return;
		}

		getJettyManager().removeChannel(channel.getId());
		markStale();
	}

	@Override
	public boolean setFormInput(final Object input) {
		if (input instanceof IJettyManager) {
			markStale();
			return true;
		}
		return super.setFormInput(input);
	}
}
