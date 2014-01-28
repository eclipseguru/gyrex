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
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.adapter;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;

/**
 * Utility for loading adapter.
 */
public class AdapterUtil {

	public static <T> T getAdapter(final Object adaptable, final Class<T> adapterType) {
		if (adaptable == null)
			return null;

		// check for direct implementation first
		if (adapterType.isInstance(adaptable))
			return adapterType.cast(adaptable);

		// try adaptable
		if (adaptable instanceof IAdaptable) {
			final T adapter = adapterType.cast(((IAdaptable) adaptable).getAdapter(adapterType));
			if (null != adapter)
				return adapter;
		}

		// check Adapter manager if an adapter is loadable
		final IAdapterManager manager = AdminUiActivator.getInstance().getService(IAdapterManager.class);
		if (manager.hasAdapter(adaptable, adapterType.getName())) {
			final Object adapter = manager.loadAdapter(adaptable, adapterType.getName());
			if (adapterType.isInstance(adapter))
				return adapterType.cast(adapter);
		}

		return null;
	}

	private AdapterUtil() {
		// empty
	}

}
