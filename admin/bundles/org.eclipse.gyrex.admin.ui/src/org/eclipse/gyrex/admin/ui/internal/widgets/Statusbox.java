/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class Statusbox extends Composite {

	public static enum Status {
		Error, Warning
	}

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final Composite contentComp;
	private final Status status;

	public Statusbox(final Composite parent, final Status status) {
		super(parent, SWT.NONE);
		this.status = status;
		setLayout(AdminUiUtil.createGridLayout(1, false, true, false));
		setLayoutData(AdminUiUtil.createFillData());
		contentComp = createInfoboxContentComposite();
	}

	public void addHeading(final String text) {
		final Label label = new Label(contentComp, SWT.NONE);
		label.setText(text.replace("&", "&&"));
		label.setData(RWT.CUSTOM_VARIANT, getStatusVariant() + "heading");
	}

	public void addLink(final String text, final SelectionListener selectionListener) {
		final Link link = new Link(contentComp, SWT.WRAP);
		link.setData(RWT.CUSTOM_VARIANT, getStatusVariant());
		link.setText(text);
		link.setLayoutData(AdminUiUtil.createFillData());
		link.addSelectionListener(selectionListener);
	}

	public void addParagraph(final String text) {
		final Label label = new Label(contentComp, SWT.WRAP);
		label.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		label.setData(RWT.CUSTOM_VARIANT, getStatusVariant());
		label.setText(text);
		label.setLayoutData(AdminUiUtil.createFillData());
	}

	private Composite createInfoboxContentComposite() {
		final Composite contentComp = new Composite(this, SWT.NONE);
		contentComp.setBackgroundMode(SWT.INHERIT_FORCE);
		contentComp.setData(RWT.CUSTOM_VARIANT, getStatusVariant());
		final GridLayout layout = AdminUiUtil.createGridLayoutWithoutMargin(1, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		contentComp.setLayout(layout);
		contentComp.setLayoutData(AdminUiUtil.createHorzFillData());
		return contentComp;
	}

	/**
	 * Returns the status.
	 * 
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	private String getStatusVariant() {
		switch (getStatus()) {
			case Warning:
				return "statusbox-warning";

			case Error:
			default:
				return "statusbox-error";
		}
	}

}
