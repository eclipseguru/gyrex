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
package org.eclipse.gyrex.jobs.internal.worker;

import org.eclipse.gyrex.jobs.IJobContext;

import org.slf4j.MDC;

/**
 * Helper class for log management of jobs.
 */
public class JobLogHelper {

	private static final String MDC_KEY_CONTEXT_PATH = "gyrex.contextPath";
	private static final String MDC_KEY_JOB_ID = "gyrex.jobId";

	public static void clearMdc() {
		MDC.remove(MDC_KEY_JOB_ID);
		MDC.remove(MDC_KEY_CONTEXT_PATH);
	}

	public static void setupMdc(final IJobContext jobContext) {
		MDC.put(MDC_KEY_JOB_ID, jobContext.getJobId());
		MDC.put(MDC_KEY_CONTEXT_PATH, jobContext.getContext().getContextPath().toString());
	}

	private JobLogHelper() {
		// empty
	}

}
