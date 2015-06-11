package com.impetus.ankush2.kafka;

/**
 * Created by xy on 15-6-11.
 * Kafka related constants.
 */
public class KafkaConstant {
    public interface Keys {

        String ERROR_KAFKA_CONF_NOT_FOUND = "Zookeeper configuration is not properly send.Please provide valid configuration and nodes.";
        String BROKER_ID = "broker.id";
        String LAST_BROKER_ID = "lastbroker.id";
        String LOG_DIRECTORY = "log.dirs";
        String CONF_FILE = "config/server.properties";
        String HOSTNAME= "host.name";
        String ZOOKEEPER_CONNECT = "zookeeper.connect";
        String PORT = "port";
    }
}
