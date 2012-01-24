/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.data.Stat;

public class RunningJob {

	final String storageKey;
	final String id;
	private final String nodeId;
	private final Stat stat;
	boolean aborting;

	/**
	 * Creates a new instance.
	 * 
	 * @param storageKey
	 * @param id
	 * @param nodeId
	 * @param stat
	 */
	public RunningJob(final String storageKey, final String id, final String nodeId, final Stat stat) {
		this.storageKey = storageKey;
		this.id = id;
		this.nodeId = nodeId;
		this.stat = stat;
	}

	public String getLabel() {
		final StringBuilder l = new StringBuilder();
		l.append(id);
		l.append(" (");
		if (aborting) {
			l.append("ABORTING ");
		}
		l.append(nodeId).append(", started ");
		final long started = System.currentTimeMillis() - stat.getCtime();
		if (started < TimeUnit.MINUTES.toMillis(2)) {
			l.append("a minute ago");
		} else if (started < TimeUnit.HOURS.toMillis(1)) {
			l.append(TimeUnit.MILLISECONDS.toMinutes(started)).append(" minutes ago");
		} else if (started < TimeUnit.DAYS.toMillis(1)) {
			l.append(TimeUnit.MILLISECONDS.toHours(started)).append(" hours ago");
			return String.format("%s (%s, started %d hours ago)", id, nodeId, TimeUnit.MILLISECONDS.toHours(started));
		} else {
			l.append(TimeUnit.MILLISECONDS.toDays(started)).append(" days ago");
			return String.format("%s (%s, started %d days ago)", id, nodeId, TimeUnit.MILLISECONDS.toDays(started));
		}
		l.append(')');
		return l.toString();
	}

}