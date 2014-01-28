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
package org.eclipse.gyrex.admin.ui.persistence.internal.adapters;

import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;

import org.eclipse.core.runtime.IAdapterFactory;

public class AdapterFactory implements IAdapterFactory {

	private static final Class[] ADAPTER_TYPES = new Class[] { LabelAdapter.class };

	private static final LabelAdapter ADAPTER = new WorkbenchAdapterImpl();

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (LabelAdapter.class.equals(adapterType))
			return ADAPTER;
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return ADAPTER_TYPES;
	}

}
