/*
 * Copyright (c) 2011.  Korwe Software
 *
 *  This file is part of TheCore.
 *
 *  TheCore is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TheCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with TheCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.korwe.thecore.webcore.listener;

import com.google.common.util.concurrent.AbstractIdleService;
import com.jolbox.bonecp.BoneCPDataSource;
import com.korwe.thecore.api.CoreMessageHandler;
import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.CoreMessageSerializer;
import com.korwe.thecore.messages.CoreMessageXmlSerializer;
import com.korwe.thecore.webcore.DataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class WebCoreListener extends AbstractIdleService implements CoreMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebCoreListener.class);
    private static final String WEBCORE = "webcore.";
    private static final String FILTER = WEBCORE + "#";

    private static final String SAVE_MESSAGE_SQL =
            "INSERT INTO received_message (queue_name, sessionId, guid, message) VALUES (?, ?, ?, ?)";

    private BoneCPDataSource ds;
    private CoreSubscriber responseSubscriber;
    private CoreSubscriber dataSubscriber;
    private CoreMessageSerializer serializer;

    public WebCoreListener() {
        serializer = new CoreMessageXmlSerializer();
    }

    /**
     * Start the service.
     */
    @Override
    protected void startUp() throws Exception {
        LOG.info("WebCoreListener starting");
        ds = DataSourceProvider.createDataSource();
        responseSubscriber = new CoreSubscriber(MessageQueue.CoreToClient, FILTER);
        dataSubscriber = new CoreSubscriber(MessageQueue.Data, FILTER);
        responseSubscriber.connect(this);
        dataSubscriber.connect(this);
        LOG.info("WebCoreListener started");
    }

    /**
     * Stop the service.
     */
    @Override
    protected void shutDown() throws Exception {
        LOG.info("WebCoreListener stopping");
        ds.close();
        responseSubscriber.close();
        dataSubscriber.close();
    }

    @Override
    public void handleMessage(CoreMessage message) {
        String sessionId = message.getSessionId();
        if (sessionId.startsWith(WEBCORE)) {
            sessionId = sessionId.substring(WEBCORE.length());
            message.setSessionId(sessionId);
            String guid = message.getGuid();
            String queueName = CoreMessage.MessageType.DataResponse == message.getMessageType() ?
                               "data" : "response";
            String xml = serializer.serialize(message);
            saveMessage(queueName, sessionId, guid, xml);
        }
        else {
            LOG.error("error: Not a webcore message");
        }
    }

    private void saveMessage(String queueName, String sessionId, String guid, String xml) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = ds.getConnection();
            statement = connection.prepareStatement(SAVE_MESSAGE_SQL);
            statement.setString(1, queueName);
            statement.setString(2, sessionId);
            statement.setString(3, guid);
            statement.setString(4, xml);
            statement.executeUpdate();
            LOG.debug("Saved message: " + xml);
        }
        catch (SQLException e) {
            LOG.error("Database error", e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

}
