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

import java.util.List;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.configuration.ConfigurationPerspective;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.dialogs.FilteredTree;

/**
 * Provides all necessary informations for configuration pages to be displayed
 * in the {@link ConfigurationPerspective}
 */
public interface IContentProviderNode {

	/**
	 * Used to add an {@link IContentProviderNode} to this category.
	 * 
	 * @param contentProviderNode
	 */
	public void addChild(IContentProviderNode contentProviderNode);

	/**
	 * Creates a new {@link ConfigurationPage} instance.
	 * 
	 * @return the created page
	 * @throws CoreException
	 */
	public ConfigurationPage createPage() throws CoreException;

	/**
	 * Getter for an <b>unmodifiable</b> list of it's related
	 * {@link IContentProviderNode}. The list may be empty but is never
	 * <code>null</code>.
	 * 
	 * @return the children list
	 */
	public List<IContentProviderNode> getChildren();

	/**
	 * @return a unique identifier
	 */
	public String getId();

	/**
	 * The keyword list will be used for the dynamic node search in a
	 * {@link FilteredTree} as optional addition to the name of the node.
	 * <p>
	 * May be empty but must not be <code>null</code>
	 * 
	 * @return the keyword list
	 */
	public List<String> getKeywords();

	/**
	 * Should return any human readable name for the content provider and must
	 * not be <code>null</code>.
	 * <p>
	 * The name will be displayed in a tree structure and is also used for the
	 * dynamic node search in a {@link FilteredTree}
	 * 
	 * @return the name of this node
	 */
	public String getName();

	/**
	 * @return the id of the parent element
	 */
	public String getParentId();

}
