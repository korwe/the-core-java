package com.korwe.thecore.client;

import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.DataResponse;
import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class DataResponseHandler extends ResponseHandler {
    private final XStream xStream;

    protected DataResponseHandler(MessageResponseRegistry messageResponseRegistry, XStream xStream) {
        super(messageResponseRegistry);
        this.xStream = xStream;
    }

    @Override
    protected void handleResponse(CoreMessage message) {
        DataResponse dataResponse = (DataResponse) message;
        Object data = dataResponse.getData() == null ? null : xStream.fromXML(dataResponse.getData());
        messageResponseRegistry.registerDataResponse(dataResponse, data);
    }
}
