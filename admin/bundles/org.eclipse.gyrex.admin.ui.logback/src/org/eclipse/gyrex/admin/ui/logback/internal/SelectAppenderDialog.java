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
 *     Peter Grube        - rework to Admin UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.widgets.ElementListSelectionDialog;
import org.eclipse.gyrex.logback.config.internal.model.Appender;

import org.eclipse.swt.widgets.Shell;

public class SelectAppenderDialog extends ElementListSelectionDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	public SelectAppenderDialog(final Shell shell, final Collection<Appender> appenders) {
		super(shell, new LogbackLabelProvider());
		setTitle("Select Appender");
		setMessage("&Select an appender to add to your logger:");
		setElements(appenders.toArray());
	}
}
