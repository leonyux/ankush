/*******************************************************************************
 * ===========================================================
 * Ankush : Big Data Cluster Management Solution
 * ===========================================================
 * 
 * (C) Copyright 2014, by Impetus Technologies
 * 
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (LGPL v3) as
 * published by the Free Software Foundation;
 * 
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this software; if not, write to the Free Software Foundation, 
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ******************************************************************************/
package com.impetus.ankush2.framework.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.impetus.ankush.AppStoreWrapper;
import com.impetus.ankush.common.domain.Cluster;
import com.impetus.ankush.common.domain.Node;
import com.impetus.ankush.common.domain.Operation;
import com.impetus.ankush2.constant.Constant;
import com.impetus.ankush2.db.DBClusterManager;
import com.impetus.ankush2.db.DBNodeManager;
import com.impetus.ankush2.db.DBOperationManager;
import com.impetus.ankush2.framework.config.AlertsConf;
import com.impetus.ankush2.framework.config.ClusterConfig;
import com.impetus.ankush2.framework.config.NodeConfig;
import com.impetus.ankush2.logger.AnkushLogger;

public class DatabaseUtils {
	private static AnkushLogger logger = new AnkushLogger(DatabaseUtils.class);

	public void addClusterOperation(ClusterConfig clusterConf,
			Constant.Cluster.Operation operation) {
		// Can be used during deploy operation
		addClusterOperation(clusterConf, operation, clusterConf.getCreatedBy());
	}

	// 添加一个操作
	public void addClusterOperation(ClusterConfig clusterConf,
			Constant.Cluster.Operation operation, String loggedInUser) {
		Operation dbOperation = new Operation();
		dbOperation.setClusterId(clusterConf.getClusterId());
		dbOperation.setOperationId(clusterConf.getOperationId());
		dbOperation.setOpName(operation.toString());
		dbOperation.setStartedAt(new Date());
		dbOperation.setStartedBy(loggedInUser);
		dbOperation.setStatus(Constant.Operation.Status.INPROGRESS.toString());
		// 保存操作，并更新状态
		if (new DBOperationManager().saveOperation(dbOperation) != null) {
			updateOperationProgress(clusterConf);
		}
	}

	public void updateClusterOperation(ClusterConfig clusterConf) {
		// 获取操作信息
		Operation operation = new DBOperationManager()
				.getOperation(clusterConf);
		if (operation == null) {
			logger.error("Could not find operation.");
			return;
		}
		// 更新操作完成时间
		operation.setCompletedAt(new Date());
		// 根据集群状态更新操作状态
		if (clusterConf.getState() != Constant.Cluster.State.ERROR) {
			operation.setStatus(Constant.Operation.Status.COMPLETED.toString());
		} else {
			operation.setStatus(Constant.Operation.Status.ERROR.toString());
			if (clusterConf.getProgress() != null) {
				clusterConf.getProgress().setState(
						Constant.Operation.Status.ERROR);
			}
		}
		// 更新操作信息
		updateOperationProgress(clusterConf);
		// 保存操作信息，貌似不需要，更新操作时已做保存
		new DBOperationManager().saveOperation(operation);
	}

	// Update operation data
	public void updateOperationData(ClusterConfig clusterConf, String key,
			Object value) {
		if (value == null) {
			return;
		}
		// 获取cluster最近的操作，cluster一次只允许一个操作执行
		Operation operation = new DBOperationManager()
				.getOperation(clusterConf);
		if (operation == null) {
			logger.error("Could not find operation.");
			return;
		}
		// 获取操作数据，从字节数组反序列化得到
		HashMap<String, Object> opData = operation.getData();
		if (opData == null) {
			opData = new HashMap<String, Object>();
		}
		// 保存操作状态
		opData.put(key, value);
		operation.setData(opData);
		// 保存操作
		new DBOperationManager().saveOperation(operation);
	}

	// Update operation progress
	public void updateOperationProgress(ClusterConfig clusterConf) {
		updateOperationData(clusterConf, "progress", clusterConf.getProgress());
	}

