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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CoreSender {

    private static final Logger LOG = Logger.getLogger(CoreSender.class);

    private final MessageQueue queue;
    private Connection connection;
    private CoreMessageSerializer serializer;
    private final Session session;

    public CoreSender(MessageQueue queue) {
        this.queue = queue;
        connection = new Connection();
        CoreConfig config = CoreConfig.getInstance();
        if (LOG.isInfoEnabled()) {
            LOG.info("Connecting to queue server " + config.getProperty("amqp_server"));
        }
        connection.connect(config.getProperty("amqp_server"), config.getIntProperty("amqp_port"),
                           config.getProperty("amqp_vhost"), config.getProperty("amqp_user"),
                           config.getProperty("amqp_password"));
        if (LOG.isInfoEnabled()) {
            LOG.info("Connected");
        }

        session = connection.createSession();
        serializer = new CoreMessageXmlSerializer();
    }

    public void close() {
        session.sync();
        session.close();
        connection.close();
    }

    public void sendMessage(CoreMessage message) {
        if (queue.isDirect()) {
            String destination = MessageQueue.DIRECT_EXCHANGE;
            String routing = queue.getQueueName();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending to " + routing);
            }

            send(message, destination, routing);
        }
        else {
            LOG.error("Message to topic queue must be explicitly addressed");
        }
    }

    public void sendMessage(CoreMessage message, String recipient) {
        if (queue.isTopic()) {
            String destination = MessageQueue.TOPIC_EXCHANGE;
            String routing = queue.getQueueName() + "." + recipient;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending to " + routing);
            }

            send(message, destination, routing);
        }
        else {
            LOG.error("Cannot send to explicitly addressed message direct point to point queue");
        }

    }

    private void send(CoreMessage message, String destination, String routing) {
        String serialized = serializer.serialize(message);
        DeliveryProperties props = new DeliveryProperties();
        props.setRoutingKey(routing);
        MessageProperties msgProps = new MessageProperties();
        Map<String, Object> appHeaders = new HashMap<String, Object>();
        appHeaders.put("sessionId", message.getSessionId());
        appHeaders.put("choreography", message.getChoreography());
        appHeaders.put("guid", message.getGuid());
        appHeaders.put("messageType", message.getMessageType().name());
        msgProps.setApplicationHeaders(appHeaders);
        session.messageTransfer(destination, MessageAcceptMode.EXPLICIT, MessageAcquireMode.PRE_ACQUIRED,
                                new Header(props, msgProps), serialized);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sent: " + serialized);
        }
    }

    public MessageQueue getQueue() {
        return queue;
    }

}
