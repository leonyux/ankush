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
package com.impetus.ankush2.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.impetus.ankush.AppStoreWrapper;
import com.impetus.ankush.common.domain.Node;
import com.impetus.ankush.common.exception.AnkushException;
import com.impetus.ankush.common.scripting.AnkushTask;
import com.impetus.ankush.common.scripting.impl.Copy;
import com.impetus.ankush.common.scripting.impl.MakeDirectory;
import com.impetus.ankush.common.scripting.impl.Remove;
import com.impetus.ankush.common.scripting.impl.Untar;
import com.impetus.ankush.common.utils.CommonUtil;
import com.impetus.ankush2.constant.Constant;
import com.impetus.ankush2.framework.config.ClusterConfig;
import com.impetus.ankush2.framework.config.NodeConfig;
import com.impetus.ankush2.utils.SSHUtils;

public class AgentNodeUpgrader extends AgentUpgrader {

	private static final String NEW_AGENT_FOLDER = NODE_ANKUSH_HOME
			+ "/.newagent/agent";
	private static final String AGENT_BACKUP_FOLDER = NODE_ANKUSH_HOME
			+ "/agentBackup";
	private static final String NEW_AGENT_TAR = NODE_ANKUSH_HOME
			+ AgentDeployer.AGENT_BUNDLE_NAME;

	private static final String UPGRADE_SCRIPT_PATH = "scripts/agent/upgrade.sh";

	private static final String RESOURCE_BUNDLE_PATH = "scripts/agent/"
			+ AgentDeployer.AGENT_BUNDLE_NAME;

	private Node node;
	private NodeConfig nodeConfig;
	private ClusterConfig clusterConfig;

	/**
	 * @param node
	 * @param cluster
	 * @param clusterConf
	 */
	public AgentNodeUpgrader(Node node, ClusterConfig clusterConfig) {
		super();
		this.node = node;
		this.nodeConfig = node.getNodeConfig();
		this.clusterConfig = clusterConfig;
	}

	/**
	 * Method to Upgrade Agent on node.
	 */
	public void upgradeAgent() {
		String host = nodeConfig.getHost();
		LOGGER.setCluster(clusterConfig);
		LOGGER.info(MESSAGE_UPGRADING_AGENT + ": " + nodeConfig.getHost(),
				Constant.Component.Name.AGENT, host);
		// connection
		SSHExec connection = null;
		boolean isRollbackRequired = false;
		try {
			// getting installed Agent version
			String installedAgentVersion = node.getAgentVersion();
			if (agentBuildVersion.equals(installedAgentVersion)) {
				return;
			}

			// connecting to node.
			// 获取该节点的ssh连接
			connection = SSHUtils.connectToNode(host,
					clusterConfig.getAuthConf());
			if (connection == null) {
				throw new AnkushException("Could not connect to node: " + host);
			}
			// stopping Agent and taking Agent's backup
			stoppingAgentWithBackup(host, connection);

			System.out
					.println("Stopping Agent and taking Agent's backup done ");

			LOGGER.info("Stopping Agent and taking Agent's backup done",
					Constant.Component.Name.AGENT, host);

			// creating upgrade script directory
			LOGGER.info("Creating Agent upgrade directory.",
					Constant.Component.Name.AGENT, host);
			//
			String upgradeScriptDirectory = AgentUpgrader.NODE_ANKUSH_HOME
					+ "upgrade/";
			// creating Agent upgrade directory
			createUpgradeDirectory(connection, upgradeScriptDirectory);

			// upload Agent upgrade script

			// setting isRollbackRequired to true to do rollback in
			// AnkushException catch clause after backup is done.
			isRollbackRequired = true;

			// Uploading the agent jar to node
			LOGGER.info("Copying Agent bundle...",
					Constant.Component.Name.AGENT, host);
			// 上传agent.tar.gz包至目标路径
			uploadBundle(connection, AppStoreWrapper.getResourcePath()
					+ RESOURCE_BUNDLE_PATH, NODE_ANKUSH_HOME);

			// extracting new Agent
			extractNewAgent(connection);

			// uploading upgrade.sh to node
			// 将升级脚本上传到upgrade目录下
			String updateScriptPath = AppStoreWrapper.getResourcePath()
					+ UPGRADE_SCRIPT_PATH;
			// node update script path.
			String nodeUpdateScriptPath = upgradeScriptDirectory
					+ FilenameUtils.getName(updateScriptPath);
			uploadBundle(connection, updateScriptPath, nodeUpdateScriptPath);

			// running agent ugrade script
			if (!executeUpgradeScript(connection, upgradeScriptDirectory)) {
				throw new AnkushException(
						"Please run the upgrade scripts available under "
								+ CommonUtil.getUserHome(clusterConfig
										.getAuthConf().getUsername())
								+ ".ankush/upgrade/ directory to upgrade the agent manually and restart Agent.");
			}

			// TODO: Component wise upgrade changes

			// Removing upgrade and backup directory after successful upgrade
			// 成功升级后,移除upgrade目录和备份目录
			removeUpgradeAndBackupDir(connection, upgradeScriptDirectory);

			// starting Agent
			// 启动agent
			startAgent(connection);
			// 更新node以及nodeconfig状态
			node.setAgentVersion(agentBuildVersion);
			nodeConfig.setStatus(true);

		} catch (AnkushException e) {
			LOGGER.error(e.getMessage(), Constant.Component.Name.AGENT, host, e);
			nodeConfig.setStatus(false);
			if (isRollbackRequired) {
				rollBack(connection);
			}
		} finally {
			// disconnecting node
			// 最后关闭该节点的连接
			if (connection != null) {
				connection.disconnect();
			}
		}
		// saving node with status true/false in nodeConfig object
		// 保存更新的节点信息到数据库node表
		node.setNodeConfig(nodeConfig);
		nodeManager.save(node);
	}