	// Save cluster details into database.
	// 保存集群配置入数据库
	public String saveCluster(ClusterConfig clusterConf) {
		logger.info("Saving cluster configuration into database.");

		// Create and save new cluster
		// 获取数据库中保存的cluster信息为cluster实例
		Cluster cluster = new DBClusterManager().getCluster(clusterConf
				.getName());
		// 如果当前数据库中不存在该cluster，创建一个新的cluster实例
		// 数据库中保存的cluster信息有：名字，创建日期，创建者，告警配置，运行的服务，以及集群id（以创建时的时间毫秒数），集群状态，序列化后的clusterconf以及AgentBuildVersion
		if (cluster == null) {
			cluster = new Cluster();
			cluster.setName(clusterConf.getName());
			cluster.setCreatedAt(new Date());
			cluster.setUser(clusterConf.getCreatedBy());
			cluster.setAlertConf(new AlertsConf());
			cluster.setTechnology(clusterConf.getTechnology());
			// setting current milliseconds as Id of the cluster
			cluster.setId(System.currentTimeMillis());
		}
		cluster.setState(clusterConf.getState().toString());
		cluster.setClusterConf(clusterConf);
		cluster.setAgentVersion(AppStoreWrapper.getAgentBuildVersion());
		// save to database
		// 保存集群信息到数据库
		cluster = new DBClusterManager().saveCluster(cluster);
		if (cluster == null) {
			return "Could not save cluster details.";
		}

		clusterConf.setClusterId(cluster.getId());

		// Create and save nodes
		return saveNodes(clusterConf, clusterConf.getNodes().values());
	}

	// update nodes into database
	// 保存节点信息到数据库
	public String saveNodes(ClusterConfig clusterConf,
			Collection<NodeConfig> nodeConfList) {
		// agent build version

		String agentBuildVersion = AppStoreWrapper.getAgentBuildVersion();
		for (NodeConfig nodeConf : nodeConfList) {
			Node node = new DBNodeManager().getNode(nodeConf.getHost());
			if (node == null) {
				node = new Node();
				node.setCreatedAt(new Date());
				node.setRackInfo(nodeConf.getRack());
				node.setClusterId(clusterConf.getClusterId());
				node.setPrivateIp(nodeConf.getPublicHost());
				node.setPublicIp(nodeConf.getHost());
				// setting current milliseconds as Id of the node
				node.setId(System.currentTimeMillis());
			}
			if (nodeConf.getState() == null) {
				node.setState(Constant.Node.State.DEPLOYING.toString());
				nodeConf.setState(Constant.Node.State.DEPLOYING);
			} else {
				node.setState(nodeConf.getState().toString());
			}
			node.setNodeConfig(nodeConf);
			node.setAgentVersion(agentBuildVersion);
			node = new DBNodeManager().saveNode(node);
			if (node == null) {
				return "Could not save node details.";
			}
			nodeConf.setId(node.getId());
		}
		return null;
	}

	public Set<String> validateCluster(ClusterConfig clusterConf,
			boolean isCreated) {
		return validateCluster(clusterConf, clusterConf, isCreated);
	}

	// Check cluster name and hosts into database
	public Set<String> validateCluster(ClusterConfig clusterConf,
			ClusterConfig newClusterConf, boolean isCreated) {
		logger.info("Cluster level validation.");
		Set<String> errors = new LinkedHashSet<String>();

		// Check for cluster name
		// 如果isCreated标记为false，检查数据库中是否已存在集群配置
		if (!isCreated
				&& new DBClusterManager().getCluster(clusterConf.getName()) != null) {
			errors.add("Cluster '" + clusterConf.getName()
					+ "'  already exist.");
			return errors;
		}

		// Check hosts
		// 检查主机是否已经在集群中
		for (String host : newClusterConf.getNodes().keySet()) {
			Node node = new DBNodeManager().getNode(host);
			// continue if node does not exist
			if (node == null) {
				continue;
			}
			// continue if cluster is created and node is with in the cluster
			if (isCreated
					&& node.getClusterId().equals(clusterConf.getClusterId())) {
				continue;
			}
			errors.add("Node '" + host + "' is already in use.");
		}

		return errors;
	}

	public void removeNodes(Collection<String> nodeList) {
		for (String host : nodeList) {
			new DBNodeManager().remove(host);
		}
	}
}
