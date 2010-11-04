/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cloud.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;
import org.eclipse.gyrex.cloud.internal.zk.IZooKeeperLayout;

import org.eclipse.osgi.util.NLS;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Captures general state of the cloud.
 */
public class CloudState {

	private static final AtomicReference<NodeInfo> myInfo = new AtomicReference<NodeInfo>();

	private static ZooKeeperGate getGate() {
		return ZooKeeperGate.get();
	}

	/**
	 * Initializes the node information.
	 * 
	 * @return the node information
	 */
	private static NodeInfo initializeNodeInfo() throws Exception {
		final NodeInfo info = new NodeInfo();

		// check if there is a recored in the "approved" list
		final byte[] record = getGate().readRecord(IZooKeeperLayout.PATH_NODES_APPROVED.append(info.getNodeId()));
		if (record != null) {
			return new NodeInfo(record);
		}

		// create an ephemeral pending record
		getGate().createRecord(IZooKeeperLayout.PATH_NODES_PENDING.append(info.getNodeId()), CreateMode.EPHEMERAL, info.getLocation());

		return info;
	}

	/**
	 * Registers this node with the cloud.
	 */
	public static void registerNode() throws Exception {

		// TODO: this is wrong, the flow needs to change
		// someone should trigger a ZooKeeper connection
		// the ZooKeeper connection would trigger this when connected successfully

		// load / create our node info
		final NodeInfo nodeInfo = initializeNodeInfo();

		// set node online
		if (myInfo.compareAndSet(null, nodeInfo)) {
			setNodeOnline(nodeInfo);
		}
	}

	private static void setNodeOnline(final NodeInfo node) throws Exception {
		if (!node.isApproved()) {
			throw new IllegalArgumentException(NLS.bind("Node {0} must be approved first before it is allowed to join the cloud.", node.getNodeId()));
		}

		// ensure the persistent record containing all online nodes exist
		try {
			getGate().createPath(IZooKeeperLayout.PATH_NODES_ONLINE, CreateMode.PERSISTENT);
		} catch (final KeeperException e) {
			// its okay if the node already exists
			if (e.code() != KeeperException.Code.NODEEXISTS) {
				throw e;
			}
		}

		// create an ephemeral record for this node
		try {
			getGate().createRecord(IZooKeeperLayout.PATH_NODES_ONLINE.append(node.getNodeId()), CreateMode.EPHEMERAL, node.getLocation());
		} catch (final KeeperException e) {
			// if the node already exists something is off
			if (e.code() == KeeperException.Code.NODEEXISTS) {
				// TODO investigate this error further
				throw new IllegalStateException(NLS.bind("Unable to register node {0} in the cloud. There is already a node with that id running.", node.getNodeId()), e);
			} else {
				throw e;
			}
		}
	}

	/**
	 *
	 */
	public static void unregisterNode() {
		// TODO Auto-generated method stub

	}

}
