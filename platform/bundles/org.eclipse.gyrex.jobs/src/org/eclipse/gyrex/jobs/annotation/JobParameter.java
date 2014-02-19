package org.eclipse.gyrex.jobs.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import org.eclipse.gyrex.jobs.IJobContext;

/**
 * {@linkplain Qualifier} for injecting {@linkplain IJobContext#getParameter()
 * job parameter}.
 * <p>
 * Note, the object to inject must be of type {@link String}.
 * 
 * <pre>
 * &#064;Inject
 * &#064;JobParameter(&quot;location&quot;)
 * String location;
 * </pre>
 * 
 * </p>
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface JobParameter {

	/** Parameter name (must not be <code>null</code>) */
	String value();
}
