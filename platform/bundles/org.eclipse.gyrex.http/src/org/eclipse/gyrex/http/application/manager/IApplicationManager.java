/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.cloudfree.http.application.manager;

import java.net.MalformedURLException;
import java.util.Map;


import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.http.application.Application;
import org.eclipse.cloudfree.http.application.provider.ApplicationProvider;

/**
 * A management interface for CloudFree HTTP applications.
 * <p>
 * The CloudFree Platform offers applications which run in a specific context
 * (eg. a tenant specific context). Thus, multiple instances of the
 * <em>same</em> application may be running. Each instance may expose
 * differences in its behavior and functionality depending on the context it is
 * running in. The manager allows to mount applications to the OSGi HTTP
 * service(s) used by the CloudFree Platform.
 * </p>
 * <p>
 * The manager is provided as an OSGi service. The CloudFree Platform may use
 * security restrictions to limit the registration of applications.
 * </p>
 * 
 * @see Application
 * @see ApplicationProvider
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApplicationManager {

	/**
	 * Registers an application at the specified URL.
	 * <p>
	 * An URL must begin with a protocol (<code>http://</code> or
	 * <code>https://</code>) followed by a domain name and a path (eg.
	 * <code>http://shop.cloudfree.net/admin/</code>). The path may be optional.
	 * Trailing slashes will be removed. If no path is provided the "root" path
	 * (i.e. "/") will be used. Everything below the specified URL is intended
	 * to be controlled by the application.
	 * </p>
	 * <p>
	 * HTTP requests will be mapped to applications similar to how the OSGi Http
	 * Service maps requests to servlet and resource registrations, i.e. only a
	 * prefix path matching on the request URL will be performed. The longest
	 * matching path wins.
	 * </p>
	 * <p>
	 * If a URL is already in use a {@link MountConflictException} will be
	 * thrown.
	 * </p>
	 * 
	 * @param url
	 *            the url the application should be mounted on (must use
	 *            <code>http://</code> or <code>https://</code> protocol)
	 * @param applicationId
	 *            the application id
	 * @throws MountConflictException
	 *             if an application for the specified URL is already registered
	 * @throws MalformedURLException
	 *             if the specified url is invalid
	 * @see Application
	 * @see ApplicationProvider
	 */
	void mount(String url, String applicationId) throws MountConflictException, MalformedURLException;

	/**
	 * Registers an application.
	 * <p>
	 * Application instances will be created using the specified provider. The
	 * provider will be looked up from the list of provides made available as
	 * OSGi service instances when an application instance is needed. Thus, the
	 * provider does not need to be available at the time this method is
	 * invoked.
	 * </p>
	 * <p>
	 * Applications are created lazily when the first request to an application
	 * is received. They are identified by an {@link Application#getId() id}
	 * specified via <code>applicationId</code>.
	 * </p>
	 * <p>
	 * If an application id is already in use a
	 * {@link ApplicationRegistrationException} will be thrown.
	 * </p>
	 * 
	 * @param applicationId
	 *            the application id
	 * @param providerId
	 *            the provider id
	 * @param context
	 *            the context the application will operate in
	 * @param properties
	 *            application properties which may configure the application
	 *            further when
	 *            {@link Application#initialize(org.eclipse.cloudfree.http.application.service.IApplicationServiceSupport)}
	 *            is invoked
	 * @throws ApplicationRegistrationException
	 *             if an application with the specified id is already defined
	 */
	void register(String applicationId, String providerId, IContext context, Map<String, String> properties) throws ApplicationRegistrationException;

	/**
	 * Unmounts an application mounted at the specified URL.
	 * 
	 * @param url
	 *            the mount point
	 */
	void unmount(String url);

	/**
	 * Unregisters an application.
	 * 
	 * @param applicationId
	 *            the application id
	 */
	void unregister(String applicationId);
}