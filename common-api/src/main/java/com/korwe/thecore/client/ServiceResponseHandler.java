package com.korwe.thecore.client;

import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.ServiceResponse;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ServiceResponseHandler extends ResponseHandler {

    protected ServiceResponseHandler(MessageResponseRegistry messageResponseRegistry) {
        super(messageResponseRegistry);
    }

    @Override
    protected void handleResponse(CoreMessage message) {
        messageResponseRegistry.registerServiceResponse((ServiceResponse) message);
    }
}
