package com.korwe.thecore.client;

import com.google.common.collect.Lists;
import com.korwe.thecore.messages.ServiceRequest;
import com.thoughtworks.xstream.XStream;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CoreClient {

    private String clientId;
    private RequestProcessor requestProcessor;
    private ServiceResponseHandler serviceResponseHandler;
    private DataResponseHandler dataResponseHandler;
    private XStream xStream;

    private MessageResponseRegistry messageResponseRegistry;


    public CoreClient(String clientId, XStream xStream) {
        this.clientId = clientId;
        this.xStream = xStream;
        messageResponseRegistry = new MessageResponseRegistry();
        requestProcessor = new RequestProcessor(clientId);
        serviceResponseHandler = new ServiceResponseHandler(messageResponseRegistry);
        dataResponseHandler = new DataResponseHandler(messageResponseRegistry, xStream);
    }

    public void initSession() {

    }

    public Map<String, ServiceResult> makeRequests(long timeoutMillis, ServiceRequest ... serviceRequests) {
        return makeRequests(timeoutMillis, Lists.newArrayList(serviceRequests));
    }

    public Map<String, ServiceResult> makeRequests(long timeoutMillis, Collection<ServiceRequest> serviceRequests) {
        CountDownLatch latch = new CountDownLatch(serviceRequests.size());
        try {
            requestProcessor.processRequests(serviceRequests, messageResponseRegistry, latch);
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            // Handle timeouts
            // Handle exceptions

            return messageResponseRegistry.getResults(serviceRequests);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeSession() {

    }

}
