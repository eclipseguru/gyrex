package org.eclipse.gyrex.eventbus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.gyrex.eventbus.internal.StringDeSerializer;

import org.junit.Test;

public class StringDeSerializerTest {

	@Test
	public void rountrip() throws Exception {
		assertNotNull(StringDeSerializer.sharedInstance);
		assertEquals("test123", StringDeSerializer.sharedInstance.deserializeEvent(StringDeSerializer.sharedInstance.serializeEvent("test123")));
		assertEquals("ü?ß\u2708✈", StringDeSerializer.sharedInstance.deserializeEvent(StringDeSerializer.sharedInstance.serializeEvent("ü?ß\u2708✈")));
	}

}
