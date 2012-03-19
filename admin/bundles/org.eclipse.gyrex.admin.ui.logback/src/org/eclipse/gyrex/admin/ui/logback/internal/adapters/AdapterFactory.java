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
package org.eclipse.gyrex.admin.ui.logback.internal.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AdapterFactory implements IAdapterFactory {

	private static final Class[] ADAPTER_TYPES = new Class[] { IWorkbenchAdapter.class };

	private static final IWorkbenchAdapter ADAPTER = new WorkbenchAdapterImpl();

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (IWorkbenchAdapter.class.equals(adapterType)) {
			return ADAPTER;
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return ADAPTER_TYPES;
	}

}
