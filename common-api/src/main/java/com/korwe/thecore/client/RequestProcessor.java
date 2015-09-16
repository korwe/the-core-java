package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
* @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
*/
public class RequestProcessor {

    private final String clientId;
    private final SerializationStrategy serializationStrategy;
    private CoreSender coreSender;

    public RequestProcessor(String clientId, SerializationStrategy serializationStrategy) {
        this.clientId = clientId;
        this.serializationStrategy = serializationStrategy;
        coreSender = new CoreSender(MessageQueue.ClientToCore);
    }

    public void processRequests(Iterable<ClientServiceRequest> clientServiceRequests,
                                MessageResponseRegistry messageResponseRegistry, CountDownLatch latch) {
        for (ClientServiceRequest clientServiceRequest : clientServiceRequests) {
            ServiceRequest serviceRequest = clientServiceRequest.getServiceRequest(clientId, serializationStrategy);
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
