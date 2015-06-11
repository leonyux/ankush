package com.impetus.ankush2.kafka;

import com.impetus.ankush2.framework.AbstractDeployer;
import com.impetus.ankush2.framework.config.ClusterConfig;
import com.impetus.ankush2.logger.AnkushLogger;
import com.impetus.ankush2.zookeeper.ZookeeperConstant;

import java.util.Collection;
import java.util.Set;

/**
 * Created by xy on 15-6-11.
 * Kafka service deployer.
 */
public class KafkaDeployer extends AbstractDeployer{

    /** The logger. */
    private final AnkushLogger logger = new AnkushLogger(
            KafkaDeployer.class);

    /** The cluster config. */
    private ClusterConfig clusterConfig;

    /**
     * Sets the cluster and logger.
     *
     * @param clusterConfig
     *            the cluster config
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

    @Override
    public boolean validateNodes(ClusterConfig conf, ClusterConfig newConf) {
        return super.validateNodes(conf, newConf);
    }

    @Override
    public void setComponentName(String componentName) {
        super.setComponentName(componentName);
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
    public boolean createConfig(ClusterConfig conf) {
        return super.createConfig(conf);
    }

    @Override
    public boolean validate(ClusterConfig conf) {
        return super.validate(conf);
    }

    @Override
    public boolean deploy(ClusterConfig conf) {
        return super.deploy(conf);
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
