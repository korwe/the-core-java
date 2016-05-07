package com.korwe.thecore.client;

import com.google.common.collect.Maps;
import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class MessageResponseRegistry {

    private ConcurrentMap<String, MessageResponse> registry;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public MessageResponseRegistry() {
        registry = new ConcurrentHashMap<>();
    }

    public boolean expectsResponse(String guid) {
        return registry.containsKey(guid);
    }

    public void registerRequest(String guid, CountDownLatch latch) {
        registry.put(guid, new MessageResponse(latch));
    }

    public void registerDataResponse(DataResponse dataResponse, Object data) {
        MessageResponse response = registry.get(dataResponse.getGuid());
        log.debug("registerDataResponse: MessageResponse for {}: {}", dataResponse.getGuid(), response);
        if (response != null) {
            response.setDataResponse(dataResponse);
            response.setData(data);
        }
    }

    public void registerServiceResponse(ServiceResponse serviceResponse) {
        MessageResponse response = registry.get(serviceResponse.getGuid());
        log.debug("registerServiceResponse: MessageResponse for {}: {}", serviceResponse.getGuid(), response);
        if (response != null) {
            response.setServiceResponse(serviceResponse);
        }
    }

    public Map<String, ServiceResult> getResults(Iterable<ClientServiceRequest> clientServiceRequests) {
        Map<String, ServiceResult> results = Maps.newHashMap();
        for (ClientServiceRequest clientServiceRequest : clientServiceRequests) {
            String guid = clientServiceRequest.getGuid();
            if (this.expectsResponse(guid)) {
                MessageResponse messageResponse = registry.remove(guid);
                results.put(guid, messageResponse.getResult());
            }
        }
        return results;
    }
}
