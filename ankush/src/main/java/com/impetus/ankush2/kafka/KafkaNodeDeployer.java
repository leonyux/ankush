package com.impetus.ankush2.kafka;

import com.impetus.ankush.common.exception.AnkushException;
import com.impetus.ankush.common.scripting.AnkushTask;
import com.impetus.ankush.common.scripting.impl.AppendFileUsingEcho;
import com.impetus.ankush.common.scripting.impl.MakeDirectory;
import com.impetus.ankush2.constant.Constant;
import com.impetus.ankush2.framework.config.ClusterConfig;
import com.impetus.ankush2.framework.config.ComponentConfig;
import com.impetus.ankush2.framework.config.NodeConfig;
import com.impetus.ankush2.logger.AnkushLogger;
import com.impetus.ankush2.utils.SSHUtils;
import com.impetus.ankush2.zookeeper.ZookeeperConstant;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;

import java.util.Map;

/**
 * Created by xy on 15-6-11.
 */
public class KafkaNodeDeployer {
    /**
     * The logger.
     */
    private final AnkushLogger logger = new AnkushLogger(KafkaNodeDeployer.class);

    /**
     * The cluster config.
     */
    private ClusterConfig clusterConfig;

    /**
     * The node config.
     */
    private NodeConfig nodeConfig;

    /**
     * The conf.
     */
    private ComponentConfig conf;

    /**
     * The node advance conf.
     */
    private Map<String, Object> nodeAdvanceConf;

    /**
     * The component name.
     */
    private String componentName;

    /**
     * Instantiates a new zookeeper worker.
     *
     * @param clusterConf     the cluster conf
     * @param componentName   the component name
     * @param nodeConfig      the node config
     * @param nodeAdvanceConf the node advance conf
     */
    public KafkaNodeDeployer(ClusterConfig clusterConf, String componentName,
                             NodeConfig nodeConfig, Map<String, Object> nodeAdvanceConf) {
        this.setClusterAndLogger(clusterConf, componentName, nodeConfig);
        this.nodeAdvanceConf = nodeAdvanceConf;
    }

    /**
     * Instantiates a new zookeeper worker.
     *
     * @param clusterConf   the cluster conf
     * @param componentName the component name
     * @param nodeConfig    the node config
     */
    public KafkaNodeDeployer(ClusterConfig clusterConf, String componentName,
                             NodeConfig nodeConfig) {
        this.setClusterAndLogger(clusterConf, componentName, nodeConfig);
    }

    /**
     * Sets the cluster and logger.
     *
     * @param clusterConfig the cluster config
     * @param componentName the component name
     * @param nodeConfig    the node config
     */
    private void setClusterAndLogger(ClusterConfig clusterConfig,
                                     String componentName, NodeConfig nodeConfig) {
        this.clusterConfig = clusterConfig;
        this.conf = clusterConfig.getComponents().get(componentName);
        this.componentName = componentName;
        this.nodeConfig = nodeConfig;
        logger.setCluster(clusterConfig);
    }

    /**
     * Creates the node.
     *
     * @return true, if successful
     */
    public boolean createNode() {
        logger.info("Starting Kafka node deployment..", nodeConfig.getHost(),
                this.componentName);

        SSHExec connection = null;
        Result res = null;
        try {
            // connect to remote node
            connection = this.nodeConfig.getConnection();

            // if connected
            if (connection != null) {
                logger.info("Create directory - " + this.conf.getInstallPath(),
                        this.componentName, this.nodeConfig.getPublicHost());
                // make installation directory if not exists
                AnkushTask mkInstallationPath = new MakeDirectory(
                        this.conf.getInstallPath());
                res = connection.exec(mkInstallationPath);

                if (!res.isSuccess) {
                    // logger.error("Could not create installation directory",
                    // this.componentName, this.nodeConfig.getPublicHost());
                    // return false;
                    throw new AnkushException(
                            "Could not create installation directory");
                }
                logger.info("Get and extract tarball", this.componentName,
                        this.nodeConfig.getPublicHost());
                // get and extract tarball
                boolean isSuccessfull = SSHUtils.getAndExtractComponent(
                        connection, this.conf,
                        Constant.Component.Name.KAFKA);
                if (!isSuccessfull) {
                    throw new AnkushException(
                            "Could not extract bundle for Zookeeper");
                }
                // make data directory
                AnkushTask makeDataDir = new MakeDirectory((String) this.conf
                        .getAdvanceConf().get(
                                KafkaConstant.Keys.LOG_DIRECTORY));

                // create server.properties file
                AnkushTask createKafkaCfg = new AppendFileUsingEcho(
                        getKafkaConfContents(), this.conf.getHomeDir()
                        + KafkaConstant.Keys.CONF_FILE);

                logger.info("Creating kafka's data directory...",
                        this.componentName, this.nodeConfig.getPublicHost());
                if (!connection.exec(makeDataDir).isSuccess) {
                    throw new AnkushException(
                            "Couldn't create kafka's data directory");
                }
                logger.info("Creating server.properties file...", this.componentName,
                        this.nodeConfig.getPublicHost());
                if (!connection.exec(createKafkaCfg).isSuccess) {
                    throw new AnkushException("Couldn't create server.properrties file");
                }

                logger.info("Kafka nodedeployer thread execution over ... ",
                        this.componentName, this.nodeConfig.getPublicHost());
                return true;
            } else {
                throw new AnkushException(
                        Constant.Strings.ExceptionsMessage.CONNECTION_NULL_STRING);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), componentName, nodeConfig.getHost(), e);
            this.clusterConfig.addError(this.nodeConfig.getHost(),
                    componentName, e.getMessage());
        }
        return false;
    }

    /**
     * 构造kafka配置文件,目前全部内容自动生成,未采用模板方式
     *
     * @return kafka配置文件内容
     * TODO 更好的方式生成配置文件
     */
    public String getKafkaConfContents() {

        // StringBulder构造配置文件内容
        StringBuilder confBuilder = new StringBuilder();
        // 对配置的文件路径去掉末尾的"/"号
        String dataDir = (String) this.conf.getAdvanceConf().get(
                KafkaConstant.Keys.LOG_DIRECTORY);
        if ((dataDir != null) && (dataDir.endsWith("/"))) {
            dataDir = dataDir.substring(0, dataDir.length() - 1);
        }
        confBuilder
                // 配置broker.id
                .append(KafkaConstant.Keys.BROKER_ID + "=")
                .append(this.nodeAdvanceConf.get(KafkaConstant.Keys.BROKER_ID))
                .append("\n")

                        // 配置port
                .append(KafkaConstant.Keys.PORT + "=")
                .append(this.conf.getAdvanceConf().get(KafkaConstant.Keys.PORT))
                .append("\n")

                        // 配置hostname,如果配置为域名需要有节点间有正确的域名服务
                .append(KafkaConstant.Keys.HOSTNAME + "=")
                .append(this.nodeConfig.getHost())
                .append("\n")

                        // 配置topic保存路径
                .append(KafkaConstant.Keys.LOG_DIRECTORY + "=")
                .append(dataDir)
                .append("\n")

                        // 配置zookeeper服务连接
                .append(KafkaConstant.Keys.ZOOKEEPER_CONNECT + "=");
        for (String host : clusterConfig.getComponents()
                .get(Constant.Component.Name.ZOOKEEPER)
                .getNodes().keySet()) {
            confBuilder
                    .append(host)
                    .append(this.conf.getNodes().get(host))
                    .append(":2181,");
        }
        confBuilder.append("\n");

        return confBuilder.toString();
    }
}
