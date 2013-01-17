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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.gyrex.http.internal.HttpActivator;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("restriction")
public final class ApplicationBrowserContentProvider implements ITreeContentProvider {

	public static class ApplicationItem {

		private final Set<String> mounts;
		private final ApplicationRegistration applicationRegistration;
		private final ApplicationProviderRegistration applicationProviderRegistration;
		private GroupNode parent;
		private final boolean active;

		public ApplicationItem(final ApplicationRegistration applicationRegistration, final ApplicationProviderRegistration applicationProviderRegistration, final boolean active, final Set<String> mounts) {
			this.applicationRegistration = applicationRegistration;
			this.applicationProviderRegistration = applicationProviderRegistration;
			this.active = active;
			this.mounts = mounts;
		}

		public String getApplicationId() {
			return applicationRegistration.getApplicationId();
		}

		public ApplicationProviderRegistration getApplicationProviderRegistration() {
			return applicationProviderRegistration;
		}

		public ApplicationRegistration getApplicationRegistration() {
			return applicationRegistration;
		}

		public String getContextPath() {
			return applicationRegistration.getContext().getContextPath().toString();
		}

		public Set<String> getMounts() {
			return mounts;
		}

		public GroupNode getParent() {
			return parent;
		}

		public String getProviderId() {
			return applicationRegistration.getProviderId();
		}

		public String getProviderLabel() {
			final String providerInfo = applicationProviderRegistration.getProviderInfo();
			if (StringUtils.isNotBlank(providerInfo))
				return providerInfo;
			return applicationRegistration.getProviderId();
		}

		public boolean isActive() {
			return active;
		}

		public void setParent(final GroupNode parent) {
			this.parent = parent;
		}
	}

	public static class GroupNode {

		private final Object value;
		private final List<ApplicationItem> children = new ArrayList<ApplicationItem>();

		public GroupNode(final Object value) {
			super();
			this.value = value;
		}

		public void addChild(final ApplicationItem item) {
			children.add(item);
		}

		public List<ApplicationItem> getChildren() {
			return children;
		}

		public Object getValue() {
			return value;
		}

	}

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

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
		if (parentElement instanceof GroupNode)
			return ((GroupNode) parentElement).getChildren().toArray();
		return EMPTY_ARRAY;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ApplicationManager) {
			final ApplicationManager appManager = (ApplicationManager) inputElement;
			final Map<Object, Object> treeItems = new HashMap<Object, Object>();
			try {
				final Collection<String> registeredApplications = new TreeSet<String>(appManager.getRegisteredApplications());
				for (final String appId : registeredApplications) {
					final ApplicationRegistration applicationRegistration = appManager.getApplicationRegistration(appId);

					final Object groupObject = applicationRegistration.getContext();
					GroupNode treeGroup = (GroupNode) treeItems.get(groupObject);
					if (treeGroup == null) {
						treeGroup = new GroupNode(groupObject);
						treeItems.put(treeGroup.getValue(), treeGroup);
					}

					final IEclipsePreferences urlsNode = ApplicationManager.getUrlsNode();
					final Set<String> mounts = new TreeSet<String>();
					try {
						final String[] urls = urlsNode.keys();
						for (final String url : urls) {
							if (appId.equals(urlsNode.get(url, StringUtils.EMPTY))) {
								mounts.add(url);
							}
						}
					} catch (final BackingStoreException e) {
						mounts.add(e.getMessage());
					}

					final ApplicationProviderRegistration applicationProviderRegistration = HttpActivator.getInstance().getProviderRegistry().getRegisteredProviders().get(applicationRegistration.getProviderId());

					final ApplicationItem item = new ApplicationItem(applicationRegistration, applicationProviderRegistration, appManager.isActive(appId), mounts);
					treeGroup.addChild(item);

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
		if (element instanceof ApplicationItem)
			return ((ApplicationItem) element).getParent();
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof GroupNode)
			return true;
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		if ((null != this.viewer) && (this.viewer != viewer))
			throw new IllegalStateException("please use separate provider instance for different viewers");
		this.viewer = viewer;
		detach(oldInput);
		attach(newInput);
	}
}