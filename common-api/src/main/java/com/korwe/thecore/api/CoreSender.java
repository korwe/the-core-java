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

import com.google.common.collect.ImmutableMap;
import com.korwe.thecore.exception.CoreSystemException;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.CoreMessageSerializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreSender {

    private static final Logger LOG = LoggerFactory.getLogger(CoreSender.class);

    private final MessageQueue queue;
    private Connection connection;
    private CoreMessageSerializer serializer;
    private Channel channel = null;

     CoreSender(Connection connection, MessageQueue queue, CoreMessageSerializer serializer) {
        this.serializer = serializer;
        this.queue = queue;
        this.connection = connection;

        try {
            channel = connection.createChannel();
        }
        catch (IOException e) {
            LOG.error("Unable to create channel", e);
            throw new CoreSystemException(e, "system.unexpected");
        }
    }

    public void close() {
        try {
            channel.close();
        }
        catch (Exception e) {
            LOG.warn("Channel closing failed", e);
            throw new CoreSystemException(e, "system.unexpected");
        }
    }

    public void sendMessage(CoreMessage message) {
        if (queue.isDirect()) {
            String destination = MessageQueue.DIRECT_EXCHANGE;
            String routing = queue.getQueueName();
            LOG.debug("Sending to " + routing);

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
            LOG.debug("Sending to " + routing);

            send(message, destination, routing);
        }
        else {
            LOG.error("Cannot send explicitly addressed message to direct point to point queue");
        }

    }

    private void send(CoreMessage message, String destination, String routing) {
        String serialized = serializer.serialize(message);
        Map<String, Object> appHeaders = ImmutableMap.of("sessionId", message.getSessionId(),
                                                         "choreography", message.getChoreography(),
                                                         "guid", message.getGuid(),
                                                         "messageType", message.getMessageType().name());
        AMQP.BasicProperties msgProps = new AMQP.BasicProperties.Builder().headers(appHeaders).build();
        try {
            channel.basicPublish(destination, routing, msgProps, serialized.getBytes());
            LOG.debug("Sent: " + serialized);
        }
        catch (IOException e) {
            LOG.error("Unable to send message", e);
            throw new CoreSystemException(e, "system.unexpected");
        }
    }

    public MessageQueue getQueue() {
        return queue;
    }

}
