package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
* @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
*/
public class RequestProcessor {

    private final String clientId;
    private CoreSender coreSender;

    public RequestProcessor(String clientId) {
        this.clientId = clientId;
        coreSender = new CoreSender(MessageQueue.ClientToCore);
    }

    public void processRequests(Iterable<ServiceRequest> serviceRequests,
                                MessageResponseRegistry messageResponseRegistry, CountDownLatch latch) {
        for (ServiceRequest serviceRequest : serviceRequests) {
            serviceRequest.setSessionId(clientId);
            messageResponseRegistry.registerRequest(serviceRequest.getGuid(), latch);
            coreSender.sendMessage(serviceRequest);
        }
    }

    public void sendInitiateSession() {
        coreSender.sendMessage(new InitiateSessionRequest(clientId));
    }

    public void sendKillSession() {
        coreSender.sendMessage(new KillSessionRequest(clientId));
    }
}
