/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

@SuppressWarnings("restriction")
public class ApplicationRegistrationPropertySource implements IPropertySource {

	private static final String PROP_CONTEXT_PATH = "contextPath";
	private static final String PROP_PROVIDER = "provider";
	private static final String PROP_ID = "id";
	private static final String PROP_MOUNTS = "urls";

	private final ApplicationRegistration applicationRegistration;
	private final ApplicationMountsPropertySource mounts;

	/**
	 * Creates a new instance.
	 */
	public ApplicationRegistrationPropertySource(final ApplicationRegistration applicationRegistration) {
		this.applicationRegistration = applicationRegistration;
		mounts = new ApplicationMountsPropertySource(applicationRegistration);
	}

	@Override
	public Object getEditableValue() {
		return applicationRegistration;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { new PropertyDescriptor(PROP_ID, "ID"), new PropertyDescriptor(PROP_PROVIDER, "Provider"), new PropertyDescriptor(PROP_CONTEXT_PATH, "Context"), new PropertyDescriptor(PROP_MOUNTS, "URLs") };
	}

	@Override
	public Object getPropertyValue(final Object id) {
		if (PROP_ID == id) {
			return applicationRegistration.getApplicationId();
		} else if (PROP_PROVIDER == id) {
			return applicationRegistration.getProviderId();
		} else if (PROP_CONTEXT_PATH == id) {
			return applicationRegistration.getContext().getContextPath().toString();
		} else if (PROP_MOUNTS == id) {
			return mounts;
		}

		return null;
	}

	@Override
	public boolean isPropertySet(final Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(final Object id) {
		// empty
	}

	@Override
	public void setPropertyValue(final Object id, final Object value) {
		// empty
	}

}