package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreFactory;
import com.korwe.thecore.api.CoreSender;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.*;

import java.util.concurrent.CountDownLatch;

/**
* @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
*/
public class RequestProcessor {

    private final String clientId;
    private final ParamSerializationStrategy paramSerializationStrategy;
    private CoreFactory coreFactory;
    private CoreSender coreSender;


    public RequestProcessor(String clientId, ParamSerializationStrategy paramSerializationStrategy, CoreFactory coreFactory) {
        this.clientId = clientId;
        this.paramSerializationStrategy = paramSerializationStrategy;
        this.coreFactory = coreFactory;
        coreSender = coreFactory.createSender(MessageQueue.ClientToCore, clientId);
    }

    public void processRequests(Iterable<ClientServiceRequest> clientServiceRequests,
                                MessageResponseRegistry messageResponseRegistry, CountDownLatch latch) {
        for (ClientServiceRequest clientServiceRequest : clientServiceRequests) {
            ServiceRequest serviceRequest = clientServiceRequest.getServiceRequest(clientId, paramSerializationStrategy);
            messageResponseRegistry.registerRequest(serviceRequest.getGuid(), latch);
            coreSender.sendMessage(serviceRequest);
        }
    }

    public void sendInitiateSession() {
        coreSender.sendMessage(new InitiateSessionRequest(clientId));
    }

    public void sendKillSession() {
        coreSender.sendMessage(new KillSessionRequest(clientId));
        coreSender.close();
    }
}
