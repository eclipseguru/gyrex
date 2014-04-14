package org.eclipse.gyrex.eventbus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.gyrex.eventbus.IEventHandler;
import org.eclipse.gyrex.eventbus.internal.EventHandler;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType1;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType2;
import org.eclipse.gyrex.eventbus.internal.examples.Handler1And2UsingAnnotation;
import org.eclipse.gyrex.eventbus.internal.examples.Handler1ImplementingInterface;

import org.junit.Before;
import org.junit.Test;

public class EventHandlerTest {

	private void assertEquality(final EventHandler handler1, final EventHandler handler2) {
		assertEquals(handler1.hashCode(), handler1.hashCode());
		assertEquals(handler1.hashCode(), handler2.hashCode());
		assertEquals(handler2.hashCode(), handler2.hashCode());
		assertTrue(handler1.equals(handler1));
		assertTrue(handler1.equals(handler2));
		assertTrue(handler2.equals(handler1));
		assertTrue(handler2.equals(handler2));
	}

	@Test
	public void EventHandlerMethod_equality() throws Exception {
		final Handler1And2UsingAnnotation object = new Handler1And2UsingAnnotation();
		final EventHandler handler1 = new EventHandler.EventHandlerMethod(object.getClass().getMethod("handleCustomEventType1", CustomEventType1.class), CustomEventType1.class, object);
		final EventHandler handler2 = new EventHandler.EventHandlerMethod(object.getClass().getMethod("handleCustomEventType1", CustomEventType1.class), CustomEventType1.class, object);

		assertEquality(handler1, handler2);
	}

	@Test
	public void EventHandlerMethod_notequals() throws Exception {
		final Handler1And2UsingAnnotation object = new Handler1And2UsingAnnotation();
		final EventHandler handler1 = new EventHandler.EventHandlerMethod(object.getClass().getMethod("handleCustomEventType1", CustomEventType1.class), CustomEventType1.class, object);
		final EventHandler handler2 = new EventHandler.EventHandlerMethod(object.getClass().getMethod("handleCustomEventType1", CustomEventType1.class), CustomEventType2.class, object);

		assertNotEquals(handler1, handler2);
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void WrappedEventHandler_equality() throws Exception {
		final Handler1ImplementingInterface object = new Handler1ImplementingInterface();
		final EventHandler handler1 = new EventHandler.WrappedEventHandler(CustomEventType1.class, (IEventHandler) object);
		final EventHandler handler2 = new EventHandler.WrappedEventHandler(CustomEventType1.class, (IEventHandler) object);

		assertEquality(handler1, handler2);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void WrappedEventHandler_notequals() throws Exception {
		final Handler1ImplementingInterface object = new Handler1ImplementingInterface();
		final EventHandler handler1 = new EventHandler.WrappedEventHandler(CustomEventType1.class, (IEventHandler) object);
		final EventHandler handler2 = new EventHandler.WrappedEventHandler(CustomEventType2.class, (IEventHandler) object);

		assertNotEquals(handler1, handler2);
	}

}
