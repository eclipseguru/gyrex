/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Tomas Tamosaitis - Updated for new API implementation 
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The Class represents RAP UI page to manage ZooKeeper Context values.
 */
public class ContextApplicationPage extends AdminPage {

	/** The context section represent UI part for context listing. */
	private ContextsSection contextSection;

	/** The page composite the root IU component for context management. */
	private Composite pageComposite;

	/**
	 * Creates a new instance.
	 */
	public ContextApplicationPage() {
		setTitle("Runtime Contexts");
		setTitleToolTip("Define runtime contexts for web applications and background processing.");
	}

	@Override
	public Control createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		contextSection = new ContextsSection();
		contextSection.createContextSectionControl(pageComposite);

		return pageComposite;
	}
}
