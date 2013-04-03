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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.http.internal.HttpActivator;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;

public final class ApplicationBrowserContentProvider implements ITreeContentProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private static final Object[] EMPTY_ARRAY = new Object[0];

	@Override
	public void dispose() {
		// empty
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof ApplicationGroup)
			return ((ApplicationGroup) parentElement).getChildren().toArray();
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

					final IRuntimeContext groupObject = applicationRegistration.getContext();
					ApplicationGroup treeGroup = (ApplicationGroup) treeItems.get(groupObject);
					if (treeGroup == null) {
						treeGroup = new ApplicationGroup(groupObject, inputElement);
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
		if (element instanceof ApplicationGroup)
			return ((ApplicationItem) element).getParent();
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof ApplicationGroup)
			return true;
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// empty
	}
}