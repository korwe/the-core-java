package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.ServiceResponse;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ServiceResponseHandler extends ResponseHandler {

    protected ServiceResponseHandler(String clientId, MessageResponseRegistry messageResponseRegistry) {
        super(clientId, messageResponseRegistry);
        coreSubscriber = new CoreSubscriber(MessageQueue.CoreToClient, clientId);
        coreSubscriber.connect(this);
    }

    @Override
    protected void handleResponse(CoreMessage message) {
        messageResponseRegistry.registerServiceResponse((ServiceResponse) message);
    }
}
