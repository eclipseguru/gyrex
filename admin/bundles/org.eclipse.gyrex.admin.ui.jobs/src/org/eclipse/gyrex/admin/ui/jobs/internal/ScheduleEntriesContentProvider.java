/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ScheduleEntriesContentProvider implements ITreeContentProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private static final Object[] EMPTY_ARRAY = new Object[0];

	@Override
	public void dispose() {
		// no-op
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		return EMPTY_ARRAY;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ScheduleImpl)
			return getScheduleEntries((ScheduleImpl) inputElement);

		return EMPTY_ARRAY;
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	private Object[] getScheduleEntries(final ScheduleImpl scheduleImpl) {
		return scheduleImpl.getEntries().toArray();
	}

	@Override
	public boolean hasChildren(final Object element) {
		return (element instanceof ScheduleImpl) && hasScheduleEntries((ScheduleImpl) element);
	}

	private boolean hasScheduleEntries(final ScheduleImpl schedule) {
		return schedule.getEntries().size() > 0;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// no-op
	}

}
