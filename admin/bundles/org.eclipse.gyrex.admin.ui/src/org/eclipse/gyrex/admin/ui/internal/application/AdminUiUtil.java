/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - adapted to Gyrex
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public final class AdminUiUtil {

	private static final int DEFAULT_SPACE = 10;

	public static GridData createFillData() {
		return new GridData(SWT.FILL, SWT.FILL, true, true);
	}

	public static FillLayout createFillLayout(final boolean setMargin) {
		final FillLayout result = new FillLayout();
		if (setMargin) {
			result.marginWidth = DEFAULT_SPACE;
			result.marginHeight = DEFAULT_SPACE;
		}
		return result;
	}

	public static GridLayout createGridLayout(final int numColumns, final boolean makeColsEqualWidth, final boolean setTopMargin, final boolean setVertSpacing) {
		final GridLayout result = new GridLayout(numColumns, makeColsEqualWidth);
		result.marginWidth = DEFAULT_SPACE;
		result.marginHeight = 0;
		result.marginBottom = DEFAULT_SPACE;
		result.horizontalSpacing = DEFAULT_SPACE;
		if (setTopMargin) {
			result.marginTop = DEFAULT_SPACE;
		}
		if (setVertSpacing) {
			result.verticalSpacing = DEFAULT_SPACE;
		}
		return result;
	}

	public static GridLayout createGridLayoutWithoutMargin(final int numColumns, final boolean makeColsEqualWidth) {
		final GridLayout result = new GridLayout(numColumns, makeColsEqualWidth);
		result.marginHeight = 0;
		result.marginWidth = 0;
		return result;
	}

	public static void createHeading(final Composite parent, final String text, final int horizontalSpan) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setData(WidgetUtil.CUSTOM_VARIANT, "heading");
		final GridData labelLayoutData = new GridData();
		labelLayoutData.horizontalSpan = horizontalSpan;
		label.setLayoutData(labelLayoutData);
	}

	private static Label createHeadlineLabel(final Composite parent, final String text) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(text.replace("&", "&&"));
		label.setData(WidgetUtil.CUSTOM_VARIANT, "pageHeadline");
		return label;
	}

	private static GridData createHeadlineLayoutData() {
		final GridData layoutData = new GridData();
		layoutData.verticalIndent = 30;
//		layoutData.horizontalIndent = DEFAULT_SPACE;
		return layoutData;
	}

	public static GridData createHorzFillData() {
		return new GridData(SWT.FILL, SWT.TOP, true, false);
	}

	public static GridData createHorzFillData(final int horizontalSpan) {
		final GridData gd = createHorzFillData();
		gd.horizontalSpan = horizontalSpan;
		return gd;
	}

	public static GridLayout createMainLayout(final int numColumns) {
		final GridLayout result = new GridLayout(numColumns, true);
		result.marginWidth = 0;
		result.marginHeight = 0;
		result.marginTop = 0;
		result.verticalSpacing = 0;
		result.horizontalSpacing = 60;
		return result;
	}

	public static GridLayout createMainLayout(final int numColumns, final int horzSpacing) {
		final GridLayout result = new GridLayout(numColumns, true);
		result.marginWidth = 0;
		result.marginHeight = 0;
		result.marginTop = 0;
		result.verticalSpacing = 0;
		result.horizontalSpacing = horzSpacing;
		return result;
	}

	public static RowLayout createRowLayout(final int type, final boolean setMargin) {
		final RowLayout result = new RowLayout(type);
		result.marginTop = 0;
		result.marginLeft = 0;
		result.marginHeight = 0;
		if (setMargin) {
			result.marginBottom = DEFAULT_SPACE;
			result.marginWidth = DEFAULT_SPACE;
		} else {
			result.marginBottom = 0;
			result.marginWidth = 0;
		}
		return result;
	}

	public static Composite initPage(final String title, final Composite parent) {
		final Composite pageComp = new Composite(parent, SWT.NONE);
		pageComp.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));
		final Label label = createHeadlineLabel(pageComp, title);
		label.setLayoutData(createHeadlineLayoutData());
		final Composite contentComp = new Composite(pageComp, SWT.NONE);
		contentComp.setLayoutData(AdminUiUtil.createFillData());
		contentComp.setLayout(new FillLayout());
		return contentComp;
	}

	private AdminUiUtil() {
		// prevent instantiation
	}

}
