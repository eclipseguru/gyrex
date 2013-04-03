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
package org.eclipse.gyrex.admin.ui.jobs.internal.externalprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.gyrex.admin.ui.internal.widgets.ElementListSelectionDialog;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;

public class SelectEnvironmentVariableDialog extends ElementListSelectionDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	public SelectEnvironmentVariableDialog(final Shell shell) {
		super(shell, new LabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof Entry) {
					final Entry e = (Entry) element;
					return e.getKey() + " [" + e.getValue() + "]";
				}
				return super.getText(element);
			}
		});
		setMultipleSelection(true);
		setTitle("Select Environment Variable");
		setMessage("&Select variable to inherit from the system environment:");

		setElements(System.getenv().entrySet().toArray());
	}

	public List<String> getVariables() {
		final List<String> result = new ArrayList<>();
		for (final Object element : getResult()) {
			if (element instanceof Entry) {
				result.add(String.valueOf(((Entry) element).getKey()));
			}
		}
		return result;
	}
}
