/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.rap.application;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Base class for pages in Gyrex RAP web applications.
 * <p>
 * This class must be subclassed by clients that contribute a page to a Gyrex
 * RAP based web application. It is considered part of a service provider API.
 * As such it may evolve faster than other APIs.
 * </p>
 */
public abstract class Page {

	private String title;
	private String titleToolTip;
	private Image titleImage;
	private String[] arguments;
	private IApplicationService applicationService;

	/**
	 * Called by the application whenever a page becomes active.
	 * <p>
	 * Subclass may override and trigger logic that is necessary in order to
	 * activate a page (eg. register listeners with underlying model, etc.).
	 * </p>
	 * <p>
	 * Note, when a page becomes active, its control has been created.
	 * </p>
	 * <p>
	 * Clients should not call this method (the application calls this method at
	 * appropriate times). However, implementors must call super as part of
	 * their implementation.
	 * </p>
	 */
	public void activate() {
		// empty
	}

	/**
	 * Creates the page control.
	 * <p>
	 * Subclasses must override and implement in order to create the page
	 * controls. Note, implementors must not make any assumptions about the
	 * parent control and/or the control created by the default implementation.
	 * </p>
	 * <p>
	 * Clients should not call this method (the application calls this method at
	 * appropriate times).
	 * </p>
	 *
	 * @param parent
	 *            the parent composite
	 * @return the created control
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public Control createControl(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("empty");
		return label;
	}

	/**
	 * Called by the application whenever a page becomes inactive.
	 * <p>
	 * The default implementation does nothing. Subclass may override and
	 * trigger logic that is necessary in order to inactivate a page (eg.
	 * unregister listeners with underlying model, etc.).
	 * </p>
	 * <p>
	 * Clients should not call this method (the application calls this method at
	 * appropriate times). However, implementors must call super at appropriate
	 * times.
	 * </p>
	 */
	public void deactivate() {
		// empty
	}

	/**
	 * Returns the {@link IApplicationService application service}.
	 *
	 * @return the {@link IApplicationService application service}
	 */
	public final IApplicationService getApplicationService() {
		if (applicationService == null)
			throw new IllegalStateException("not initialized");
		return applicationService;
	}

	/**
	 * Returns the page arguments.
	 *
	 * @return the arguments
	 */
	public String[] getArguments() {
		return arguments;
	}

	/**
	 * Returns the title of this configuration page. If this value changes the
	 * page must fire a property listener event with {@link #PROP_TITLE}.
	 * <p>
	 * The title is used to populate the title bar of this page's visual
	 * container.
	 * </p>
	 *
	 * @return the configuration page title (not <code>null</code>)
	 */
	public String getTitle() {
		return title != null ? title : "";
	}

	/**
	 * Returns the title image of this configuration page. If this value changes
	 * the page must fire a property listener event with {@link #PROP_TITLE}.
	 * <p>
	 * The title image is usually used to populate the title bar of this page's
	 * visual container.
	 * </p>
	 *
	 * @return the title image
	 */
	public Image getTitleImage() {
		return titleImage;
	}

	/**
	 * Returns the title tool tip text of this configuration page. An empty
	 * string result indicates no tool tip. If this value changes the page must
	 * fire a property listener event with {@link #PROP_TITLE}.
	 * <p>
	 * The tool tip text is used to populate the title bar of this page's visual
	 * container.
	 * </p>
	 *
	 * @return the configuration page title tool tip (not <code>null</code>)
	 */
	public String getTitleToolTip() {
		return titleToolTip != null ? titleToolTip : "";
	}

	/**
	 * Sets the application service.
	 *
	 * @param ui
	 *            the application service to set
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void setApplicationService(final IApplicationService ui) {
		applicationService = ui;
	}

	/**
	 * Sets the page arguments.
	 * <p>
	 * If any, arguments were received together with the request to open/show
	 * the page (eg., within the URL). They may provide further hints for
	 * pre-filling the page with data. The first element in the argument is the
	 * id which triggered this page.
	 * </p>
	 * <p>
	 * Note, when this method is invoked by the framework, no control might have
	 * been created yet.
	 * </p>
	 *
	 * @param args
	 *            the arguments (never <code>null</code>)
	 */
	public void setArguments(final String[] args) {
		arguments = args;
	}

	/**
	 * Sets the title of this page.
	 *
	 * @param title
	 *            the title to set (maybe <code>null</code>)
	 */
	protected void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Sets the title image of this page.
	 *
	 * @param titleImage
	 *            the title image of this configuration page to set (maybe
	 *            <code>null</code>)
	 */
	protected void setTitleImage(final Image titleImage) {
		final Image oldImage = this.titleImage;
		if ((oldImage != null) && oldImage.equals(titleImage))
			return;
		this.titleImage = titleImage;
	}

	/**
	 * Sets the tool tip text of this page.
	 *
	 * @param titleToolTip
	 *            the tool tip text of this configuration page to set (maybe
	 *            <code>null</code>)
	 */
	protected void setTitleToolTip(final String titleToolTip) {
		this.titleToolTip = titleToolTip;
	}
}
