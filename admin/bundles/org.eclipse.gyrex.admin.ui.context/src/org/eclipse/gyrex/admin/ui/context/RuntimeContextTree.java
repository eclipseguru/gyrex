/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.context;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.gyrex.admin.ui.context.internal.ContextUiActivator;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.TreeNode;

public class RuntimeContextTree {

	private static TreeNode findParent(final TreeNode parent, final TreeNode next) {
		if (!getPath(parent).isPrefixOf(getPath(next)))
			return null;

		final TreeNode[] children = parent.getChildren();
		if (null == children) {
			// no existing children, assume first
			parent.setChildren(new TreeNode[] { next });
			return parent;
		}

		// need to make sure that the node is not a child of any existing children
		for (final TreeNode child : children) {
			final TreeNode result = findParent(child, next);
			if (null != result)
				return result;
		}

		// 'next' is a sibling of existing children
		final TreeNode[] newChildren = new TreeNode[children.length + 1];
		System.arraycopy(children, 0, newChildren, 0, children.length);
		newChildren[children.length] = next;
		parent.setChildren(newChildren);
		return parent;
	}

	private static IPath getPath(final TreeNode node) {
		return ((ContextDefinition) node.getValue()).getPath();
	}

	public static TreeNode[] getTree() {
		final IRuntimeContextDefinitionManager registry = ContextUiActivator.getInstance().getService(IRuntimeContextDefinitionManager.class);

		// collect based on path name (ascending; makes tree building easier)
		final SortedMap<IPath, TreeNode> nodes = new TreeMap<IPath, TreeNode>(new Comparator<IPath>() {
			@Override
			public int compare(final IPath o1, final IPath o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		// create tree nodes for every context
		for (final ContextDefinition contextDefinition : registry.getDefinedContexts()) {
			nodes.put(contextDefinition.getPath(), new TreeNode(contextDefinition));
		}

		// the root is first
		if (!nodes.firstKey().isRoot())
			throw new IllegalStateException("sort error");

		// wire tree together (starting at root)
		final TreeNode root = nodes.remove(nodes.firstKey());
		for (final Iterator stream = nodes.values().iterator(); stream.hasNext();) {
			final TreeNode next = (TreeNode) stream.next();
			stream.remove();
			final TreeNode parent = findParent(root, next);
			if (null == parent)
				throw new IllegalStateException("build tree error");
		}

		return new TreeNode[] { root };
	}

}
