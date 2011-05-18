/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation, AGETO Service GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - copied from JDT and adapted to Gyrex
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.helper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

/**
 * Utility class to simplify access to some SWT resources.
 */
public class SwtUtil {

	/**
	 * The default visible item count for {@link Combo}s. Workaround for
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=245569 .
	 * 
	 * @see Combo#setVisibleItemCount(int)
	 * @since 3.5
	 */
	public static final int COMBO_VISIBLE_ITEM_COUNT = 30;

	/**
	 * Returns a width hint for a button control.
	 * 
	 * @param button
	 *            the button
	 * @return the width hint
	 */
	public static int getButtonWidthHint(final Button button) {
		button.setFont(JFaceResources.getDialogFont());
		final PixelConverter converter = new PixelConverter(button);
		final int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	/**
	 * Returns the shell for the given widget. If the widget doesn't represent a
	 * SWT object that manage a shell, <code>null</code> is returned.
	 * 
	 * @param widget
	 *            the widget
	 * @return the shell for the given widget
	 */
	public static Shell getShell(final Widget widget) {
		if (widget instanceof Control) {
			return ((Control) widget).getShell();
		}
//		if (widget instanceof Caret) {
//			return ((Caret) widget).getParent().getShell();
//		}
		if (widget instanceof DragSource) {
			return ((DragSource) widget).getControl().getShell();
		}
		if (widget instanceof DropTarget) {
			return ((DropTarget) widget).getControl().getShell();
		}
		if (widget instanceof Menu) {
			return ((Menu) widget).getParent().getShell();
		}
		if (widget instanceof ScrollBar) {
			return ((ScrollBar) widget).getParent().getShell();
		}

		return null;
	}

	public static int getTableHeightHint(final Table table, final int rows) {
		if (table.getFont().equals(JFaceResources.getDefaultFont())) {
			table.setFont(JFaceResources.getDialogFont());
		}
		int result = table.getItemHeight() * rows + table.getHeaderHeight();
		if (table.getLinesVisible()) {
			result += table.getGridLineWidth() * (rows - 1);
		}
		return result;
	}

	public static GridLayout newLayoutNoMargins(final int columns) {
		final GridLayout layout = new GridLayout(columns, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		return layout;
	}

	/**
	 * Adds an accessibility listener returning the given fixed name.
	 * 
	 * @param control
	 *            the control to add the accessibility support to
	 * @param text
	 *            the name
	 */
	public static void setAccessibilityText(final Control control, final String text) {
//		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
//			@Override
//			public void getName(final AccessibleEvent e) {
//				if (e.childID == ACC.CHILDID_SELF) {
//					e.result = text;
//				}
//			}
//		});
	}

	/**
	 * Sets width and height hint for the button control. <b>Note:</b> This is a
	 * NOP if the button's layout data is not an instance of
	 * <code>GridData</code>.
	 * 
	 * @param button
	 *            the button for which to set the dimension hint
	 */
	public static void setButtonDimensionHint(final Button button) {
		Assert.isNotNull(button);
		final Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	/**
	 * Sets the default visible item count for {@link Combo}s. Workaround for
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=7845 .
	 * 
	 * @param combo
	 *            the combo
	 * @see Combo#setVisibleItemCount(int)
	 * @see #COMBO_VISIBLE_ITEM_COUNT
	 * @since 3.5
	 */
	public static void setDefaultVisibleItemCount(final Combo combo) {
		combo.setVisibleItemCount(COMBO_VISIBLE_ITEM_COUNT);
	}

}
