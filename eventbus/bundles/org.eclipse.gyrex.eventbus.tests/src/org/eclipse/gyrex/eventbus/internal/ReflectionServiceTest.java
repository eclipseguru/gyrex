package org.eclipse.gyrex.eventbus.internal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.gyrex.eventbus.IEventHandler;
import org.eclipse.gyrex.eventbus.internal.EventHandler;
import org.eclipse.gyrex.eventbus.internal.ReflectionService;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType1;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType1SerializerAndDeserializerVariant2;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType2;
import org.eclipse.gyrex.eventbus.internal.examples.Handler1And2UsingAnnotation;
import org.eclipse.gyrex.eventbus.internal.examples.Handler1ImplementingInterface;
import org.eclipse.gyrex.eventbus.internal.examples.Handler1ImplementingInterfaceAndUsingAnnotations;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableCollection;

public class ReflectionServiceTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private ReflectionService service;

	@Test
	public void clearCaches() throws Exception {
		final Set<Class<?>> hierarchy = service.getHierarchy(CustomEventType1.class);
		final Set<Class<?>> cachedValue = service.classHierarchyByClassCache.getIfPresent(CustomEventType1.class);
		assertNotNull(cachedValue);
		assertSame(hierarchy, cachedValue);

		service.clearCaches();
		assertNull(service.classHierarchyByClassCache.getIfPresent(CustomEventType1.class));
		assertTrue(service.classHierarchyByClassCache.size() == 0);
	}

	@Test
	public void getEventHandlersFromIEventHandler() throws Exception {
		testOneIEventHandlerDiscoveredForEventType(new Handler1ImplementingInterface(), CustomEventType1.class);
		testOneIEventHandlerDiscoveredForEventType(new Handler1ImplementingInterfaceAndUsingAnnotations(), CustomEventType1.class);
		testNoIEventHandlerDiscoveredForEventType(new Handler1ImplementingInterface(), CustomEventType2.class);
		testNoIEventHandlerDiscoveredForEventType(new Handler1ImplementingInterfaceAndUsingAnnotations(), CustomEventType2.class);
	}

	@Test
	public void getEventHandlersFromObjectMethods_Handler1And2UsingAnnotation() throws Exception {
		final Handler1And2UsingAnnotation object = new Handler1And2UsingAnnotation();
		final Set<EventHandler> handlers = service.getEventHandlersFromObjectMethods(object);
		assertNotNull(handlers);
		assertEquals(2, handlers.size());
		final EventHandler handler1 = EventHandler.of(CustomEventType1.class, object.getClass().getMethod("handleCustomEventType1", CustomEventType1.class), object);
		final EventHandler handler2 = EventHandler.of(CustomEventType2.class, object.getClass().getMethod("handleCustomEventType2", CustomEventType2.class), object);
		assertThat(handlers, hasItem(handler1));
		assertThat(handlers, hasItem(handler2));
	}

	@Test
	public void getHierarchy_containsSpecifiedType() throws Exception {
		assertTrue(service.getHierarchy(ReflectionServiceTest.class).contains(ReflectionServiceTest.class));
		assertTrue(service.getHierarchy(CustomEventType1.class).contains(CustomEventType1.class));
		assertTrue(service.getHierarchy(CustomEventType2.class).contains(CustomEventType2.class));
		assertTrue(service.getHierarchy(CustomEventType2.class).contains(CustomEventType1.class));
	}

	@Test
	public void getHierarchy_mustNotContainTypeToIgnore() throws Exception {
		assertFalse(service.getHierarchy(ReflectionServiceTest.class).contains(Object.class));
		assertFalse(service.getHierarchy(CustomEventType1.class).contains(Object.class));
		assertFalse(service.getHierarchy(CustomEventType2.class).contains(Object.class));
	}

	@Test
	public void getHierarchy_resultNotModifieable() throws Exception {
		final Set<Class<?>> hierarchy = service.getHierarchy(CustomEventType1.class);
		assertNotNull(hierarchy);
		assertTrue(hierarchy instanceof ImmutableCollection);

		exception.expect(UnsupportedOperationException.class);
		hierarchy.clear();
	}

	@Test
	public void getHierarchy_specifiedTypeIsFirst() throws Exception {
		assertSame(ReflectionServiceTest.class, service.getHierarchy(ReflectionServiceTest.class).iterator().next());
		assertSame(CustomEventType1.class, service.getHierarchy(CustomEventType1.class).iterator().next());
		assertSame(CustomEventType2.class, service.getHierarchy(CustomEventType2.class).iterator().next());
		assertSame(CustomEventType1SerializerAndDeserializerVariant2.class, service.getHierarchy(CustomEventType1SerializerAndDeserializerVariant2.class).iterator().next());
	}

	@Test
	public void isAllowedEventType() throws Exception {
		assertTrue(service.isAllowedEventType(CustomEventType1.class));
		assertTrue(service.isAllowedEventType(CustomEventType2.class));
		assertFalse(service.isAllowedEventType(Object.class));
	}

	@Before
	public void setUp() throws Exception {
		service = new ReflectionService();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testNoIEventHandlerDiscoveredForEventType(final IEventHandler<?> object, final Class<?> eventType) {
		final Set<EventHandler> handlers = service.getEventHandlersFromIEventHandler((IEventHandler) object);
		assertNotNull(handlers);
		assertThat(handlers, not(hasItem(EventHandler.of(eventType, (IEventHandler) object))));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testOneIEventHandlerDiscoveredForEventType(final IEventHandler<?> object, final Class<?> eventType) {
		final Set<EventHandler> handlers = service.getEventHandlersFromIEventHandler((IEventHandler) object);
		assertNotNull(handlers);
		assertEquals(1, handlers.size());
		assertThat(handlers, hasItem(EventHandler.of(eventType, (IEventHandler) object)));
	}
}
