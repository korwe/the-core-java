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

package com.korwe.thecore.session;

import com.google.inject.Inject;
import com.korwe.thecore.api.CoreMessageHandler;
import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CoreSession implements CoreMessageHandler {

    private static final Logger LOG = Logger.getLogger(CoreSession.class);

    private String sessionId;
    private long timeoutMillis;
    private CoreMessageProcessor processor;
    private long lastMessageTime;
    private static final long MILLIS_PER_SEC = 1000L;
    private CoreSender serviceSender = new CoreSender(MessageQueue.CoreToService);
    private CoreSender clientSender = new CoreSender(MessageQueue.CoreToClient);
    private Map<String, CoreMessage> cache = new ConcurrentHashMap<String, CoreMessage>(64);

    public String getSessionId() {
        return sessionId;
    }

    @Inject
    public CoreSession(String sessionId, CoreMessageProcessor processor, int timeoutSeconds) {
        this.processor = processor;
        this.sessionId = sessionId;
        this.timeoutMillis = MILLIS_PER_SEC * timeoutSeconds;
        lastMessageTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session created for " + sessionId);
        }

    }

    @Override
    public void handleMessage(CoreMessage message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling message " + message);
        }

        lastMessageTime = System.currentTimeMillis();
        cache.put(message.getGuid(), message);
        if (processor.shouldProcessMessage(message)) {
            processor.processMessage(message);
        }
    }

    public void stop() {
        processor.stop();
        clientSender.close();
        serviceSender.close();
    }

    public boolean isTimedOut() {
        return System.currentTimeMillis() - lastMessageTime > timeoutMillis;
    }

    public void sendToService(CoreMessage message) {
        serviceSender.sendMessage(message, message.getChoreography());
    }

    public void sendToClient(CoreMessage message) {
        clientSender.sendMessage(message, sessionId);
    }

    public void forwardToService(String guid) {
        sendToService(cache.remove(guid));
    }

    public void forwardToClient(String guid) {
        sendToClient(cache.remove(guid));
    }

    public void discard(String guid) {
        cache.remove(guid);
    }
}
