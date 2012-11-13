/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Sebastian Davids - bug 128529
 * Semion Chichelnitsky (semion@il.ibm.com) - bug 278064
 * Peter Grube - adapt to Gyrex UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.lifecycle.UICallBack;
import org.eclipse.swt.widgets.Display;

/**
 * Messages class for FilteredItemsSelectionDialog messages.
 */
public class FilteredItemsSelectionDialogMessages {

	private static final String BUNDLE_NAME = "org.eclipse.gyrex.admin.ui.p2.internal.messages";

	public static FilteredItemsSelectionDialogMessages get() {
		final Class clazz = FilteredItemsSelectionDialogMessages.class;
		final Object result = RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, clazz);
		return (FilteredItemsSelectionDialogMessages) result;
	}

	public static FilteredItemsSelectionDialogMessages get(final Display display) {
		final FilteredItemsSelectionDialogMessages[] result = { null };
		UICallBack.runNonUIThreadWithFakeContext(display, new Runnable() {
			@Override
			public void run() {
				result[0] = get();
			}
		});
		return result[0];
	}

	public String FilteredItemsSelectionDialog_cacheSearchJob_taskName;
	public String FilteredItemsSelectionDialog_menu;
	public String FilteredItemsSelectionDialog_refreshJob;
	public String FilteredItemsSelectionDialog_progressRefreshJob;
	public String FilteredItemsSelectionDialog_cacheRefreshJob;
	public String FilteredItemsSelectionDialog_cacheRefreshJob_checkDuplicates;
	public String FilteredItemsSelectionDialog_cacheRefreshJob_getFilteredElements;
	public String FilteredItemsSelectionDialog_patternLabel;
	public String FilteredItemsSelectionDialog_listLabel;
	public String FilteredItemsSelectionDialog_toggleStatusAction;
	public String FilteredItemsSelectionDialog_removeItemsFromHistoryAction;
	public String FilteredItemsSelectionDialog_searchJob_taskName;
	public String FilteredItemsSelectionDialog_separatorLabel;
	public String FilteredItemsSelectionDialog_storeError;

	public String FilteredItemsSelectionDialog_restoreError;

	public String FilteredItemsSelectionDialog_nItemsSelected;

	public String FilteredItemsSelectionDialog_jobLabel;
	public String FilteredItemsSelectionDialog_jobError;
	public String FilteredItemsSelectionDialog_jobCancel;

	public String FilteredItemsSelectionDialog_taskProgressMessage;
	public String FilteredItemsSelectionDialog_subtaskProgressMessage;
}
