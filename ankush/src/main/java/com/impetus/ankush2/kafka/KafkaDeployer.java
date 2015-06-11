package com.impetus.ankush2.kafka;

import com.impetus.ankush.AppStoreWrapper;
import com.impetus.ankush2.constant.Constant;
import com.impetus.ankush2.framework.AbstractDeployer;
import com.impetus.ankush2.framework.config.ClusterConfig;
import com.impetus.ankush2.framework.config.ComponentConfig;
import com.impetus.ankush2.framework.config.NodeConfig;
import com.impetus.ankush2.logger.AnkushLogger;
import com.impetus.ankush2.utils.AnkushUtils;
import com.impetus.ankush2.zookeeper.ZookeeperConstant;
import com.impetus.ankush2.zookeeper.ZookeeperServiceMonitor;
import com.impetus.ankush2.zookeeper.ZookeeperWorker;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Created by xy on 15-6-11.
 * Kafka service deployer.
 */
public class KafkaDeployer extends AbstractDeployer {

    /**
     * The logger.
     */
    private final AnkushLogger logger = new AnkushLogger(
            KafkaDeployer.class);

    /**
     * The cluster config.
     */
    private ClusterConfig clusterConfig;

    private ComponentConfig compConf;

    private Map<String, Object> advanceConf;

    /**
     * Sets the cluster and logger.
     *
     * @param clusterConfig the cluster config
     * @return true, if successful
     */
    private boolean setClusterAndLogger(ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        if (this.clusterConfig.getComponents().get(this.componentName) == null) {
            logger.error(KafkaConstant.Keys.ERROR_KAFKA_CONF_NOT_FOUND,
                    this.componentName);
            this.clusterConfig.addError(this.componentName,
                    KafkaConstant.Keys.ERROR_KAFKA_CONF_NOT_FOUND);
            return false;
        }
        logger.setCluster(clusterConfig);
        return true;
    }


    /**
     * 验证节点
     *
     * @param conf
     * @param newConf
     * @return
     */
    @Override
    public boolean validateNodes(ClusterConfig conf, ClusterConfig newConf) {
        return super.validateNodes(conf, newConf);
    }

    @Override
    public boolean start(ClusterConfig conf, Collection<String> nodes) {
        return super.start(conf, nodes);
    }

    @Override
    public boolean stop(ClusterConfig conf, Collection<String> nodes) {
        return super.stop(conf, nodes);
    }

    @Override
    public String getComponentName() {
        return super.getComponentName();
    }

    @Override
    public void setComponentName(String componentName) {
        super.setComponentName(componentName);
    }

    /**
     * 为组件准备配置
     *
     * @param clusterConfig
     * @return
     */
    @Override
    public boolean createConfig(ClusterConfig clusterConfig) {
        if (!this.setClusterAndLogger(clusterConfig)) {
            return false;
        }
        try {
            this.compConf = clusterConfig.getComponents().get(
                    this.componentName);
            if (this.compConf == null) {

                String errMsg = Constant.Strings.ExceptionsMessage.INVALID_CLUSTER_CONFIG_MSG
                        + ": Kafka component missing.";

                logger.error(errMsg, componentName);
                clusterConfig.addError(componentName, errMsg);
                return false;
            }

            this.advanceConf = this.compConf.getAdvanceConf();
        } catch (Exception e) {
            logger.error("Couldn't create configuration",
                    this.componentName, e);
            return false;
        }
    return true;
}

    @Override
    public boolean validate(ClusterConfig conf) {
        return super.validate(conf);
    }

    @Override
    public boolean deploy(ClusterConfig conf) {
        if (!this.setClusterAndLogger(clusterConfig)) {
            return false;
        }
        ComponentConfig compConf = clusterConfig.getComponents().get(
                this.componentName);
        try {
            int nodeId = 0;
            // iterate over node list
            for (String host : compConf.getNodes().keySet()) {
                nodeId++;
                ((Map<String, Object>) compConf.getNodes().get(host)).put(
                        KafkaConstant.Keys.BROKER_ID, nodeId);
            }
            compConf.getAdvanceConf().put(KafkaConstant.Keys.LAST_BROKER_ID,
                    nodeId);
            return deployNodes(clusterConfig);
        } catch (Exception e) {
            logger.error(e.getMessage(), this.componentName, e);
        }
        return false;
    }

