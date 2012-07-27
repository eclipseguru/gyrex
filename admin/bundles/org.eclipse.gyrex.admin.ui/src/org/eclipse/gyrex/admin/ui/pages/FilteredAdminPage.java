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

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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
	public final Control getFilterControl(final Composite parent) {
		if (null != filterPanel) {
			return filterPanel;
		}

		filterPanel = new Composite(parent, SWT.NONE);
		final RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.fill = true;
		layout.marginTop = 0;
		filterPanel.setLayout(layout);

		final String customVariant = "filter";

		// toolbar
		final ToolBar toolBar = new ToolBar(filterPanel, SWT.HORIZONTAL);
		toolBar.setData(WidgetUtil.CUSTOM_VARIANT, customVariant);

		// tool item
		for (final String filter : getFilters()) {
			final ToolItem toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
			toolItem.setData(WidgetUtil.CUSTOM_VARIANT, customVariant);
			toolItem.setText(getFilterText(filter));
		}

		updateFilterPanel();
		return filterPanel;
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
