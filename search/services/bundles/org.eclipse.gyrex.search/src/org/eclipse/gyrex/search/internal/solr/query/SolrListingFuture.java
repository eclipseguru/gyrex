/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.service.solr.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cds.result.IListingResult;
import org.eclipse.gyrex.cds.result.IListingResultCallback;

/**
 * 
 */
public class SolrListingFuture implements Future<IListingResult>, IListingResultCallback {

	private final AtomicReference<SolrQueryJob> queryJobRef = new AtomicReference<SolrQueryJob>();
	private final AtomicBoolean canceled = new AtomicBoolean();
	private final AtomicBoolean done = new AtomicBoolean();
	private final AtomicReference<IListingResult> resultRef = new AtomicReference<IListingResult>();
	private final AtomicReference<Throwable> exceptionRef = new AtomicReference<Throwable>();
	private final AtomicReference<IListingResultCallback> callbackRef = new AtomicReference<IListingResultCallback>();
	private final CountDownLatch gotResult = new CountDownLatch(1);

	/**
	 * Creates a new instance.
	 * 
	 * @param queryJob
	 */
	public SolrListingFuture(final SolrQueryJob queryJob) {
		queryJobRef.set(queryJob);
		queryJob.setCallback(this);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		if (canceled.get() || done.get()) {
			return false;
		}
		final SolrQueryJob queryJob = queryJobRef.getAndSet(null);
		if (null != queryJob) {
			queryJob.setCallback(null);
			canceled.set(queryJob.cancel());
		}
		markDone();
		gotResult.countDown();
		return canceled.get();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#get()
	 */
	@Override
	public IListingResult get() throws InterruptedException, ExecutionException {
		gotResult.await();
		return getResultOrException();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public IListingResult get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!gotResult.await(timeout, unit)) {
			throw new TimeoutException("timeout while waiting on result");
		}
		return getResultOrException();
	}

	private IListingResult getResultOrException() throws ExecutionException {
		final Throwable throwable = exceptionRef.get();
		if (null != throwable) {
			throw new ExecutionException(throwable);
		}
		return resultRef.get();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		return canceled.get();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#isDone()
	 */
	@Override
	public boolean isDone() {
		return done.get();
	}

	private void markDone() {
		done.set(true);
		queryJobRef.set(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResultCallback#onError(java.lang.Throwable)
	 */
	@Override
	public void onError(final Throwable exception) {
		if (exceptionRef.compareAndSet(null, exception)) {
			markDone();
			gotResult.countDown();
			final IListingResultCallback callback = callbackRef.get();
			if (null != callback) {
				callback.onError(exception);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.service.result.IListingResultCallback#onResult(org.eclipse.gyrex.cds.service.result.IListingResult)
	 */
	@Override
	public void onResult(final IListingResult result) {
		if (resultRef.compareAndSet(null, result)) {
			markDone();
			gotResult.countDown();
			final IListingResultCallback callback = callbackRef.get();
			if (null != callback) {
				callback.onResult(result);
			}
		}
	}

	void setCallback(final IListingResultCallback callback) {
		callbackRef.set(callback);
	}

}
