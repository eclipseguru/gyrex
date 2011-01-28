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
package org.eclipse.gyrex.admin.ui.internal.forms;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A section part with a viewer on the left and buttons on the right side (eg.
 * Add/Remove buttons).
 * <p>
 * Clients must call {@link #createContent(Section, FormToolkit)} at appropriate
 * times.
 * </p>
 */
public abstract class ViewerWithButtonsSectionPart extends SectionPart {

	private final FormToolkit toolkit;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public ViewerWithButtonsSectionPart(final Composite parent, final FormToolkit toolkit, final int style) {
		super(parent, toolkit, style);
		this.toolkit = toolkit;
	}

	/**
	 * Convenience method which creates a button.
	 * 
	 * @param toolkit
	 * @param buttonsPanel
	 *            the buttons panel (created in
	 *            {@link #createButtonPanel(FormToolkit, Composite)})
	 * @param label
	 *            a label text
	 * @param selectionListener
	 *            a selection listener
	 * @return the created button
	 */
	protected Button createButton(final Composite buttonsPanel, final String label, final SelectionListener selectionListener) {
		final Button button = getToolkit().createButton(buttonsPanel, label, SWT.PUSH);
		GridDataFactory.defaultsFor(button).applyTo(button);
		if (selectionListener != null) {
			button.addSelectionListener(selectionListener);
		}
		return button;
	}

	protected void createButtonPanel(final Composite parent) {
		final Composite buttonsPanel = getToolkit().createComposite(parent);
		buttonsPanel.setLayoutData(GridDataFactory.fillDefaults().create());
		buttonsPanel.setLayout(GridLayoutFactory.fillDefaults().create());

		createButtons(buttonsPanel);
	}

	protected abstract void createButtons(final Composite parent);

	protected void createContent(final Section section) {
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));

		final Composite client = getToolkit().createComposite(section);
		client.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		section.setClient(client);

		createViewer(client);

		createButtonPanel(client);
	}

	protected abstract void createViewer(final Composite parent);

	/**
	 * Returns the toolkit.
	 * 
	 * @return the toolkit
	 */
	protected FormToolkit getToolkit() {
		return toolkit;
	}

}