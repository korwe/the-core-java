package com.korwe.thecore.client;

import com.google.common.collect.Lists;
import com.korwe.thecore.messages.InitiateSessionRequest;
import com.korwe.thecore.messages.KillSessionRequest;
import com.korwe.thecore.messages.ServiceRequest;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Logger log = LoggerFactory.getLogger(this.getClass());


    public CoreClient(String clientId, XStream xStream) {
        this.clientId = clientId;
        this.xStream = xStream;
        messageResponseRegistry = new MessageResponseRegistry();
        requestProcessor = new RequestProcessor(clientId);
        serviceResponseHandler = new ServiceResponseHandler(clientId, messageResponseRegistry);
        dataResponseHandler = new DataResponseHandler(clientId, messageResponseRegistry, xStream);
    }

    public void initSession() {
        requestProcessor.sendInitiateSession();
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
            if (latch.getCount() > 0) {
                log.error("Latch timed out: {}", latch);
            }
            return messageResponseRegistry.getResults(serviceRequests);
        }
        catch (InterruptedException e) {
            // Log and ignore for now
            log.error("Latch await interrupted: {}", latch);
        }
        return null;
    }

    public void closeSession() {
        requestProcessor.sendKillSession();
    }

}
