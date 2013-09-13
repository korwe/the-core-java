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

package com.korwe.thecore.scxml;

import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreResponse;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;
import com.korwe.thecore.session.CoreSession;
import com.korwe.thecore.session.SessionManager;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.model.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ScxmlMessageSender implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ScxmlMessageSender.class);

    private final String method;
    private Map params;
    private final SCInstance scInstance;
    private final String parentStateId;

    public ScxmlMessageSender(String source, Map params, SCInstance scInstance, String parentStateId) {
        method = source;
        this.params = params;
        this.scInstance = scInstance;
        this.parentStateId = parentStateId;
    }

    private String sendRequest(String queue, String sessionId, String serviceName, String function, String paramNames,
                               String paramValues) {
        return sendRequest(queue, sessionId, serviceName, function, paramNames, paramValues, null);
    }

    private String sendRequest(String queue, String sessionId, String serviceName, String function, String paramNames,
                               String paramValues, String guid) {
        ServiceRequest message = new ServiceRequest(sessionId, function);
        message.setChoreography(serviceName);

        if (guid != null) {
            message.setGuid(guid);
        }

        setMessageParams(paramNames, paramValues, message);

        return sendRequest(MessageQueue.valueOf(queue), message);
    }

    private String sendRequest(MessageQueue queue, ServiceRequest message) {
        CoreSender sender = new CoreSender(queue);
        sender.sendMessage(message, message.getChoreography());
        sender.close();
        return message.getGuid();
    }

    private void setMessageParams(String paramNames, String paramValues, ServiceRequest message) {
        String[] names = paramNames.split("\\|");
        String[] values = paramValues.split("\\|");

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (i < values.length) {
                message.setParameter(name, values[i]);
            }
        }
    }

    private void sendResponse(String queue, String sessionId, String guid, boolean successful, boolean hasData,
                              String errorCode, String errorMessage) {
        CoreResponse response = new ServiceResponse(sessionId, guid, successful, hasData);

        if (errorCode != null && !errorCode.isEmpty()) {
            response.setErrorCode(errorCode);
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
            response.setErrorMessage(errorMessage);
        }

        sendResponse(MessageQueue.valueOf(queue), response);
    }

    private void sendResponse(MessageQueue queue, CoreResponse response) {
        CoreSender sender = new CoreSender(queue);
        sender.sendMessage(response, response.getSessionId());
        sender.close();
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("message sender thread started");
        }
        try {
            scInstance.getExecutor()
                    .triggerEvent(new TriggerEvent(parentStateId + ".invoke.done", TriggerEvent.SIGNAL_EVENT));
        }
        catch (ModelException e) {
            LOG.error("Error triggering 'invoke.done' event", e);
        }


        if ("sendRequest".equals(method)) {
            sendRequest(params);
        }
        else if ("sendResponse".equals(method)) {
            sendResponse(params);
        }
        else if ("forwardRequest".equals(method)) {
            forwardRequest(params);
        }
        else if ("forwardResponse".equals(method)) {
            forwardResponse(params);
        }
    }

    private void sendResponse(Map params) {
        sendResponse((String) params.get("queue"), (String) params.get("sessionId"), (String) params.get("guid"),
                     Boolean.valueOf((String) params.get("successful")),
                     Boolean.valueOf((String) params.get("hasData")), (String) params.get("errorCode"),
                     (String) params.get("errorMessage"));
    }

    private void sendRequest(Map params) {
        sendRequest((String) params.get("queue"), (String) params.get("sessionId"), (String) params.get("service"),
                    (String) params.get("function"), (String) params.get("paramNames"),
                    (String) params.get("paramValues"), (String) params.get("guid"));
    }

    private void forwardRequest(Map params) {
        CoreSession session = lookupSession(params);
        String guid = (String) params.get("guid");
        session.forwardToService(guid);
    }

    private CoreSession lookupSession(Map params) {
        String sessionId = (String) params.get("sessionId");
        CoreSession session = SessionManager.getSession(sessionId);
        return session;
    }

    private void forwardResponse(Map params) {
        CoreSession session = lookupSession(params);
        String guid = (String) params.get("guid");
        session.forwardToClient(guid);
    }
}