	private void stoppingAgentWithBackup(String host, SSHExec connection)
			throws AnkushException {
		LOGGER.info("Stopping Ankush " + Constant.Component.Name.AGENT
				+ " and taking its backup.", Constant.Component.Name.AGENT,
				host);
		String errMsg = "Could not take Agent Backup.";
		try {
			// Stopping Ankush Agent
			// 执行agent停止脚本
			CustomTask killProcess = new ExecCommand("sh "
					+ clusterConfig.getAgentHomeDir()
					+ AgentConstant.Relative_Path.STOP_SCRIPT);
			// 不检查是否成功?
			connection.exec(killProcess);

			// backup old existing agent folder.
			// 备份目前的agent目录
			Copy backUpAgent = new Copy(clusterConfig.getAgentHomeDir(),
					AGENT_BACKUP_FOLDER, true);
			if (connection.exec(backUpAgent).rc != 0) {
				throw new AnkushException("Could not take Agent backup.");
			}
		} catch (TaskExecFailException e) {
			throw new AnkushException(errMsg);
		} catch (Exception e) {
			throw new AnkushException(errMsg);
		}
	}

	private void uploadBundle(SSHExec connection, String source,
			String destination) throws AnkushException {
		// Uploading the jar files to node
		try {
			connection.uploadSingleDataToServer(source, destination);
		} catch (Exception e) {
			throw new AnkushException("Could not upload bundle: " + source);
		}
	}

	private void createUpgradeDirectory(SSHExec connection, String source)
			throws AnkushException {
		try {
			if (connection.exec(new MakeDirectory(source)).rc != 0) {
				throw new AnkushException(
						"Could not create Agent upgrade directory.");
			}
		} catch (TaskExecFailException e) {
			throw new AnkushException(
					"Could not execute task for creating Agent upgrade directory.");
		}
	}

	private void extractNewAgent(SSHExec connection) throws AnkushException {
		try {
			// 创建新目录存放解压后的agent
			AnkushTask ankushTask = new MakeDirectory(NEW_AGENT_FOLDER);
			if (connection.exec(ankushTask).rc != 0) {
				throw new AnkushException(
						"Could not create directory for new agent bundle.");
			}

			// 将agent解压到NEW_AGENT_FOLDER目录下
			ankushTask = new Untar(NEW_AGENT_TAR, NEW_AGENT_FOLDER, false);
			if (connection.exec(ankushTask).rc != 0) {
				throw new AnkushException("Could not extract agent bundle.");
			}
		} catch (TaskExecFailException e) {
			throw new AnkushException(
					"Could not execute task for extracting agent bundle.");
		}
	}

