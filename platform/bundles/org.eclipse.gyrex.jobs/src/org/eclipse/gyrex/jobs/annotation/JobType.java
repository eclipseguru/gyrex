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
package org.eclipse.gyrex.jobs.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.jobs.IJob;
import org.eclipse.gyrex.jobs.IJobContext;

import org.eclipse.core.runtime.jobs.Job;

/**
 * Marks a class as a provider of an {@link Job Eclipse Job} implementation.
 * <p>
 * This annotation can be used to identify jobs provided to Gyrex. In
 * combination with the scanning job provider, Gyrex will automatically
 * instantiate job class when needed.
 * </p>
 * <p>
 * Dependency inject will be support for creating new instances based on
 * {@link IRuntimeContext#getInjector() the runtime context}. In addition to the
 * runtime context, injection of {@link IJobContext} will be supported to.
 * </p>
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface JobType {

	/**
	 * Defines the job type identifier (see {@link IJob#getTypeId()}).
	 * 
	 * @return
	 */
	String typeId();

}
