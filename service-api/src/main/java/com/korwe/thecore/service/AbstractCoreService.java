package com.korwe.thecore.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractIdleService;
import com.korwe.thecore.api.*;
import com.korwe.thecore.exception.CoreException;
import com.korwe.thecore.exception.ErrorType;
import com.korwe.thecore.messages.CoreMessage;
import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */

public abstract class AbstractCoreService extends AbstractIdleService implements CoreMessageHandler {

    private static final String ERRORCODE_BAD_MESSAGE_TYPE = "system.protocol.badMessageType" ;
    private static final String ERRORCODE_BAD_SERVICE = "system.badService" ;
    private static final String ERRORCODE_BAD_FUNCTION = "system.bus.badFunction" ;
    private static final String BAD_MESSAGE_TYPE = "Message is not a service request";
    private static final String BAD_SERVICE = "The request is not for this service";
    private static final String BAD_FUNCTION = "Unsupported function: ";
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCoreService.class);

    private CoreSender responseSender;
    private CoreSender dataSender;
    private CoreSubscriber requestSubscriber;
    private XStream xStream;
    private ExecutorService executorService;
    private int maxThreads = 10;
    private CoreFactory coreFactory;

    protected AbstractCoreService(int maxThreads, CoreFactory coreFactory) {
        this(maxThreads, null, coreFactory);
    }

    protected AbstractCoreService(int maxThreads, XStream xStream, CoreFactory coreFactory) {
        this.coreFactory = coreFactory;
        if (maxThreads > 0) this.maxThreads = maxThreads;
        this.xStream = xStream == null ? new XStream() : xStream;
    }

    @Override
    public void handleMessage(final CoreMessage message) {
        executorService.execute(() -> {
            ServiceRequest request = toServiceRequest(message);
            if (null == request) {
                handleBadMessageType(message);
            }
            else if (!getServiceName().equals(message.getChoreography())) {
                handleBadRequest(request);
            }
            else {
                handleServiceRequest(request);
            }

        });
    }

    protected void handleBadMessageType(CoreMessage message) {
        LOG.error(BAD_MESSAGE_TYPE);
        ServiceResponse response = new ServiceResponse(message.getSessionId(), message.getGuid(), false, false);
        response.setErrorType(ErrorType.System);
        response.setErrorCode(ERRORCODE_BAD_MESSAGE_TYPE);
        response.setErrorMessage(BAD_MESSAGE_TYPE);
        sendResponse(response);
    }

    protected void handleBadRequest(ServiceRequest request) {
        LOG.error(BAD_SERVICE);
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorType(ErrorType.System);
        response.setErrorCode(ERRORCODE_BAD_SERVICE);
        response.setErrorMessage(BAD_SERVICE);
        sendResponse(response);
    }

    protected abstract void handleServiceRequest(ServiceRequest request);

    protected void handleUnsupportedFunctionRequest(ServiceRequest request) {
        LOG.error(BAD_FUNCTION + request.getFunction());
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorType(ErrorType.System);
        response.setErrorCode(ERRORCODE_BAD_FUNCTION);
        response.setErrorMessage(BAD_FUNCTION + request.getFunction());
        sendResponse(response);
    }

    protected void sendErrorResponse(ServiceRequest request, CoreException exception) {
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorType(exception.getErrorType());
        response.setErrorCode(exception.getErrorCode());
        response.setErrorMessage(Joiner.on('|').join(exception.getErrorVars()));
        response.setErrorVars(Lists.newArrayList(exception.getErrorVars()));
        sendResponse(response);
    }

    protected void sendSuccessDataResponses(ServiceRequest request, String data) {
        DataResponse dataResponse = new DataResponse(request.getSessionId(), request.getGuid(), data);
        sendData(dataResponse);
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), true, true);
        sendResponse(response);
    }

    protected void sendSuccessDataResponses(ServiceRequest request, Object o){
        sendSuccessDataResponses(request, xStream.toXML(o));
    }

    protected void sendSuccessResponse(ServiceRequest request) {
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), true, false);
        sendResponse(response);
    }


    protected ServiceRequest toServiceRequest(CoreMessage message) {
        if (CoreMessage.MessageType.ServiceRequest != message.getMessageType()) {
            return null;
        }
        ServiceRequest request = (ServiceRequest) message;
        return request;
    }

    protected void sendData(DataResponse dataResponse) {
        dataSender.sendMessage(dataResponse, dataResponse.getSessionId());
    }

    protected void sendResponse(ServiceResponse response) {
        responseSender.sendMessage(response);
    }

    /**
     * Start the service.
     */
    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting "+getServiceName());
        executorService = Executors.newFixedThreadPool(maxThreads);
        responseSender = coreFactory.createSender(MessageQueue.ServiceToCore, getServiceName());
        dataSender = coreFactory.createSender(MessageQueue.Data, getServiceName());
        requestSubscriber = coreFactory.createSubscriber(MessageQueue.CoreToService, getServiceName());
        requestSubscriber.connect(this);
    }

    /**
     * Stop the service.
     */
    @Override
    protected void shutDown() throws Exception {
        LOG.info("Stopping " + getServiceName());
        executorService.shutdown();
        requestSubscriber.close();
        dataSender.close();
        responseSender.close();
    }

    protected XStream getXStream(){
        return this.xStream;
    }

    public void setXStream(XStream xStream){
        this.xStream=xStream;
    }

    public String getServiceName() {
        return getClass().getSimpleName().substring(4);
    }

    protected abstract void handlePingRequest(ServiceRequest request);

    @Override
    protected String serviceName() {
        return getServiceName();
    }
}
