package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.context.internal.registry.ContextDefinition;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.views.properties.IPropertySource;

public class AdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof ContextDefinition) {
			if (adapterType == IPropertySource.class) {
				return new ContextData((ContextDefinition) adaptableObject, null);
			}
		}
		// not supported
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class, ILabelProvider.class };
	}

}
