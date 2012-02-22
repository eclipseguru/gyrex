/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminApplicationConfigurator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

public class AdminUiServiceFilterFactory implements IExecutableExtensionFactory {

	@Override
	public Object create() throws CoreException {
		try {
			return FrameworkUtil.createFilter(AdminApplicationConfigurator.FILTER);
		} catch (final InvalidSyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, e.getMessage(), e));
		}
	}

}
