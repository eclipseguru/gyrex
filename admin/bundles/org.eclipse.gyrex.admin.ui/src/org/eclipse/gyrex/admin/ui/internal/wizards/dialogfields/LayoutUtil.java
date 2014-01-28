/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Guven Demir <guven.internet+eclipse@gmail.com> - [package explorer] Alternative package name shortening: abbreviation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=299514
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LayoutUtil {

	/**
	 * Creates a composite and fills in the given editors.
	 * 
	 * @param labelOnTop
	 *            Defines if the label of all fields should be on top of the
	 *            fields
	 */
	public static void doDefaultLayout(final Composite parent, final DialogField[] editors, final boolean labelOnTop) {
		doDefaultLayout(parent, editors, labelOnTop, 0, 0);
	}

	/**
	 * Creates a composite and fills in the given editors.
	 * 
	 * @param labelOnTop
	 *            Defines if the label of all fields should be on top of the
	 *            fields
	 * @param marginWidth
	 *            The margin width to be used by the composite
	 * @param marginHeight
	 *            The margin height to be used by the composite
	 */
	public static void doDefaultLayout(final Composite parent, final DialogField[] editors, final boolean labelOnTop, final int marginWidth, final int marginHeight) {
		int nCulumns = getNumberOfColumns(editors);
		final Control[][] controls = new Control[editors.length][];
		for (int i = 0; i < editors.length; i++) {
			controls[i] = editors[i].doFillIntoGrid(parent, nCulumns);
		}
		if (labelOnTop) {
			nCulumns--;
			modifyLabelSpans(controls, nCulumns);
		}
		GridLayout layout = null;
		if (parent.getLayout() instanceof GridLayout) {
			layout = (GridLayout) parent.getLayout();
		} else {
			layout = new GridLayout();
		}
		if (marginWidth != SWT.DEFAULT) {
			layout.marginWidth = marginWidth;
		}
		if (marginHeight != SWT.DEFAULT) {
			layout.marginHeight = marginHeight;
		}
		layout.numColumns = nCulumns;
		parent.setLayout(layout);
	}

	/**
	 * Calculates the number of columns needed by field editors
	 */
	public static int getNumberOfColumns(final DialogField[] editors) {
		int nCulumns = 0;
		for (final DialogField editor : editors) {
			nCulumns = Math.max(editor.getNumberOfControls(), nCulumns);
		}
		return nCulumns;
	}

	private static void modifyLabelSpans(final Control[][] controls, final int nCulumns) {
		for (final Control[] control : controls) {
			setHorizontalSpan(control[0], nCulumns);
		}
	}

	/**
	 * Sets the heightHint hint of a control. Assumes that GridData is used.
	 */
	public static void setHeightHint(final Control control, final int heightHint) {
		final Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).heightHint = heightHint;
		}
	}

	/**
	 * Sets the horizontal grabbing of a control to true. Assumes that GridData
	 * is used.
	 */
	public static void setHorizontalGrabbing(final Control control) {
		final Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).grabExcessHorizontalSpace = true;
		}
	}

	/**
	 * Sets the horizontal indent of a control. Assumes that GridData is used.
	 */
	public static void setHorizontalIndent(final Control control, final int horizontalIndent) {
		final Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).horizontalIndent = horizontalIndent;
		}
	}

	/**
	 * Sets the span of a control. Assumes that GridData is used.
	 */
	public static void setHorizontalSpan(final Control control, final int span) {
		final Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).horizontalSpan = span;
		} else if (span != 1) {
			final GridData gd = new GridData();
			gd.horizontalSpan = span;
			control.setLayoutData(gd);
		}
	}

	/**
	 * Sets the margin of a control. Assumes that GridLayout is used.
	 */
	public static void setMargin(final Composite composite, final int margin) {
		final Object l = composite.getLayout();
		if (l instanceof GridLayout) {
			final GridLayout gl = (GridLayout) l;
			gl.marginHeight = margin;
			gl.marginWidth = margin;
		}
	}

	/**
	 * Sets the vertical grabbing of a control to true. Assumes that GridData is
	 * used.
	 * 
	 * @since 3.6
	 */
	public static void setVerticalGrabbing(final Control control) {
		final Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			final GridData gd = (GridData) ld;
			gd.grabExcessVerticalSpace = true;
			gd.verticalAlignment = SWT.FILL;
		}
	}

	/**
	 * Sets the width hint of a control. Assumes that GridData is used.
	 */
	public static void setWidthHint(final Control control, final int widthHint) {
		final Object ld = control.getLayoutData();
		if (ld instanceof GridData) {
			((GridData) ld).widthHint = widthHint;
		}
	}
}
