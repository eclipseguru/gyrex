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

import org.eclipse.gyrex.jobs.provider.JobProvider;

/**
 * Use by content providers to represent a job type
 */
public class JobType {

	final String id;
	final JobProvider provider;

	public JobType(final String id, final JobProvider provider) {
		super();
		this.id = id;
		this.provider = provider;
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public String getName() {
		return id;
	}

}
