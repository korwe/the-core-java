package com.korwe.thecore.api;

import org.apache.qpid.transport.Connection;

import java.util.HashMap;

/**
 * @author <a href="mailto:dario.matonicki@korwe.com>Dario Matonicki</a>
 */
public class CoreSenderFactory {

    private HashMap<String, Connection> connections = new HashMap<>();

    public  CoreSender createCoreSender(MessageQueue queue,
                                        CoreSenderConnectionType senderConnectionType,
                                        String serviceName) {

        CoreSender coreSender = null;

        if (senderConnectionType == CoreSenderConnectionType.NewConnection) {
            coreSender = new CoreSender(queue);

        }
        else if (senderConnectionType == CoreSenderConnectionType.SharedConnection){

            Connection connection = getConnection(serviceName);

            coreSender = new CoreConnectionSharingSender(queue, connection);

        }

        return coreSender;

    }

    public void close(String serviceName) {
        Connection connection = getConnection(serviceName);
        if (connection != null) {
            connection.close();
        }
    }

    private Connection getConnection(String serviceName) {

        Connection connection;

        if (connections.containsKey(serviceName)) {
             connection = connections.get(serviceName);
        }
        else {
            Connection newConnection = new Connection();

            CoreConfig config = CoreConfig.getConfig();

            newConnection.connect(config.getSetting("amqp_server"), config.getIntSetting("amqp_port"),
                    config.getSetting("amqp_vhost"), config.getSetting("amqp_user"),
                    config.getSetting("amqp_password"));

            connections.put(serviceName, newConnection);

            connection = newConnection;
        }

        return connection;
    }
}
