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

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public enum MessageQueue {
    ClientToCore("core.client-core", true),
    CoreToService("core.core-service", false),
    ServiceToCore("core.service-core", true),
    CoreToClient("core.core-client", false),
    CoreToSession("core.core-session", false),
    Data("core.data", false),
    Trace("core.trace", true);

    public static final String DIRECT_EXCHANGE = "core.direct";
    public static final String TOPIC_EXCHANGE = "core.topic";

    private String queueName;
    private boolean direct;

    private MessageQueue(String queueName, boolean direct) {
        this.queueName = queueName;
        this.direct = direct;
    }

    public String getQueueName() {
        return queueName;
    }

    public boolean isDirect() {
        return direct;
    }

    public boolean isTopic() {
        return !isDirect();
    }

    public static MessageQueue fromQueueName(String queueName) {
        for (MessageQueue messageQueue : values()) {
            if (messageQueue.getQueueName().equals(queueName)) {
                return messageQueue;
            }
        }
        return null;
    }
}
