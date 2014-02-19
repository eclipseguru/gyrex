/*******************************************************************************
 * Copyright (c) 2014 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.jobs.internal.components;

import java.lang.annotation.Annotation;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.provider.di.ExtendedObjectResolver;
import org.eclipse.gyrex.jobs.IJobContext;
import org.eclipse.gyrex.jobs.annotation.JobParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExtendedObjectSupplier for injecting {@link JobParameter} annotated strings.
 */
@SuppressWarnings("restriction")
public class JobParameterSupplierComponent extends ExtendedObjectResolver {
	private static final Logger LOG = LoggerFactory.getLogger(JobParameterSupplierComponent.class);

	@Override
	public Object get(final Class<?> type, final IRuntimeContext context, final Annotation annotation) {
		final IJobContext jobContext = context.get(IJobContext.class);
		if (jobContext == null) {
			LOG.debug("No IJobContext available in {}.", context);
		}
		final JobParameter parameter = (JobParameter) annotation;
		return jobContext.getParameter().get(parameter.value());
	}
}
