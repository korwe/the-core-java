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

import org.apache.qpid.transport.Option;
import org.apache.qpid.transport.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreSubscriber extends CoreReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CoreSubscriber.class);

    private final String filter;

    public CoreSubscriber(MessageQueue queue, String filter) {
        this.filter = filter;
        this.queueName = getQueueName(queue);
    }

    @Override
    protected void bindToQueue(String queueName, Session session) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Binding to topic " + queueName);
        }

        session.queueDeclare(queueName, null, null, Option.AUTO_DELETE);
        session.exchangeBind(queueName, MessageQueue.TOPIC_EXCHANGE, queueName, null);
        super.bindToQueue(queueName, session);
    }

    @Override
    protected String getQueueName(MessageQueue queue) {
        return queue.getQueueName() + "." + filter;
    }
}
