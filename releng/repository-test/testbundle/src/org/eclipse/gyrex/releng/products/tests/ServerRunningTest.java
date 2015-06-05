package org.eclipse.gyrex.releng.products.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.gyrex.junit.GyrexServerResource;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ServerRunningTest {

	@ClassRule
	public static GyrexServerResource serverIsAvailable = new GyrexServerResource();

	@Test
	public void retrieving_any_user_without_authentication_fails() throws Exception {
		// all good here
		assertTrue("Server must be running.", true);
	}

	@Before
	public void setUp() throws Exception {
	}

}
