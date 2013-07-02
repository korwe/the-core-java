package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreMessageHandler;
import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.messages.CoreMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public abstract class ResponseHandler implements CoreMessageHandler {

    protected MessageResponseRegistry messageResponseRegistry;
    protected String clientId;
    protected CoreSubscriber coreSubscriber;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected ResponseHandler(String clientId, MessageResponseRegistry messageResponseRegistry) {
        this.clientId = clientId;
        this.messageResponseRegistry = messageResponseRegistry;
    }

    protected abstract void handleResponse(CoreMessage message);

    @Override
    public void handleMessage(CoreMessage message) {
        if (messageResponseRegistry.expectsResponse(message.getGuid())) {
            handleResponse(message);
        }
        else {
            log.info("Message received: {}", message);
        }
    }
}
