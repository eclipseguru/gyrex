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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.jobs.schedules.ISchedule;
import org.eclipse.gyrex.jobs.schedules.manager.IScheduleManager;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SchedulesContentProvider implements ITreeContentProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private static final Object[] EMPTY_ARRAY = new Object[0];

	@Override
	public void dispose() {
		// no-op
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof ContextDefinition)
			return getSchedules(((ContextDefinition) parentElement).getPath());
		return EMPTY_ARRAY;
	}

	private IRuntimeContext getContext(final IPath contextPath) {
		return JobsUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(contextPath);
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof IRuntimeContextDefinitionManager) {
			final IRuntimeContextDefinitionManager contextRegistry = (IRuntimeContextDefinitionManager) inputElement;
			final List<ContextDefinition> definedContexts = contextRegistry.getDefinedContexts();
			final List<ContextDefinition> result = new ArrayList<>(definedContexts.size());
			for (final ContextDefinition contextDefinition : definedContexts) {
				if (hasSchedules(contextDefinition.getPath())) {
					result.add(contextDefinition);
				}
			}
			return result.toArray();
		}

		return EMPTY_ARRAY;
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	private Object[] getSchedules(final IPath contextPath) {
		try {
			final IRuntimeContext context = getContext(contextPath);
			if (context == null)
				return EMPTY_ARRAY;

			final IScheduleManager scheduleManager = context.get(IScheduleManager.class);
			if (scheduleManager == null)
				return EMPTY_ARRAY;

			final Collection<String> scheduleIds = scheduleManager.getSchedules();
			final List<ISchedule> schedules = new ArrayList<>(scheduleIds.size());
			for (final String id : scheduleIds) {
				final ISchedule schedule = scheduleManager.getSchedule(id);
				if (schedule != null) {
					schedules.add(schedule);
				}
			}
			return schedules.toArray();
		} catch (final Exception e) {
			final String[] errorresponse = { e.getMessage() };
			return errorresponse;
		}
	}

	@Override
	public boolean hasChildren(final Object element) {
		return (element instanceof ContextDefinition) && hasSchedules(((ContextDefinition) element).getPath());
	}

	private boolean hasSchedules(final IPath contextPath) {
		try {
			final IRuntimeContext context = getContext(contextPath);
			if (context == null)
				return false;

			final IScheduleManager scheduleManager = context.get(IScheduleManager.class);
			if (scheduleManager == null)
				return false;

			return !scheduleManager.getSchedules().isEmpty();
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// no-op
	}

}
