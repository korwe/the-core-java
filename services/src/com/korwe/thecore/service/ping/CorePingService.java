/*
 * Copyright (c) 2011.  Korwe Software
 *
 *  This file is part of TheCore.
 *
 *  TheCore is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TheCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with TheCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.korwe.thecore.service.ping;

import com.google.common.util.concurrent.AbstractIdleService;
import com.korwe.thecore.api.*;
import com.korwe.thecore.messages.*;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CorePingService extends AbstractIdleService implements CoreMessageHandler {

    private static final Logger LOG = Logger.getLogger(CorePingService.class);
    private static final String BAD_MESSAGE_TYPE = "Message is not a service request";
    private static final String BAD_SERVICE = "The request is not for this service";
    private static final String BAD_FUNCTION = "Unsupported function: ";

    private CoreSender responseSender;
    private CoreSender dataSender;
    private CoreSubscriber requestSubscriber;
    private PingService pingService = new PingServiceImpl();

    @Override
    public void handleMessage(CoreMessage message) {
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
    }

    protected void handleBadMessageType(CoreMessage message) {
        LOG.error(BAD_MESSAGE_TYPE);
        ServiceResponse response = new ServiceResponse(message.getSessionId(), message.getGuid(), false, false);
        response.setErrorCode("BadMessageType");
        response.setErrorMessage(BAD_MESSAGE_TYPE);
        sendResponse(response);
    }

    protected void handleBadRequest(ServiceRequest request) {
        LOG.error(BAD_SERVICE);
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorCode("BadService");
        response.setErrorMessage(BAD_SERVICE);
        sendResponse(response);
    }

    protected void handleServiceRequest(ServiceRequest request) {
        if (!"Ping".equalsIgnoreCase(request.getFunction())) {
            handleUnsupportedFunctionRequest(request);
        }
        handlePingRequest(request);
    }

    protected void handleUnsupportedFunctionRequest(ServiceRequest request) {
        LOG.error(BAD_FUNCTION + request.getFunction());
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorCode("BadFunction");
        response.setErrorMessage(BAD_FUNCTION + request.getFunction());
        sendResponse(response);
    }

    protected ServiceRequest toServiceRequest(CoreMessage message) {
        if (CoreMessage.MessageType.ServiceRequest != message.getMessageType()) {
            return null;
        }
        ServiceRequest request = (ServiceRequest) message;
        return request;
    }

    protected void handlePingRequest(ServiceRequest request) {
        boolean pingResult = pingService.ping();
        ServiceResponse pingResponse = new ServiceResponse(request.getSessionId(), request.getGuid(), pingResult, true);
        sendResponse(pingResponse);
        String resultData = "<pingResult>" + pingResult + "</pingResult>";
        DataResponse dataResponse = new DataResponse(request.getSessionId(), request.getGuid(), resultData);
        sendData(dataResponse);
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
        LOG.info(getServiceName() + " starting");
        responseSender = new CoreSender(MessageQueue.ServiceToCore);
        dataSender = new CoreSender(MessageQueue.Data);
        requestSubscriber = new CoreSubscriber(MessageQueue.CoreToService, getServiceName());
        requestSubscriber.connect(this);
    }

    /**
     * Stop the service.
     */
    @Override
    protected void shutDown() throws Exception {
        LOG.info(getServiceName() + " stopping");
        requestSubscriber.close();
        dataSender.close();
        responseSender.close();
    }

    public String getServiceName() {
        return getClass().getSimpleName().substring(4);
    }
}
