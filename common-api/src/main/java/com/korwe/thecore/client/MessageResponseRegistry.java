package com.korwe.thecore.client;

import com.google.common.collect.Maps;
import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class MessageResponseRegistry {

    private ConcurrentMap<String, MessageResponse> registry;

    public MessageResponseRegistry() {
        registry = new ConcurrentHashMap<String, MessageResponse>();
    }

    public boolean expectsResponse(String guid) {
        return registry.containsKey(guid);
    }

    public void registerRequest(String guid, CountDownLatch latch) {
        registry.put(guid, new MessageResponse(latch));
    }

    public void registerDataResponse(DataResponse dataResponse, Object data) {
        MessageResponse response = registry.get(dataResponse.getGuid());
        if (response != null) {
            response.setDataResponse(dataResponse);
            response.setData(data);
            if (response.hasServiceResponse()) {
                response.countDown();
            }
        }
    }

    public void registerServiceResponse(ServiceResponse serviceResponse) {
        MessageResponse response = registry.get(serviceResponse.getGuid());
        if (response != null) {
            response.setServiceResponse(serviceResponse);
            if (serviceResponse.isSuccessful()) {
                if (serviceResponse.hasData()) {
                    if (response.hasDataResponse()) {
                        response.countDown();
                    }
                }
                else {
                    response.countDown();
                }
            }
            else {
                response.countDown();
            }
        }
    }

    public Map<String, ServiceResult> getResults(Collection<ServiceRequest> serviceRequests) {
        Map<String, ServiceResult> results = Maps.newHashMap();
        for (ServiceRequest serviceRequest : serviceRequests) {
            String guid = serviceRequest.getGuid();
            if (this.expectsResponse(guid)) {
                MessageResponse messageResponse = registry.remove(guid);
                results.put(guid, messageResponse.getResult());
            }
        }
        return results;
    }
}
