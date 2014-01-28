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
package org.eclipse.gyrex.admin.ui.pages;

import java.util.Collections;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.DropDownItem;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Specialized AdminPage which allows to filter the content within the page
 * based on some criteria.
 */
public abstract class FilteredAdminPage extends AdminPage {

	private Composite filterPanel;
	private List<String> filters;

	/**
	 * Called when the drop-down for a filter has been clicked to create the
	 * contents for the filter pop-up.
	 * <p>
	 * Subclasses must override and create an appropriate control for
	 * manipulating a filter.
	 * </p>
	 * 
	 * @param filter
	 *            the filter
	 * @param parent
	 *            the parent composite
	 * @return the created control
	 */
	protected Control createFilterControl(final String filter, final Composite parent) {
		final Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout());

		final Label label = new Label(parent, SWT.NONE);
		label.setText(filter);

		return composite;
	}

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
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				protected void openDropDown(final Point location) {
					setOpen(true);
					openFilterPopUp(filter, location, new Runnable() {

						@Override
						public void run() {
							setOpen(false);
						}
					});
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

	/**
	 * Called dynamically to obtain the text to be displayed as title for a
	 * filter in the filter bar.
	 * 
	 * @param filter
	 *            the filter
	 * @return the text
	 */
	protected String getFilterText(final String filter) {
		return filter;
	}

	void openFilterPopUp(final String filter, final Point location, final Runnable closeCallback) {
		final PopupDialog dialog = new PopupDialog(SwtUtil.getShell(filterPanel), SWT.NO_TRIM | SWT.NO_SCROLL | SWT.MODELESS, false, false, false, false, false, null, null) {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			protected void adjustBounds() {
				getShell().pack(true);
				getShell().setLocation(location);
			}

			@Override
			public boolean close() {
				final boolean closed = super.close();
				if (!closed) {
					return closed;
				}

				if (null != closeCallback) {
					closeCallback.run();
				}
				return closed;
			}

			@Override
			protected void configureShell(final Shell shell) {
				super.configureShell(shell);
				shell.setLayout(new FillLayout());
				shell.setData(RWT.CUSTOM_VARIANT, "filter-popup"); //$NON-NLS-1$
			}

			@Override
			protected Control createContents(final Composite parent) {
				final Control control = createFilterControl(filter, parent);
				if (parent.getLayout() instanceof FillLayout) {
					final Control[] children = parent.getChildren();
					for (final Control child : children) {
						if (null != child.getLayoutData()) {
							throw new IllegalStateException(String.format("%s#createFilterControl not allowed to set layout data on children!", FilteredAdminPage.this.getClass()));
						}
					}
				}
				return control;
			}

			@Override
			public int open() {
				final int result = super.open();
				final Listener closeListener = new Listener() {
					/** serialVersionUID */
					private static final long serialVersionUID = 1L;

					@Override
					public void handleEvent(final Event event) {
						close();
					}
				};
				getShell().addListener(SWT.Deactivate, closeListener);
				getShell().addListener(SWT.Close, closeListener);

				getShell().setActive();
				return result;
			}
		};

		dialog.open();
	}

	protected void setFilters(final List<String> filters) {
		this.filters = filters;
	}

	void updateFilterPanel() {
		filterPanel.setVisible(!getFilters().isEmpty());
	}
}
