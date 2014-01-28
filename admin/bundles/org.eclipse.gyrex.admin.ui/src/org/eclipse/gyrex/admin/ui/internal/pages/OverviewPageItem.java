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
package org.eclipse.gyrex.admin.ui.internal.pages;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A way to provide items to the overview page without making the overview page
 * depend on too many Gyrex features.
 * <p>
 * Implementations may be registered as OSGi services. Note, this is internal
 * and not official API. If you wish to use it in your project, please open a
 * bug and we'll happily work with you on making this API.
 * </p>
 */
public abstract class OverviewPageItem {

	/**
	 * Creates the item control for the specified parent.
	 * <p>
	 * Items are presented in a grid on the overview page. Thus, implementors
	 * may assume that the parent has a {@link GridLayout} set.
	 * </p>
	 * 
	 * @param parent
	 *            the parent control
	 * @return the created control
	 */
	public abstract Control createControl(Composite parent);

}
