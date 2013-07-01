package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreMessageHandler;
import com.korwe.thecore.messages.CoreMessage;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public abstract class ResponseHandler implements CoreMessageHandler {

    protected MessageResponseRegistry messageResponseRegistry;

    protected ResponseHandler(MessageResponseRegistry messageResponseRegistry) {
        this.messageResponseRegistry = messageResponseRegistry;
    }

    protected abstract void handleResponse(CoreMessage message);

    @Override
    public void handleMessage(CoreMessage message) {
        if (messageResponseRegistry.expectsResponse(message.getGuid())) {
            handleResponse(message);
        }
        else {
            // Log and throw away
        }
    }
}
