/*******************************************************************************
 * Copyright (c) 2011, 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.context.internal.commands;

import org.eclipse.gyrex.common.internal.services.ServiceProxy;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.internal.configuration.ContextConfiguration;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;

import org.apache.commons.lang.StringUtils;

import org.kohsuke.args4j.Argument;

/**
 * Sets or unsets a context filter
 */
@SuppressWarnings("restriction")
public class SetContextFilterCmd extends BaseContextDefinitionCmd {

	@Argument(index = 1, usage = "type name of the class a filter applies to", required = true, metaVar = "CLASS")
	String typeName;

	@Argument(index = 2, usage = "the filter (leave empty for unset)", required = false, metaVar = "FILTER")
	String filter;

	public SetContextFilterCmd() {
		super("<path> <class> [<filter>] - sets or unsets a context filter");
	}

	@Override
	protected void doExecute(final ContextDefinition contextDefinition) throws Exception {
		if (StringUtils.isNotBlank(filter)) {
			ServiceProxy.verifyFilterContainsObjectClassConditionForServiceInterface(typeName, filter);
			final Filter parsedFilter = FrameworkUtil.createFilter(filter);
			printf("succeddfully verified filter: %s", parsedFilter);
		}
		ContextConfiguration.setFilter(contextDefinition.getPath(), typeName, StringUtils.trimToNull(filter));
		printf("updated filter configuration");
	}
}