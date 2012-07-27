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

import java.util.Collections;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.DropDownItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Specialized AdminPage which allows to filter the content within the page
 * based on some criteria.
 */
public abstract class FilteredAdminPage extends AdminPage {

	private Composite filterPanel;
	private List<String> filters;

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
	public final void createFilterControls(final Composite parent) {
		filterPanel = new Composite(parent, SWT.NONE);
		filterPanel.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(5, false));

		final String customVariant = "filter";

		// filter drop-downs
		for (final String filter : getFilters()) {
			new DropDownItem(filterPanel, getFilterText(filter), customVariant) {
				@Override
				protected void openDropDown(final Point location) {
					// TODO Auto-generated method stub
				}
			};
		}

		updateFilterPanel();
	}

	protected List<String> getFilters() {
		if (null == filters) {
			return Collections.emptyList();
		}

		return filters;
	}

	protected String getFilterText(final String filter) {
		return filter;
	}

	protected void setFilters(final List<String> filters) {
		this.filters = filters;
	}

	void updateFilterPanel() {
		filterPanel.setVisible(!getFilters().isEmpty());
	}
}
