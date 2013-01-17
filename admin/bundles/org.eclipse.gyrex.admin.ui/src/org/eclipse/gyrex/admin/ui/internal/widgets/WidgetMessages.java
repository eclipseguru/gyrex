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
package org.eclipse.gyrex.admin.ui.internal.widgets;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;

/**
 * Message bundle for forked widgets.
 */
public class WidgetMessages {

	private static final String BUNDLE_NAME = "org.eclipse.gyrex.admin.ui.internal.widgets.messages";//$NON-NLS-1$

	public static WidgetMessages get() {
		final Class<WidgetMessages> clazz = WidgetMessages.class;
		return RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, clazz);
	}

	public static WidgetMessages get(final Display display) {
		final WidgetMessages[] result = { null };
		RWT.getUISession(display).exec(new Runnable() {
			@Override
			public void run() {
				result[0] = get();
			}
		});
		return result[0];
	}

	public String SelectionDialog_selectLabel;
	public String SelectionDialog_deselectLabel;

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
