package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.context.internal.registry.ContextDefinition;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class AdapterFactory implements IAdapterFactory {

	/** CLASSES */
	private static final Class[] ADAPTER_LIST = new Class[] { IPropertySource.class, IWorkbenchAdapter.class };
	private static final IWorkbenchAdapter WORKBENCH_ADAPTER = new WorkbenchAdapterImpl();

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof ContextDefinition) {
			if (adapterType == IPropertySource.class) {
				return new ContextData((ContextDefinition) adaptableObject, null);
			}
			if (adapterType == IWorkbenchAdapter.class) {
				return WORKBENCH_ADAPTER;
			}
		}
		// not supported
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
