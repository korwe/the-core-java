package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.DataResponse;
import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class DataResponseHandler extends ResponseHandler {
    private final XStream xStream;

    protected DataResponseHandler(String clientId, MessageResponseRegistry messageResponseRegistry, XStream xStream) {
        super(clientId, messageResponseRegistry);
        this.xStream = xStream;
        coreSubscriber = new CoreSubscriber(MessageQueue.Data, clientId);
        coreSubscriber.connect(this);
    }

    @Override
    protected void handleResponse(CoreMessage message) {
        DataResponse dataResponse = (DataResponse) message;
        Object data = dataResponse.getData() == null ? null : xStream.fromXML(dataResponse.getData());
        messageResponseRegistry.registerDataResponse(dataResponse, data);
    }
}
