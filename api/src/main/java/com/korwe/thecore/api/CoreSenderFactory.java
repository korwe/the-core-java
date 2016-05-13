package com.korwe.thecore.api;

import org.apache.qpid.transport.Connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:dario.matonicki@korwe.com>Dario Matonicki</a>
 */
public class CoreSenderFactory {

    private Map<String, Connection> connections = new ConcurrentHashMap<>();

    public  CoreSender createCoreSender(MessageQueue queue,
                                        CoreSenderConnectionType senderConnectionType,
                                        String serviceName) {

        CoreSender coreSender = null;

        switch (senderConnectionType) {
            case NewConnection:
                coreSender = new CoreSender(queue);

                break;
            case SharedConnection:
                Connection connection = getConnection(serviceName);

                coreSender = new CoreConnectionSharingSender(queue, connection);

                break;
        }

        return coreSender;

    }

    public void close(String serviceName) {
        Connection connection = getConnection(serviceName);
        if (connection != null) {
            connection.close();
        }
    }

    private synchronized Connection getConnection(String serviceName) {

        Connection connection;

        if (connections.containsKey(serviceName)) {
             connection = connections.get(serviceName);
        }
        else {
            Connection newConnection = new Connection();
            newConnection.addConnectionListener(new LoggingConnectionListener());

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