    private boolean deployNodes(ClusterConfig clusterConfig) {
        logger.info("Deploying Kafka...", this.componentName);
        ComponentConfig compConfig = clusterConfig.getComponents().get(
                this.componentName);
        final Semaphore semaphore = new Semaphore(compConfig.getNodes().size());

        boolean status = false;
        try {
            for (final String host : compConfig.getNodes().keySet()) {
                // acuiring the semaphore
                semaphore.acquire();
                final NodeConfig nodeConfig = clusterConfig.getNodes()
                        .get(host);
                Map<String, Object> nodeAdvanceConf = compConfig.getNodes()
                        .get(host);

                final KafkaNodeDeployer kafkaNodeDeployer = new KafkaNodeDeployer(
                        this.clusterConfig, this.componentName, nodeConfig,
                        nodeAdvanceConf);
                final ZookeeperServiceMonitor zooServiceMonitor = new ZookeeperServiceMonitor(
                        clusterConfig, this.componentName);
                AppStoreWrapper.getExecutor().execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            nodeConfig.setStatus(kafkaNodeDeployer.createNode());
                            if (nodeConfig.getStatus()) {
                                if (!zooServiceMonitor.action(
                                        ZookeeperConstant.Action.START,
                                        nodeConfig.getHost())) {
                                    logger.warn("Couldn't "
                                                    + ZookeeperConstant.Action.START
                                                    + " Zookeeper.", componentName,
                                            nodeConfig.getHost());
                                }
                            } else {
                                logger.error("Couldn't deploy Zookeeper.",
                                        componentName, nodeConfig.getHost());
                            }
                        } catch (Exception e) {
                            nodeConfig.setStatus(false);
                        } finally {
                            if (semaphore != null) {
                                semaphore.release();
                            }
                        }
                    }
                });
            }
            // waiting for all semaphores to finish the installation.
            semaphore.acquire(compConfig.getNodes().size());
            status = AnkushUtils.getStatus(clusterConfig, compConfig.getNodes()
                    .keySet());
        } catch (Exception e) {
            logger.error(e.getMessage(), this.componentName, e);
            status = false;
        }
        if (status) {
            logger.info("Deploying Zookeeper is over.", componentName);
        } else {
            logger.error("Deploying Zookeeper failed.", componentName);
            clusterConfig.addError(getComponentName(),
                    "Deploying Zookeeper failed.");
        }
        return status;
    }

    @Override
    public boolean register(ClusterConfig conf) {
        return super.register(conf);
    }

    @Override
    public boolean undeploy(ClusterConfig conf) {
        return super.undeploy(conf);
    }

    @Override
    public boolean unregister(ClusterConfig conf) {
        return super.unregister(conf);
    }

    @Override
    public boolean start(ClusterConfig conf) {
        return super.start(conf);
    }

    @Override
    public boolean stop(ClusterConfig conf) {
        return super.stop(conf);
    }

    @Override
    public boolean addNode(ClusterConfig conf, ClusterConfig newConf) {
        return super.addNode(conf, newConf);
    }

    @Override
    public boolean removeNode(ClusterConfig conf, Collection<String> nodes) {
        return super.removeNode(conf, nodes);
    }

    @Override
    public boolean removeNode(ClusterConfig conf, ClusterConfig newConf) {
        return super.removeNode(conf, newConf);
    }

    @Override
    public boolean canNodeBeDeleted(ClusterConfig clusterConfig, Collection<String> nodes) {
        return super.canNodeBeDeleted(clusterConfig, nodes);
    }

    @Override
    public Set<String> getNodesForDependenciesDeployment(ClusterConfig clusterConfig) {
        return super.getNodesForDependenciesDeployment(clusterConfig);
    }
}
