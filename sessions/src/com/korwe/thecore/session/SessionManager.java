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

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.korwe.thecore.api.*;
import com.korwe.thecore.messages.*;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class SessionManager extends AbstractExecutionThreadService implements CoreMessageHandler {

    private static final Logger LOG = Logger.getLogger(SessionManager.class);

    private static Map<String, CoreSession> sessions = new ConcurrentHashMap<String, CoreSession>(128, 2.0f, 8);

    private final CoreSubscriber subscriber;
    private final CoreSender clientSender;
    private final String processorType;
    private ExecutorService executorService;
    private int maxThreads;

    public static CoreSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public SessionManager() {
        processorType = CoreConfig.getConfig().getSetting("processor_type");
        subscriber = new CoreSubscriber(MessageQueue.CoreToSession, CoreConfig.getConfig().getSetting("session_message_filter"));
        clientSender = new CoreSender(MessageQueue.CoreToClient);
        maxThreads = CoreConfig.getConfig().getIntSetting("max_threads");
    }

    @Override
    protected void startUp() {
        executorService = Executors.newFixedThreadPool(maxThreads);
        subscriber.connect(this);
    }

    protected void triggerShutdown() {
        if (LOG.isInfoEnabled()) {
            LOG.info("SessionManager stopping");
        }
        subscriber.close();
        clientSender.close();
        executorService.shutdown();
    }

    @Override
    public void handleMessage(final CoreMessage message) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (message != null) {
                    String sessionId = message.getSessionId();
                    CoreResponse response;
                    switch (message.getMessageType()) {
                        case InitiateSessionRequest:
                            response = handleInitiateSession(message, sessionId);
                            break;
                        case KillSessionRequest:
                            response = handleKillSession(message, sessionId);
                            break;
                        default:
                            response = handleDefault(message, sessionId);
                            break;
                    }
                    if (response != null) {
                        clientSender.sendMessage(response, sessionId);
                    }
                }

            }
        });
    }

    private CoreResponse handleDefault(CoreMessage message, String sessionId) {
        if (sessions.containsKey(sessionId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Forwarding to session");
            }
            sessions.get(sessionId).handleMessage(message);
            return null;
        }
        else {
            CoreResponse response = new ServiceResponse(sessionId, message.getGuid(), false, false);
            response.setErrorCode("BadSession");
            response.setErrorMessage("Session " + sessionId + " does not exist");
            return response;
        }
    }

    private CoreResponse handleKillSession(CoreMessage message, String sessionId) {
        CoreResponse response;
        if (sessions.containsKey(sessionId)) {
            CoreSession removed = sessions.remove(sessionId);
            removed.stop();
            response = new KillSessionResponse(sessionId, message.getGuid(), true);
        }
        else {
            response = new KillSessionResponse(sessionId, message.getGuid(), false);
            response.setErrorCode("BadSession");
            response.setErrorMessage("Session " + sessionId + " does not exist");
        }
        return response;
    }

    private CoreResponse handleInitiateSession(CoreMessage message, String sessionId) {
        CoreResponse response;
        if (sessions.containsKey(sessionId)) {
            response = new InitiateSessionResponse(sessionId, message.getGuid(), false);
            response.setErrorCode("BadSession");
            response.setErrorMessage("Session " + sessionId + " already exists");
        }
        else {
            CoreMessageProcessor processor = createProcessor(sessionId);
            CoreSession session = new CoreSession(sessionId, processor, CoreConfig.getConfig().getIntSetting("timeout_seconds"));
            sessions.put(sessionId, session);
            response = new InitiateSessionResponse(sessionId, message.getGuid(), true);
        }
        return response;
    }

    private CoreMessageProcessor createProcessor(String sessionId) {
        if (processorType == null || sessionId == null || processorType.isEmpty() || sessionId.isEmpty()) {
            return null;
        }
        try {
            CoreMessageProcessor processor = (CoreMessageProcessor) Class.forName(processorType).newInstance();
            processor.initialize(sessionId);
            return processor;
        }
        catch (Exception e) {
            LOG.error("Unable to create message processor", e);
            return null;
        }
    }


    public void run() {
        while (isRunning()) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Waiting for messages");
                }
                Thread.sleep(10 * 1000L);
                checkTimeouts();
            }
            catch (InterruptedException e) {
                // Ignore interruptions and carry on
            }
        }
    }

    private void checkTimeouts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking timeouts");
        }

        for (Iterator<Map.Entry<String, CoreSession>> iterator = sessions.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, CoreSession> entry = iterator.next();
            CoreSession session = entry.getValue();
            if (session.isTimedOut()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Session " + entry.getKey() + " has timed out; removing.");
                }
                session.stop();
                iterator.remove();
            }
        }
    }

}
