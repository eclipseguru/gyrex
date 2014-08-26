/*******************************************************************************
 * Copyright (c) 2014 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Konrad Schergaut - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.server.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.server.settings.SystemSetting;

import org.junit.After;
import org.junit.Test;

/**
 *
 */
public class SystemSettingTest {

	public static final String PROPERTY_NAME = "systemSettingTestProperty";

	@After
	public void cleanUpSettings() {
		//avoid an ongoing effect on other test
		System.clearProperty(PROPERTY_NAME);
	}

	/*
	 * A test for reading from the environment is missing here due to over
	 * complicated setup-procedures.
	 */

	@Test
	public void testGetOrFail() {
		final String value = "sample";

		//we just use a simple String property here for the sake of simplicity
		final SystemSetting<Integer> setting = SystemSetting.newIntegerSetting(PROPERTY_NAME, "testingPurposes").create();

		System.setProperty(PROPERTY_NAME, value);

		try {
			setting.getOrFail();
			fail();
		} catch (final IllegalArgumentException e) {

		}

		System.setProperty(PROPERTY_NAME, "1");
		assertEquals(Integer.valueOf(1), setting.get());
	}

	@Test
	public void testReadList() {
		final SystemSetting<List<String>> setting = SystemSetting.newMultiValueStringSetting(PROPERTY_NAME, "testingPurposes").create();

		System.setProperty(PROPERTY_NAME, "a");

		final List<String> values = new ArrayList<>();
		values.add("a");
		assertEquals(values, setting.get());

		System.setProperty(PROPERTY_NAME, "a,b,c");

		values.add("b");
		values.add("c");
		assertEquals(values, setting.get());
	}

	@Test
	public void testReadSystem() {
		//we just use a simple String property here for the sake of simplicity
		final SystemSetting<String> setting = SystemSetting.newStringSetting(PROPERTY_NAME, "testingPurposes").create();

		assertEquals(null, setting.get());

		final String value = "sample";
		System.setProperty(PROPERTY_NAME, value);

		assertEquals(value, setting.get());
	}

	@Test
	public void testUseDefault() {
		final SystemSetting<Long> setting = SystemSetting.newLongSetting(PROPERTY_NAME, "testingPurposes").usingDefault(Long.valueOf(321l)).create();
		assertEquals(Long.valueOf(321l), setting.get());
	}

}
