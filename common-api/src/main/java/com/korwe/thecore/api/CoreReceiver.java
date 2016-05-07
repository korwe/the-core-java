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

import com.korwe.thecore.exception.CoreSystemException;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.CoreMessageSerializer;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CoreReceiver.class);

    private Connection connection;
    private CoreMessageSerializer serializer;
    private Channel channel;
    private CoreMessageHandler handler;
    protected String queueName;

    CoreReceiver(Connection connection, CoreMessageSerializer serializer){
        this.connection = connection;
        this.serializer = serializer;
    }

    CoreReceiver(Connection connection, String queueName, CoreMessageSerializer serializer){
        this.connection = connection;
        this.queueName = queueName;
        this.serializer = serializer;
    }

    CoreReceiver(Connection connection, MessageQueue queue, CoreMessageSerializer serializer) {
        this.connection = connection;
        this.serializer = serializer;
        queueName = getQueueName(queue);
    }

    public void connect(CoreMessageHandler handler) {
        this.handler = handler;
        try {
            channel = connection.createChannel();
            bindToQueue(queueName, channel);
            channel.basicConsume(queueName, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(final String consumerTag, final Envelope envelope,
                                           final AMQP.BasicProperties properties, final byte[] body)
                        throws IOException {
                    String msgText = body == null || body.length == 0 ? null : new String(body);
                    LOG.debug("Received: " + msgText);
                    if (msgText != null) {
                        CoreMessage message = serializer.deserialize(msgText);
                        handler.handleMessage(message);
                    }
                }
            });
        }
        catch (IOException e) {
            LOG.error("Unable to create channel", e);
            throw new CoreSystemException(e, "system.unexpected");

        }

        LOG.debug("Connected and waiting for messages");

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

    protected void bindToQueue(String queueName, Channel channel) {
        try {
            channel.queueDeclarePassive(queueName);
            channel.queueBind(queueName, MessageQueue.DIRECT_EXCHANGE, queueName);
        }
        catch (IOException e) {
            LOG.error("Unable to connect to queue", e);
        }
    }

    protected String getQueueName(MessageQueue queue) {
        return queue.getQueueName();
    }

}
