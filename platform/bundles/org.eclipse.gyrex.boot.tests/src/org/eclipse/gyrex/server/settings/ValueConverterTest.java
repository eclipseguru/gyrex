package org.eclipse.gyrex.server.settings;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValueConverterTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void convertBooleans() throws Exception {
		assertEquals(Boolean.TRUE, ValueConverter.convertToValueType("true", Boolean.class));
		assertEquals(Boolean.FALSE, ValueConverter.convertToValueType("false", Boolean.class));
		assertEquals(Boolean.FALSE, ValueConverter.convertToValueType("", Boolean.class));
		assertEquals(Boolean.FALSE, ValueConverter.convertToValueType("kcsajbcia", Boolean.class));
	}

	@Test
	public void convertNumbers() throws Exception {
		assertEquals(new Integer(1), ValueConverter.convertToValueType("1", Integer.class));
		assertEquals(new Long(1L), ValueConverter.convertToValueType("1", Long.class));
		assertEquals(new Double(1.2D), ValueConverter.convertToValueType("1.2", Double.class));
		assertEquals(new Float(1.2F), ValueConverter.convertToValueType("1.2", Float.class));
	}

	@Test
	public void convertStrings() throws Exception {
		assertEquals("blah", ValueConverter.convertToValueType("blah", String.class));
	}

	@Test
	public void unparsableDouble_does_throw_IllegalArgumentException() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		ValueConverter.convertToValueType("1s", Double.class);
	}

	@Test
	public void unparsableFloat_does_throw_IllegalArgumentException() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		ValueConverter.convertToValueType("1s", Float.class);
	}

	@Test
	public void unparsableInteger_does_throw_IllegalArgumentException() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		ValueConverter.convertToValueType("1s", Integer.class);
	}

	@Test
	public void unparsableLong_does_throw_IllegalArgumentException() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		ValueConverter.convertToValueType("1s", Long.class);
	}
}
