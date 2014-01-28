/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Gunnar Wagenknecht - fork to Gyrex Admin UI and adopted for RAP
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * The UIJob is a Job that runs within the UI Thread via an asyncExec.
 * 
 * @since 1.0
 */
public abstract class UiJob extends Job {
	/**
	 * Convenience method to return a status for an exception.
	 * 
	 * @param exception
	 * @return IStatus an error status built from the exception
	 * @see Job
	 */
	public static IStatus errorStatus(final Throwable exception) {
		return new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, exception.getMessage(), exception);
	}

	private Display cachedDisplay;

	/**
	 * Create a new instance of the receiver with the supplied Display.
	 * 
	 * @param jobDisplay
	 *            the display
	 * @param name
	 *            the job name
	 * @see Job
	 */
	public UiJob(final Display jobDisplay, final String name) {
		super(name);
		setDisplay(jobDisplay);
	}

	/**
	 * Returns the display for use by the receiver when running in an asyncExec.
	 * If it is not set then the display set in the workbench is used. If the
	 * display is null the job will not be run.
	 * 
	 * @return Display or <code>null</code>.
	 */
	public Display getDisplay() {
		if (cachedDisplay == null) {
			return Display.getCurrent();
		}
		return cachedDisplay;
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 *      Note: this message is marked final. Implementors should use
	 *      runInUIThread() instead.
	 */
	@Override
	public final IStatus run(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		final Display asyncDisplay = getDisplay();
		if (asyncDisplay == null || asyncDisplay.isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		asyncDisplay.asyncExec(new Runnable() {
			@Override
			public void run() {
				IStatus result = null;
				Throwable throwable = null;
				try {
					//As we are in the UI Thread we can
					//always know what to tell the job.
					setThread(Thread.currentThread());
					if (monitor.isCanceled()) {
						result = Status.CANCEL_STATUS;
					} else {
						result = runInUIThread(monitor);
					}
				} catch (final Exception t) {
					throwable = t;
				} finally {
					if (result == null) {
						result = new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, IStatus.ERROR, "Internal Error", throwable);
					}
					done(result);
				}
			}
		});
		return Job.ASYNC_FINISH;
	}

	/**
	 * Run the job in the UI Thread.
	 * 
	 * @param monitor
	 * @return IStatus
	 */
	public abstract IStatus runInUIThread(IProgressMonitor monitor);

	/**
	 * Sets the display to execute the asyncExec in. Generally this is not' used
	 * if there is a valid display available via
	 * PlatformUI.isWorkbenchRunning().
	 * 
	 * @param runDisplay
	 *            Display
	 * @see UiJob#getDisplay()
	 * @see PlatformUI#isWorkbenchRunning()
	 */
	public void setDisplay(final Display runDisplay) {
		Assert.isNotNull(runDisplay);
		cachedDisplay = runDisplay;
	}
}
