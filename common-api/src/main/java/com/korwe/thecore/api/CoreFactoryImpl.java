package com.korwe.thecore.api;

import com.korwe.thecore.exception.CoreSystemException;
import com.korwe.thecore.messages.CoreMessageSerializer;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class CoreFactoryImpl implements CoreFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CoreFactoryImpl.class);

    private ConnectionFactory connectionFactory;
    private CoreMessageSerializer serializer;
    private Map<String, Connection> connections = new ConcurrentHashMap<>();

    public CoreFactoryImpl(final ConnectionFactory connectionFactory, final CoreMessageSerializer serializer) {
        this.connectionFactory = connectionFactory;
        this.serializer = serializer;
    }

    @Override
    public CoreSender createSender(final MessageQueue messageQueue, final String clientId) {
        LOG.info("Creating sender for queue {} for client {}", messageQueue, clientId);
        return new CoreSender(getConnection(clientId), messageQueue, serializer);
    }

    private String computeKey(final MessageQueue messageQueue, final String clientId) {
        return messageQueue + "_" + clientId;
    }

    @Override
    public CoreSubscriber createSubscriber(final MessageQueue messageQueue, final String clientId) {
        LOG.info("Creating subscriber for queue {} for client {}", messageQueue, clientId);
        return new CoreSubscriber(getConnection(clientId), messageQueue, clientId, serializer);
    }

    private Connection getConnection(final String clientId) {
        return connections.computeIfAbsent(clientId, s -> {
                try {
                    return connectionFactory.newConnection();
                }
                catch (Exception e) {
                    LOG.error("Unable to create connection", e);
                    throw new CoreSystemException(e, "system.unexpected");
                }
            });
    }


    public void shutdown() {
        connections.forEach((s, connection) -> {
            try {
                connection.close();
            }
            catch (IOException e) {
                LOG.error("Unable to close connection", e);
            }
        });
    }
}
