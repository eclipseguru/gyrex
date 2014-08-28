package org.eclipse.gyrex.server.settings;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SystemSettingBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void SystemSettingBuilder() throws Exception {
		final SystemSettingBuilder<String> settingBuilder = new SystemSettingBuilder<>(String.class);
		assertEquals(String.class, settingBuilder.type);
	}

}
