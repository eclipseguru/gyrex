/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.gyrex.http.internal.HttpActivator;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public final class ApplicationBrowserContentProvider implements ITreeContentProvider {

	public class AppRegItem {

		private final String applicationId;
		private final String providerId;
		private final String providerLabel;
		private final String contextPath;
		private final String activationStatus;
		private final String mounts;
		private final ApplicationRegistration applicationRegistration;
		private final ApplicationProviderRegistration applicationProviderRegistration;
		private GroupingItem parent;

		/**
		 * Creates a new instance.
		 * 
		 * @param applicationId
		 * @param providerId
		 * @param providerDesc
		 * @param contextPath
		 * @param activationStatus
		 * @param mounts
		 * @param applicationRegistration
		 * @param applicationProviderRegistration
		 */
		public AppRegItem(final String applicationId, final String providerId, final String contextPath, final String activationStatus, final String mounts, final ApplicationRegistration applicationRegistration, final ApplicationProviderRegistration applicationProviderRegistration) {
			super();
			this.applicationId = applicationId;
			this.providerId = providerId;
			this.contextPath = contextPath;
			this.activationStatus = activationStatus;
			this.mounts = mounts;
			this.applicationRegistration = applicationRegistration;
			this.applicationProviderRegistration = applicationProviderRegistration;
			providerLabel = HttpUiAdapter.getLabel(applicationProviderRegistration);
		}

		/**
		 * Returns the activationStatus.
		 * 
		 * @return the activationStatus
		 */
		public String getActivationStatus() {
			return activationStatus;
		}

		/**
		 * Returns the applicationId.
		 * 
		 * @return the applicationId
		 */
		public String getApplicationId() {
			return applicationId;
		}

		/**
		 * Returns the applicationProviderRegistration.
		 * 
		 * @return the applicationProviderRegistration
		 */
		public ApplicationProviderRegistration getApplicationProviderRegistration() {
			return applicationProviderRegistration;
		}

		/**
		 * Returns the applicationRegistration.
		 * 
		 * @return the applicationRegistration
		 */
		public ApplicationRegistration getApplicationRegistration() {
			return applicationRegistration;
		}

		/**
		 * Returns the contextPath.
		 * 
		 * @return the contextPath
		 */
		public String getContextPath() {
			return contextPath;
		}

		/**
		 * Returns the mounts.
		 * 
		 * @return the mounts
		 */
		public String getMounts() {
			return mounts;
		}

		/**
		 * Returns the parent.
		 * 
		 * @return the parent
		 */
		public GroupingItem getParent() {
			return parent;
		}

		/**
		 * Returns the providerId.
		 * 
		 * @return the providerId
		 */
		public String getProviderId() {
			return providerId;
		}

		/**
		 * Returns the providerLabel.
		 * 
		 * @return the providerLabel
		 */
		public String getProviderLabel() {
			return providerLabel;
		}

		/**
		 * Sets the parent.
		 * 
		 * @param parent
		 *            the parent to set
		 */
		public void setParent(final GroupingItem parent) {
			this.parent = parent;
		}

	}

	public class GroupingItem {

		private final String value;
		private final List<AppRegItem> appRegItemChildren = new ArrayList<AppRegItem>();

		/**
		 * Creates a new instance.
		 * 
		 * @param value
		 */
		public GroupingItem(final String value) {
			super();
			this.value = value;
		}

		public void addAppregItem(final AppRegItem item) {
			appRegItemChildren.add(item);
		}

		/**
		 * Returns the appRegItemChildren.
		 * 
		 * @return the appRegItemChildren
		 */
		public List<AppRegItem> getAppRegItemChildren() {
			return appRegItemChildren;
		}

		/**
		 * Returns the value.
		 * 
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

	}

	private static final Object[] EMPTY_ARRAY = new Object[0];

	/* what is this one for
	private final INodeListener nodeListener = new INodeListener() {
		@Override
		public void nodesChanged() {
			final Viewer viewer = ApplicationBrowserContentProvider.this.viewer;
			final Control control = viewer.getControl();
			if ((null != control) && !control.isDisposed()) {
				control.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!control.isDisposed()) {
							viewer.refresh();
						}
					}
				});
			}
		}
	};
	*/

	Viewer viewer;

	private void attach(final Object input) {
		// doNothing for now
	}

	private void detach(final Object input) {
		// doNothing for now
	}

	@Override
	public void dispose() {
		if (null != viewer) {
			detach(viewer.getInput());
			viewer = null;
		}
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof GroupingItem) {
			return ((GroupingItem) parentElement).getAppRegItemChildren().toArray();
		}
		return EMPTY_ARRAY;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ApplicationManager) {
			final ApplicationManager appManager = (ApplicationManager) inputElement;
			final java.util.Hashtable<String, Object> treeItems = new Hashtable<String, Object>();

			try {
				final Collection<String> registeredApplications = new TreeSet<String>(appManager.getRegisteredApplications());
				for (final String appId : registeredApplications) {
					final ApplicationRegistration applicationRegistration = appManager.getApplicationRegistration(appId);

					GroupingItem treeGroup = (GroupingItem) treeItems.get(applicationRegistration.getContext().getContextPath().toString());
					if (treeGroup == null) {
						treeGroup = new GroupingItem(applicationRegistration.getContext().getContextPath().toString());
						treeItems.put(treeGroup.getValue(), treeGroup);
					}

					String activationStatus = "INACTIVE";
					if (appManager.isActive(appId)) {
						activationStatus = "ACTIVE";
					}

					final ApplicationRegistrationPropertySource appRegProps = new ApplicationRegistrationPropertySource(applicationRegistration);
					final String mounts = ((ApplicationMountsPropertySource) appRegProps.getPropertyValue("urls")).getEditableValue().toString();

					final ApplicationProviderRegistration applicationProviderRegistration = HttpActivator.getInstance().getProviderRegistry().getRegisteredProviders().get(applicationRegistration.getProviderId());

					final AppRegItem item = new AppRegItem(applicationRegistration.getApplicationId(), applicationRegistration.getProviderId(), applicationRegistration.getContext().getContextPath().toString(), activationStatus, mounts, applicationRegistration, applicationProviderRegistration);
					treeGroup.addAppregItem(item);

				}
			} catch (final BackingStoreException e) {
				e.printStackTrace();
			}

			return treeItems.values().toArray();
		}
		return EMPTY_ARRAY;
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof AppRegItem) {
			return ((AppRegItem) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof GroupingItem) {
			return true;
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		if ((null != this.viewer) && (this.viewer != viewer)) {
			throw new IllegalStateException("please use separate provider instance for different viewers");
		}
		this.viewer = viewer;
		detach(oldInput);
		attach(newInput);
	}
}