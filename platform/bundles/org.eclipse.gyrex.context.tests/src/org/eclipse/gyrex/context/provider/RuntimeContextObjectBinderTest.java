package org.eclipse.gyrex.context.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RuntimeContextObjectBinderTest {

	private RuntimeContextObjectBinder binder;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void addBinding_adds_binding() throws Exception {
		final Class<?> anonymousClass = new Object() {
		}.getClass();

		assertTrue(binder.getBindings().isEmpty());
		binder.addBinding(Object.class, anonymousClass);
		final Map<Class<?>, Class<?>> bindings = binder.getBindings();
		assertFalse(bindings.isEmpty());
		assertEquals(1, bindings.size());

		final Entry<Class<?>, Class<?>> binding = bindings.entrySet().iterator().next();
		assertSame(Object.class, binding.getKey());
		assertSame(anonymousClass, binding.getValue());
	}

	@Test
	public void bindingbuilder_toImplementationClass_fails_for_null_argument() throws Exception {
		exception.expect(IllegalArgumentException.class);
		binder.bindType(Object.class).toImplementationClass(null);
	}

	@Test
	public void bindType_fails_for_null_type() throws Exception {
		exception.expect(IllegalArgumentException.class);
		binder.bindType(null);
	}

	@Test
	public void bindType_returns_BindingBuilder() throws Exception {
		assertNotNull(binder.bindType(Object.class));
	}

	@Test
	public void getBindings_returns_never_null() throws Exception {
		assertNotNull(binder.getBindings());
		binder.addBinding(Object.class, Object.class);
		assertNotNull(binder.getBindings());
	}

	@Before
	public void setupNewBinder() {
		binder = new RuntimeContextObjectBinder();
	}

	@Test
	public void toImplementationClass_adds_binding() throws Exception {
		assertTrue(binder.getBindings().isEmpty());
		new RuntimeContextObjectBinder.BindingBuilder<Object>(Object.class, binder).toImplementationClass(getClass());

		final Map<Class<?>, Class<?>> bindings = binder.getBindings();
		assertFalse(bindings.isEmpty());
		assertEquals(1, bindings.size());

		final Entry<Class<?>, Class<?>> binding = bindings.entrySet().iterator().next();
		assertSame(Object.class, binding.getKey());
		assertSame(getClass(), binding.getValue());
	}
}
