/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.gyrex.jobs.internal.monitoring.IJobMonitor;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 
 */
public class JobMonitor extends ServiceTracker<IJobMonitor, IJobMonitor> {

	/**
	 * Creates a new instance.
	 */
	public JobMonitor(final BundleContext context) {
		super(context, IJobMonitor.class, null);
	}

	/**
	 * Returns the jobs.
	 * 
	 * @return the jobs
	 */
	public Collection<IJobMonitor> getJobs() {
		return Collections.unmodifiableCollection(getTracked().values());
	}
}
