package com.korwe.thecore.api;

import org.apache.qpid.transport.Connection;
import org.apache.qpid.transport.ConnectionException;
import org.apache.qpid.transport.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class LoggingConnectionListener implements ConnectionListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConnectionListener.class);

    @Override
    public void opened(final Connection connection) {
        logger.info("### Connection opened {}", connection.toString());
    }

    @Override
    public void exception(final Connection connection, final ConnectionException e) {
        logger.error("### Connection exception {}", connection, e);
    }

    @Override
    public void closed(final Connection connection) {
        logger.warn("### Connection closed {}", connection);
    }
}
