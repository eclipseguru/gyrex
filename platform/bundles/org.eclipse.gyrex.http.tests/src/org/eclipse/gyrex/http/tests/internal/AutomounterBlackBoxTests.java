package org.eclipse.gyrex.http.tests.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.SortedSet;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.http.application.Application;
import org.eclipse.gyrex.http.application.manager.IApplicationManager;
import org.eclipse.gyrex.http.application.provider.ApplicationProvider;
import org.eclipse.gyrex.http.internal.application.manager.AutomountComponent;
import org.eclipse.gyrex.http.internal.application.manager.IAutomountService;
import org.eclipse.gyrex.junit.GyrexServerResource;
import org.eclipse.gyrex.junit.OsgiResources;

import org.eclipse.core.runtime.CoreException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class AutomounterBlackBoxTests {

	static class TestApp extends Application {
		TestApp(final String id, final IRuntimeContext context) {
			super(id, context);
		}
	}

	static class TestAppProvider extends ApplicationProvider {
		TestAppProvider() {
			super(TestAppProvider.class.getSimpleName());
		}

		@Override
		public Application createApplication(final String applicationId, final IRuntimeContext context) throws CoreException {
			return new TestApp(applicationId, context);
		}
	}

	@ClassRule
	public static GyrexServerResource server = new GyrexServerResource();

	@Rule
	public OsgiResources osgi = new OsgiResources(Activator.getBundleContext());

	@Test
	public void testAutomount() {
		// create manager
		final BundleContext context = Activator.getBundleContext();

		final TestAppProvider appProvider = new TestAppProvider();

		final String applicationId = "my-app-" + System.nanoTime();
		final String url = "http:/myapp";

		final Dictionary<String, Object> propereties = new Hashtable<>();
		propereties.put(AutomountComponent.PROPERTY_AUTOMOUNT_APPLICATION_ID, applicationId);
		propereties.put(AutomountComponent.PROPERTY_AUTOMOUNT_URL, url);

		// register provider
		final ServiceRegistration<ApplicationProvider> serviceRegistration = context.registerService(ApplicationProvider.class, appProvider, propereties);

		osgi.startBundle("org.eclipse.gyrex.http");
		osgi.getService(IAutomountService.class); // this trick ensures the automount component is properly initialized

		// assert registration not null
		final IApplicationManager manager = osgi.getService(IApplicationManager.class);
		assertTrue(manager.isRegistered(applicationId));
		final SortedSet<String> mounts = manager.getMounts(applicationId);
		assertNotNull(mounts);
		assertTrue(mounts.contains(url));

		// unregister
		serviceRegistration.unregister();
	}
}
