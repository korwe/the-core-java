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

import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class BasicMessageProcessor implements CoreMessageProcessor {

    private static final Logger LOG = Logger.getLogger(BasicMessageProcessor.class);

    private String sessionId;
    private CoreSender clientSender;
    private CoreSender serviceSender;

    @Override
    public boolean shouldProcessMessage(CoreMessage message) {
        CoreMessage.MessageType messageType = message.getMessageType();
        return messageType == CoreMessage.MessageType.ServiceRequest ||
               messageType == CoreMessage.MessageType.ServiceResponse ||
               messageType == CoreMessage.MessageType.DataResponse;
    }

    @Override
    public void processMessage(CoreMessage message) {
        switch (message.getMessageType()) {
            case ServiceRequest: {
                String serviceName = message.getChoreography();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got request, forwarding to service " + serviceName);
                }
                serviceSender.sendMessage(message, serviceName);
                break;
            }
            case ServiceResponse: {
                String sessionId = message.getSessionId();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Got response, forwarding to client " + sessionId);
                }
                clientSender.sendMessage(message, sessionId);
            }
            default:
                break;
        }
    }

    @Override
    public void initialize(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void stop() {
    }

    protected CoreSender getClientSender() {
        return clientSender;
    }

    public void setClientSender(CoreSender clientSender) {
        this.clientSender = clientSender;
    }

    protected CoreSender getServiceSender() {
        return serviceSender;
    }

    public void setServiceSender(CoreSender serviceSender) {
        this.serviceSender = serviceSender;
    }
}
