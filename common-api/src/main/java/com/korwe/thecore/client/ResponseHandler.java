package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreFactory;
import com.korwe.thecore.api.CoreMessageHandler;
import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public abstract class ResponseHandler implements CoreMessageHandler {

    protected MessageResponseRegistry messageResponseRegistry;
    protected String clientId;
    protected CoreSubscriber coreSubscriber;
    protected CoreFactory coreFactory;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected ResponseHandler(String clientId, MessageResponseRegistry messageResponseRegistry, CoreFactory coreFactory) {
        this.clientId = clientId;
        this.messageResponseRegistry = messageResponseRegistry;
        this.coreFactory = coreFactory;
    }

    protected abstract void handleResponse(CoreMessage message);

    @Override
    public void handleMessage(CoreMessage message) {
        log.info("Message received: {}", message);
        if (messageResponseRegistry.expectsResponse(message.getGuid())) {
            log.debug("Response expected, handling it");
            handleResponse(message);
        }
    }

    public void close() {
        coreSubscriber.close();
    }

    protected abstract MessageQueue getMessageQueue();
}