	// 执行升级脚本
	private boolean executeUpgradeScript(SSHExec connection,
			String upgradeScriptDirectory) {
		String componentName = Constant.Component.Name.AGENT;
		String host = nodeConfig.getHost();
		try {
			String updateScriptPath = AppStoreWrapper.getResourcePath()
					+ UPGRADE_SCRIPT_PATH;
			// Getting the dependency file name for
			// the fetched OS name
			File file = new File(updateScriptPath);
			// if file exists.
			if (file.exists()) {
				LOGGER.info("Copying Agent upgrade script path to node: "
						+ nodeConfig.getHost(), Constant.Component.Name.AGENT,
						nodeConfig.getHost());
				// List of upgrade commands
				List<String> upgradeCommands = FileUtils.readLines(new File(
						updateScriptPath));
				// Iterating over the commands.
				// 一条指令一条指令执行,并没有采用之前上传的脚本文件
				for (String command : upgradeCommands) {
					// executing script.
					if (connection.exec(new ExecCommand(command)).rc != 0) {
						throw new AnkushException(
								"Could not execute agent upgrade command: "
										+ command);
					}
				}
			}
			return true;
		} catch (AnkushException e) {
			LOGGER.error(e.getMessage(), componentName, host, e);
		} catch (IOException e) {
			LOGGER.error("Could not read agent upgrade script.", componentName,
					host, e);
		} catch (TaskExecFailException e) {
			LOGGER.error("Could not execute agent upgrade task.",
					componentName, host, e);
		} catch (Exception e) {
			LOGGER.error("Could not upgrade agent.", componentName, host, e);
		}
		return false;
	}

	// 回滚操作,还原文件,并没有重启agent
	private void rollBack(SSHExec connection) {
		try {
			// Removing Agent upgrade directory, agent.zip, .newAgent folder and
			// agnet installation path
			List<String> dirs = new ArrayList<String>(Arrays.asList(
					NODE_ANKUSH_HOME + "upgrade/", NODE_ANKUSH_HOME
							+ AgentDeployer.AGENT_BUNDLE_NAME,
					NEW_AGENT_FOLDER, clusterConfig.getAgentHomeDir()));
			Remove remove;
			for (String dir : dirs) {
				remove = new Remove(dir);
				connection.exec(remove);
			}

			// backup old existing agent folder.
			Copy copyAgentBackup = new Copy(AGENT_BACKUP_FOLDER,
					clusterConfig.getAgentHomeDir(), true);
			if (connection.exec(copyAgentBackup).rc != 0) {
				LOGGER.error("Could not copy Agent backup folder.");
			}

		} catch (TaskExecFailException e) {
			LOGGER.error("Could not execute Agent rollback task.",
					Constant.Component.Name.AGENT, nodeConfig.getHost(), e);
		}
	}

	// 删除upgrade目录和agent备份目录
	private void removeUpgradeAndBackupDir(SSHExec connection,
			String upgradeScriptDirectory) {
		LOGGER.info("Removing agent upgrade script folder...",
				Constant.Component.Name.AGENT, nodeConfig.getHost());
		// remove agent upgrade script folder.
		Remove remove = new Remove(upgradeScriptDirectory);
		try {
			connection.exec(remove);

			// remove Agnet backup directory
			remove = new Remove(AGENT_BACKUP_FOLDER);
			connection.exec(remove);
		} catch (TaskExecFailException e) {
			LOGGER.error(
					"Could not execute task for removing Agent upgrade and backup directory.",
					Constant.Component.Name.AGENT, nodeConfig.getHost(), e);
		}
	}

	private void startAgent(SSHExec connection) throws AnkushException {
		// start agent
		// String startAgent = "sh " + Constant.Agent.AGENT_START_SCRIPT;
		String startAgent = "sh " + clusterConfig.getAgentHomeDir()
				+ AgentConstant.Relative_Path.START_SCRIPT;
		// create task
		// 调用启动脚本
		CustomTask task = new ExecCommand(startAgent);
		try {
			if (connection.exec(task).rc != 0) {
				throw new AnkushException("Could not start Agent");
			}
		} catch (TaskExecFailException e) {
			throw new AnkushException(
					"Could not execute task for starting Agent");
		}
	}

}
