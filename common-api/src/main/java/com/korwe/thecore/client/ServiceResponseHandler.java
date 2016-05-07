package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreFactory;
import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class ServiceResponseHandler extends ResponseHandler {

    private static final MessageQueue MESSAGE_QUEUE = MessageQueue.CoreToClient;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected ServiceResponseHandler(String clientId, MessageResponseRegistry messageResponseRegistry, CoreFactory coreFactory) {
        super(clientId, messageResponseRegistry, coreFactory);
        coreSubscriber = coreFactory.createSubscriber(MESSAGE_QUEUE, clientId);
        coreSubscriber.connect(this);
    }

    @Override
    protected void handleResponse(CoreMessage message) {
        log.debug("Handling service response: {}", message.getGuid());
        messageResponseRegistry.registerServiceResponse((ServiceResponse) message);
    }

    @Override
    protected MessageQueue getMessageQueue() {
        return MESSAGE_QUEUE;
    }
}
