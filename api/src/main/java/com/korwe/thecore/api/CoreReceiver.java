/*
 * Copyright (c) 2010.  Korwe Software
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

package com.korwe.thecore.api;

import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.CoreMessageSerializer;
import com.korwe.thecore.messages.CoreMessageXmlSerializer;
import org.apache.log4j.Logger;
import org.apache.qpid.transport.*;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CoreReceiver implements SessionListener {

    private static final Logger LOG = Logger.getLogger(CoreReceiver.class);

    private final MessageQueue queue;
    private Connection connection;
    private CoreMessageSerializer serializer;
    private Session session;
    private CoreMessageHandler handler;
    private String queueName;

    public CoreReceiver(MessageQueue queue) {
        this.queue = queue;
        serializer = new CoreMessageXmlSerializer();
    }

    public void connect(CoreMessageHandler handler) {
        this.handler = handler;
        connection = new Connection();
        CoreConfig config = CoreConfig.getConfig();
        connection.connect(config.getSetting("amqp_server"), config.getIntSetting("amqp_port"),
                           config.getSetting("amqp_vhost"), config.getSetting("amqp_user"),
                           config.getSetting("amqp_password"));
        session = connection.createSession();
        session.setSessionListener(this);
        queueName = getQueueName(queue);
        bindToQueue(queueName, session);
        if (LOG.isInfoEnabled()) {
            LOG.info("Connected and waiting for messages: " + queueName);
        }

    }

    public void close() {
        LOG.info("Closing receiver session and connection");
        session.messageCancel(queueName);
        session.close();
        connection.close();
    }

    protected void bindToQueue(String queueName, Session session) {
        session.messageSubscribe(queueName, queueName, MessageAcceptMode.NONE, MessageAcquireMode.PRE_ACQUIRED, null, 0,
                                 null);
        session.messageFlow(queueName, MessageCreditUnit.BYTE, Session.UNLIMITED_CREDIT);
        session.messageFlow(queueName, MessageCreditUnit.MESSAGE, Session.UNLIMITED_CREDIT);
        session.sync();
    }

    protected String getQueueName(MessageQueue queue) {
        return queue.getQueueName();
    }

    @Override
    public void closed(Session session) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Session listener closed: " + queueName);
        }
    }

    @Override
    public void opened(Session session) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Session listener opened");
        }

    }

    @Override
    public void resumed(Session session) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Session listener resumed");
        }

    }

    @Override
    public void message(Session session, MessageTransfer messageTransfer) {
        String msgText = messageTransfer.getBodyString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received: " + msgText);
        }
        else {
            int endIndex = msgText.indexOf("</function");
            LOG.info("Received: " + msgText.substring(0, endIndex > 0 ? endIndex : Math.min(350, msgText.length())));
        }
        CoreMessage message = serializer.deserialize(msgText);
        handler.handleMessage(message);
    }

    @Override
    public void exception(Session session, SessionException e) {
        LOG.error("Error receiving message from queue", e);
    }
}
