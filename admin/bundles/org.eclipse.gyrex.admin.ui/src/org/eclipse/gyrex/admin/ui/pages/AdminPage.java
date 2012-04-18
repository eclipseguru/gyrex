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
package org.eclipse.gyrex.admin.ui.pages;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for pages in the Gyrex Admin Console.
 * <p>
 * The Gyrex Admin Console provides a pluggable way for configuring systems.
 * Clients need to provide an implementation of this class in order to
 * participate in the Admin Console.
 * </p>
 * <p>
 * This class must be subclassed by clients that contribute a page to the Gyrex
 * Admin Console. It is considered part of a service provider API. As such it
 * may evolve faster than other APIs.
 * </p>
 */
public abstract class AdminPage extends EventManager {

	/**
	 * The property id for {@link #getTitle()}, {@link #getTitleImage()} and
	 * {@link #getTitleToolTip()}.
	 */
	public static final int PROP_TITLE = IWorkbenchPartConstants.PROP_TITLE;

	/**
	 * The property id for {@link #isDirty()}.
	 */
	public static final int PROP_DIRTY = IWorkbenchPartConstants.PROP_DIRTY;

	private static final Logger LOG = LoggerFactory.getLogger(AdminPage.class);

	private String title;
	private String titleToolTip;
	private Image titleImage;

	/**
	 * Called by the Admin UI whenever a page becomes active.
	 * <p>
	 * Subclass may override and trigger logic that is necessary in order to
	 * activate a page (eg. register listeners with underlying model, etc.).
	 * </p>
	 * <p>
	 * Note, when a page becomes active, its control has been created.
	 * </p>
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
	 * appropriate times). However, implementors must call super at appropriate
	 * times.
	 * </p>
	 */
	public void activate() {
		// empty
	}

	/**
	 * Adds a listener for changes to properties of this configuration page. Has
	 * no effect if an identical listener is already registered.
	 * <p>
	 * The property ids are defined in {@link IWorkbenchPartConstants}.
	 * </p>
	 * 
	 * @param listener
	 *            a property listener
	 */
	public void addPropertyListener(final IPropertyListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Creates the page control.
	 * <p>
	 * Subclasses must override and implement in order to create the page
	 * controls. Note, implementors must not make any assumptions about the
	 * parent control.
	 * </p>
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
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
	 * Called by the Admin UI whenever a page becomes inactive.
	 * <p>
	 * The default implementation does nothing. Subclass may override and
	 * trigger logic that is necessary in order to inactivate a page (eg.
	 * unregister listeners with underlying model, etc.).
	 * </p>
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
	 * appropriate times). However, implementors must call super at appropriate
	 * times.
	 * </p>
	 */
	public void deactivate() {
		// empty
	}

	/**
	 * Fires a property changed event.
	 * 
	 * @param propertyId
	 *            the id of the property that changed
	 */
	protected void firePropertyChange(final int propertyId) {
		final Object[] array = getListeners();
		for (int nX = 0; nX < array.length; nX++) {
			final IPropertyListener l = (IPropertyListener) array[nX];
			try {
				l.propertyChanged(AdminPage.this, propertyId);
			} catch (final RuntimeException e) {
				LOG.error("Error notifying listener {}. {}", new Object[] { l, e.getMessage(), e });
			}
		}
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
	 * Removes the given property listener from this configuration page. Has no
	 * affect if an identical listener is not registered.
	 * 
	 * @param listener
	 *            a property listener
	 */
	public void removePropertyListener(final IPropertyListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Sets the title of this configuration page and fires a
	 * {@value #PROP_TITLE} change event.
	 * 
	 * @param title
	 *            the title to set (maybe <code>null</code>)
	 */
	protected void setTitle(final String title) {
		this.title = title;
		firePropertyChange(PROP_TITLE);
	}

	/**
	 * Sets the title image of this configuration page and fires a
	 * {@value #PROP_TITLE} change even.
	 * 
	 * @param titleImage
	 *            the title image of this configuration page to set (maybe
	 *            <code>null</code>)
	 */
	protected void setTitleImage(final Image titleImage) {
		final Image oldImage = this.titleImage;
		if ((oldImage != null) && oldImage.equals(titleImage)) {
			return;
		}
		this.titleImage = titleImage;
		firePropertyChange(PROP_TITLE);
	}

	/**
	 * Sets the tool tip text of this configuration page and fires a
	 * {@value #PROP_TITLE} change event.
	 * 
	 * @param titleToolTip
	 *            the tool tip text of this configuration page to set (maybe
	 *            <code>null</code>)
	 */
	protected void setTitleToolTip(final String titleToolTip) {
		this.titleToolTip = titleToolTip;
		firePropertyChange(PROP_TITLE);
	}
}
