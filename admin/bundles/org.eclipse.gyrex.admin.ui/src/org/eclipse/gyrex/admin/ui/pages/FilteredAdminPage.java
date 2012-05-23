/*******************************************************************************
 * Copyright (c) 2012 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.pages;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Specialized AdminPage which allows to filter the content within the page
 * based on some criteria.
 */
public abstract class FilteredAdminPage extends AdminPage {

	private Composite filterPanel;

	/**
	 * Creates the filter control.
	 * <p>
	 * Clients should not call this method (the Admin UI calls this method at
	 * appropriate times).
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @return the created filter control
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public Control createFilterControl(final Composite parent) {
		filterPanel = new Composite(parent, SWT.NONE);
		filterPanel.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));
		updateFilterPanel();
		return filterPanel;
	}

	void updateFilterPanel() {
		filterPanel.setVisible(false);
	}
}
