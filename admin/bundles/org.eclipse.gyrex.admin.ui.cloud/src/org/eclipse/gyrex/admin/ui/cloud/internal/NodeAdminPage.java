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
package org.eclipse.gyrex.admin.ui.cloud.internal;

import org.eclipse.gyrex.admin.ui.pages.AdminPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.StringUtils;

public class NodeAdminPage extends AdminPage {

	public static final String ID = "node";

	@Override
	public Control createControl(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(StringUtils.join(getArguments(), " - "));
		return label;
	}

}
