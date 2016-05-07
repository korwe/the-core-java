package com.korwe.thecore.client;

import com.korwe.thecore.api.CoreFactory;
import com.korwe.thecore.api.CoreSubscriber;
import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.DataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class DataResponseHandler extends ResponseHandler {
    private static final MessageQueue MESSAGE_QUEUE = MessageQueue.Data;

    private final ParamSerializationStrategy paramSerializationStrategy;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected DataResponseHandler(String clientId, MessageResponseRegistry messageResponseRegistry,
                                  ParamSerializationStrategy paramSerializationStrategy, CoreFactory coreFactory) {

        super(clientId, messageResponseRegistry, coreFactory);
        this.paramSerializationStrategy = paramSerializationStrategy;
        coreSubscriber = coreFactory.createSubscriber(MESSAGE_QUEUE, clientId);
        coreSubscriber.connect(this);
    }

    @Override
    protected void handleResponse(CoreMessage message) {
        log.debug("Handling data response: {}", message.getGuid());
        DataResponse dataResponse = (DataResponse) message;
        Object data = dataResponse.getData() == null ? null : paramSerializationStrategy.deserialize(dataResponse.getData());
        messageResponseRegistry.registerDataResponse(dataResponse, data);
    }

    @Override
    protected MessageQueue getMessageQueue() {
        return MESSAGE_QUEUE;
    }
}
