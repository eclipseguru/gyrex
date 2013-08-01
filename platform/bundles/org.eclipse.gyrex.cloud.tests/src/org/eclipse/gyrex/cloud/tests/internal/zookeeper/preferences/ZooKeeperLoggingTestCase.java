/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Konrad Schergaut - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cloud.tests.internal.zookeeper.preferences;

import org.apache.zookeeper.JUnit4ZKTestRunner;
import org.apache.zookeeper.ZKTestCase;

import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similar to {@link ZKTestCase} but with usage of TestRule rather then method
 * rule.
 */
@RunWith(JUnit4ZKTestRunner.class)
public class ZooKeeperLoggingTestCase {
	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperLoggingTestCase.class);

	@Rule
	public TestRule watchman = new TestWatcher() {

		@Override
		protected void failed(final Throwable e, final Description description) {
			LOG.info("FAILED " + getReadableTestName(description), e);
		}

		@Override
		protected void finished(final Description description) {
			LOG.info("FINISHED " + getReadableTestName(description));
		}

		private String getReadableTestName(final Description description) {
			String testName = description.getMethodName();
			if (testName == null) {
				testName = description.getDisplayName();
			}
			return testName;
		}

		@Override
		protected void skipped(final AssumptionViolatedException e, final Description description) {
			LOG.info("SKIPPED " + getReadableTestName(description), e);
		}

		@Override
		protected void starting(final Description description) {
			LOG.info("STARTING " + getReadableTestName(description));
		}

		@Override
		protected void succeeded(final Description description) {
			LOG.info("SUCCEEDED " + getReadableTestName(description));
		}

	};

}
