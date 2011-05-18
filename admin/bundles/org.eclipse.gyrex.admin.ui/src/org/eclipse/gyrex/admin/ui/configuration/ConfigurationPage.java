/*******************************************************************************
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.configuration;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for configuration pages in the Gyrex Admin UI.
 * <p>
 * The Gyrex Admin UI provides a pluggable way for configuring systems. Clients
 * simply need to provide an implementation of this class in order to
 * participate in the configuration perspective in the Admin UI. The concept is
 * similar to Eclipse preferences but not tight to just preferences.
 * </p>
 * <p>
 * This class must be subclassed by clients that contribute a configuration page
 * to Gyrex.
 * </p>
 */
public abstract class ConfigurationPage extends EventManager {

	private static class PageForm extends ManagedForm {
		public PageForm(final ConfigurationPage page, final ScrolledForm form, final FormToolkit toolkit) {
			super(toolkit, form);
			setContainer(page);
		}

		@Override
		public void dirtyStateChanged() {
			getPage().firePropertyChange(PROP_DIRTY);
		}

		public ConfigurationPage getPage() {
			return (ConfigurationPage) getContainer();
		}

		@Override
		public void staleStateChanged() {
			if (getForm().isDisposed()) {
				return;
			}
			if (getPage().isActive()) {
				refresh();
			}
		}
	}

	/**
	 * The property id for {@link #getTitle()}, {@link #getTitleImage()} and
	 * {@link #getTitleToolTip()}.
	 */
	public static final int PROP_TITLE = IWorkbenchPartConstants.PROP_TITLE;

	/**
	 * The property id for {@link #isDirty()}.
	 */
	public static final int PROP_DIRTY = IWorkbenchPartConstants.PROP_DIRTY;

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationPage.class);

	private IConfigurationPageContainer container;
	private PageForm mform;
	private DataBindingContext bindingContext;

	private String title;
	private String titleToolTip;
	private Image titleImage;

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
	 * Subclasses should override this method to create content in the form
	 * hosted in this page.
	 * <p>
	 * Implementors should not set a title or image on the form. Pages are
	 * hosted in a container with a shared, stable header.
	 * </p>
	 * <p>
	 * The method {@link IManagedForm#getForm()} returns the form whose
	 * {@link Form#getBody() body} should be used as a parent composite. Note
	 * that the form's body does not have a layout manager set. Implementors are
	 * responsible for setting one.
	 * </p>
	 * 
	 * @param managedForm
	 *            the form hosted in this page.
	 */
	protected void createFormContent(final IManagedForm managedForm) {
		// empty
	}

	/**
	 * Creates the page control.
	 * <p>
	 * Calls the method
	 * {@link ConfigurationPage#createFormContent(IManagedForm)} to fill the
	 * page with specific content.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @param toolkit
	 *            a reusable {@link FormToolkit} that must <strong>not</strong>
	 *            be disposed by this page
	 */
	public void createPage(final Composite parent, final FormToolkit toolkit) {
		final ScrolledForm form = toolkit.createScrolledForm(parent);
		mform = new PageForm(this, form, toolkit);
		BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
			public void run() {
				createFormContent(mform);
			}
		});
	}

	/**
	 * Disposes of this page.
	 * <p>
	 * This is the last method called on the {@link ConfigurationPage}. At this
	 * point the page controls (if they were ever created) have been disposed as
	 * part of an SWT composite. There is no guarantee that
	 * {@link #createPageForm(IManagedForm)} has been called, so the page
	 * controls may never have been created.
	 * </p>
	 * <p>
	 * Within this method a page may release any resources, fonts, images,
	 * etc.&nbsp; held by this page. It is also very important to deregister all
	 * listeners from the platform.
	 * </p>
	 * <p>
	 * The default implementation disposes the manager form and removes all
	 * listeners.
	 * </p>
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
	 * appropriate times).
	 * </p>
	 */
	public void dispose() {
		if (mform != null) {
			mform.dispose();
		}
		if (bindingContext != null) {
			// dispose but don't set to null
			// (otherwise the getter might create a new one)
			bindingContext.dispose();
		}
		clearListeners();
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
				l.propertyChanged(ConfigurationPage.this, propertyId);
			} catch (final RuntimeException e) {
				LOG.error("Error notifying listener {}. {}", new Object[] { l, e.getMessage(), e });
			}
		}
	}

	/**
	 * Returns the {@link DataBindingContext} for the page.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		if (null == bindingContext) {
			final Realm realm = SWTObservables.getRealm(PlatformUI.getWorkbench().getDisplay());
			bindingContext = new DataBindingContext(realm);
		}
		return bindingContext;
	}

	/**
	 * Returns the container which hosts this page.
	 * 
	 * @return the {@link IConfigurationPageContainer}
	 */
	public IConfigurationPageContainer getContainer() {
		return container;
	}

	/**
	 * Returns the page control.
	 * 
	 * @return managed form's control
	 */
	public Control getControl() {
		return mform != null ? mform.getForm() : null;
	}

	/**
	 * Returns the managed form owned by this page.
	 * 
	 * @return the managed form
	 */
	public IManagedForm getManagedForm() {
		return mform;
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
	 * Tests if the page is active by asking the parent container if this page
	 * is the currently active page.
	 * 
	 * @return <code>true</code> if the page is currently active,
	 *         <code>false</code> otherwise.
	 */
	public boolean isActive() {
		return equals(container.getActivePageInstance());
	}

	/**
	 * Indicates whether the page contains unapplied changes.
	 * <p>
	 * The default implementation delegates to the managed form.
	 * </p>
	 * 
	 * @return true when unapplied changes occur on the page
	 */
	public boolean isDirty() {
		if (mform != null) {
			return mform.isDirty();
		}
		return false;
	}

	/**
	 * Notifies that the save button of this page's container has been pressed.
	 * <p>
	 * The default implementation calls commit on the managed form and returns
	 * {@link Status#OK_STATUS}.
	 * </p>
	 * 
	 * @param monitor
	 *            a monitor for reporting progress
	 * @return a status indicating the result of the save operation
	 */
	public IStatus performSave(final IProgressMonitor monitor) {
		if (mform != null) {
			mform.commit(true);
			firePropertyChange(PROP_DIRTY);
		}
		return Status.OK_STATUS;
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
	 * Notifies this page that is has been activated within the Admin UI.
	 * <p>
	 * The default implementation refreshes the managed form if it is stale and
	 * the page became active.
	 * </p>
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
	 * appropriate times).
	 * </p>
	 * 
	 * @param active
	 *            <code>true</code> if the page has been activate,
	 *            <code>false</code> otherwise
	 */
	public void setActive(final boolean active) {
		if (active) {
			if ((mform != null) && mform.isStale()) {
				mform.refresh();
			}
		}
	}

	/**
	 * Sets the container which hosts the page.
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
	 * appropriate times).
	 * </p>
	 * 
	 * @param container
	 *            the container
	 */
	public void setContainer(final IConfigurationPageContainer container) {
		this.container = container;
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
