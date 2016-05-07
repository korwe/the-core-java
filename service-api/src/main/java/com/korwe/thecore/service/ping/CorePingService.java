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

import com.korwe.thecore.api.*;
import com.korwe.thecore.messages.*;
import com.korwe.thecore.service.*;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CorePingService extends AbstractCoreService implements CoreMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CorePingService.class);

    private PingService pingService;

    public CorePingService(int maxThreads, CoreFactory coreFactory) {
        super(maxThreads, coreFactory);
    }

    public CorePingService(int maxThreads, XStream xStream, CoreFactory coreFactory) {
        super(maxThreads, xStream, coreFactory);
    }

    public CorePingService(PingService pingService, int maxThreads, CoreFactory coreFactory){
        super(maxThreads, coreFactory);
        this.pingService = pingService;
    }

    public CorePingService(PingService pingService, int maxThreads, XStream xStream, CoreFactory coreFactory){
        super(maxThreads, xStream, coreFactory);
        this.pingService = pingService;
    }

    protected void handleServiceRequest(ServiceRequest request) {
        if (!"Ping".equalsIgnoreCase(request.getFunction())) {
            handleUnsupportedFunctionRequest(request);
        }
        handlePingRequest(request);
    }

    protected void handlePingRequest(ServiceRequest request) {
        boolean pingResult = pingService == null || pingService.ping();
        ServiceResponse pingResponse = new ServiceResponse(request.getSessionId(), request.getGuid(), pingResult, true);
        sendResponse(pingResponse);
        String resultData = "<pingResult>" + pingResult + "</pingResult>";
        DataResponse dataResponse = new DataResponse(request.getSessionId(), request.getGuid(), resultData);
        sendData(dataResponse);
    }
}
