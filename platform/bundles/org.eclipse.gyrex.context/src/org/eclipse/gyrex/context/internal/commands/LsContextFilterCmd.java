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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.internal.configuration.ContextConfiguration;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.kohsuke.args4j.Option;

/**
 * Sets a context filter
 */
public class LsContextFilterCmd extends BaseContextDefinitionCmd {

	@Option(name = "-r", aliases = { "--recursive" }, usage = "enabled recursive lookup till the root context", required = false)
	boolean recursive;

	public LsContextFilterCmd() {
		super("<path> - lists context filters");
	}

	@Override
	protected void doExecute(final ContextDefinition contextDefinition) throws Exception {
		if (!recursive) {
			listFilters(contextDefinition.getPath());
			return;
		}

		IPath path = contextDefinition.getPath();
		while ((path != null) && !path.isRoot()) {
			listFilters(path);
			printf("");
			path = path.removeLastSegments(1);
		}

		listFilters(Path.ROOT);

	}

	private void listFilters(final IPath path) {
		final Map<String, String> filters = ContextConfiguration.getFilters(path);
		if (filters.isEmpty()) {
			printf("%s:%n  no filters configured", path.toString());
			return;
		}

		printf("%s:", path);
		for (final Entry<String, String> e : new TreeMap<>(filters).entrySet()) {
			printf("  %-60s   %s", e.getKey(), e.getValue());
		}
	}
}