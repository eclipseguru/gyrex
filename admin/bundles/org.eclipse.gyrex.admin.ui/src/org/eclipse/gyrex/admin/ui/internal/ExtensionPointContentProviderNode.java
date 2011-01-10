/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Implementation of {@link IContentProviderNode} that's input is an
 * {@link IConfigurationElement} of the extension point
 * <code>org.eclipse.gyrex.admin.ui.configurationPages</code>
 */
public class ExtensionPointContentProviderNode implements IContentProviderNode {

	class NodeComparator implements Comparator<IContentProviderNode> {

		@Override
		public int compare(final IContentProviderNode o1, final IContentProviderNode o2) {
			if ((o1 != null) && (o2 != null)) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
			return 0;
		}
	}

	private final Comparator<? super IContentProviderNode> nodeComparator = new ExtensionPointContentProviderNode.NodeComparator();

	IConfigurationElement configurationElement;

	private final List<IContentProviderNode> children;

	public ExtensionPointContentProviderNode(final IConfigurationElement configurationElement) {
		this.configurationElement = configurationElement;
		children = new Vector<IContentProviderNode>();
	}

	@Override
	public void addChild(final IContentProviderNode contentProviderNode) {
		children.add(contentProviderNode);
		Collections.sort(children, nodeComparator);
	}

	@Override
	public ConfigurationPage createPage() throws CoreException {
		return (ConfigurationPage) configurationElement.createExecutableExtension("class");
	}

	@Override
	public boolean equals(final Object provider) {
		return provider instanceof IContentProviderNode ? getId().equals(((IContentProviderNode) provider).getId()) : Boolean.FALSE;
	}

	@Override
	public List<IContentProviderNode> getChildren() {
		return children;
	}

	@Override
	public String getId() {
		return configurationElement.getAttribute("id");
	}

	@Override
	public List<String> getKeywords() {
		final String keywords = configurationElement.getAttribute("name");
		final List<String> keywordList = new Vector<String>();
		if (keywords != null) {
			final StringTokenizer tok = new StringTokenizer(keywords, " ");
			while (tok.hasMoreTokens()) {
				keywordList.add(tok.nextToken());
			}
		}
		return keywordList;
	}

	@Override
	public String getName() {
		return configurationElement.getAttribute("name");
	}

	@Override
	public String getParentId() {
		return configurationElement.getAttribute("parentId");
	}

	public boolean isToplevel() {
		return getParentId() == null;
	}
}
