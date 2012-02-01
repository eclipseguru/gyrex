/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.configuration;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * A container that hosts {@link ConfigurationPage configuration pages}.
 */
public interface IConfigurationPageContainer {

	/**
	 * Returns active page instance if the currently selected page index is not
	 * -1, or <code>null</code> if it is.
	 * 
	 * @return active page instance if selected, or <code>null</code> if no page
	 *         is currently active.
	 */
	ConfigurationPage getActivePageInstance();

	/**
	 * Returns the workbench part site the container is hosted in.
	 * 
	 * @return the workbench part site (may be <code>null</code>)
	 * @see IWorkbenchPart#getSite()
	 */
	public IWorkbenchPartSite getSite();

}
