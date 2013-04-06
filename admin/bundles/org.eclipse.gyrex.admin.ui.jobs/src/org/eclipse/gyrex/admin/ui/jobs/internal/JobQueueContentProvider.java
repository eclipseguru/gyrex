/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.gyrex.cloud.services.queue.IMessage;
import org.eclipse.gyrex.cloud.services.queue.IQueue;
import org.eclipse.gyrex.cloud.services.queue.IQueueServiceProperties;
import org.eclipse.gyrex.jobs.internal.worker.JobInfo;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * List content of a job queue. Input must be queue object.
 */
public class JobQueueContentProvider implements ITreeContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];
	private static final long serialVersionUID = 1L;

	@Override
	public void dispose() {
		// no-op
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IQueue) {
			final IQueue queue = (IQueue) parentElement;

			// get message (without timeout)
			final HashMap<String, Object> properties = new HashMap<>(2);
			properties.put(IQueueServiceProperties.MESSAGE_RECEIVE_TIMEOUT, new Long(0));
			final List<IMessage> message = queue.receiveMessages(500, properties);
			if (message.isEmpty())
				return NO_CHILDREN;

			final List<Object> result = new ArrayList<Object>(message.size());
			for (final IMessage m : message) {
				try {
					result.add(JobInfo.parse(m));
				} catch (final Exception | LinkageError | AssertionError e) {
					result.add(String.format("Unparsable message (%s). %s", m, ExceptionUtils.getRootCauseMessage(e)));
				}
			}
			return result.toArray();
		}
		if (parentElement instanceof Collection)
			return ((Collection) parentElement).toArray();
		if (parentElement instanceof Object[])
			return (Object[]) parentElement;
		return NO_CHILDREN;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof IQueue) {
			final IQueue queue = (IQueue) element;

			// get message (without timeout)
			final HashMap<String, Object> properties = new HashMap<>(2);
			properties.put(IQueueServiceProperties.MESSAGE_RECEIVE_TIMEOUT, new Long(0));
			final List<IMessage> message = queue.receiveMessages(1, properties);
			return !message.isEmpty();
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// TODO Auto-generated method stub

	}

}
