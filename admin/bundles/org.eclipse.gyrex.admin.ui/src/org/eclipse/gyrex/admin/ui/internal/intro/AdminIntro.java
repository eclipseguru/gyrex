/**
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.intro;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.IntroPart;

/**
 * Intro implementation
 */
public class AdminIntro extends IntroPart {

	private Text text;

	@Override
	public void createPartControl(final Composite parent) {
		text = new Text(parent, SWT.NONE);
		text.setText("Hallo Intro!");
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#standbyStateChanged(boolean)
	 */
	@Override
	public void standbyStateChanged(final boolean standby) {
		// TODO Auto-generated method stub

	}

}
