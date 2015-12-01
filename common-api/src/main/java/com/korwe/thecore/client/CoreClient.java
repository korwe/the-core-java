package com.korwe.thecore.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class CoreClient {

    private String clientId;
    private RequestProcessor requestProcessor;
    private ServiceResponseHandler serviceResponseHandler;
    private DataResponseHandler dataResponseHandler;

    private MessageResponseRegistry messageResponseRegistry;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public CoreClient(String clientId, SerializationStrategy serializationStrategy) {
  //      CoreConfig.setConfigFilePath(".");
        this.clientId = clientId;
        messageResponseRegistry = new MessageResponseRegistry();
        requestProcessor = new RequestProcessor(clientId, serializationStrategy);
        serviceResponseHandler = new ServiceResponseHandler(clientId, messageResponseRegistry);
        dataResponseHandler = new DataResponseHandler(clientId, messageResponseRegistry, serializationStrategy);
    }

    public void initSession() {
        requestProcessor.sendInitiateSession();
    }

    public Map<String, ServiceResult> makeRequests(long timeoutMillis, ClientServiceRequest ... clientServiceRequests) {
        return makeRequests(timeoutMillis, Lists.newArrayList(clientServiceRequests));
    }

    public Map<String, ServiceResult> makeRequests(long timeoutMillis, Collection<ClientServiceRequest> clientServiceRequests) {
        CountDownLatch latch = new CountDownLatch(clientServiceRequests.size());
        try {
            requestProcessor.processRequests(clientServiceRequests, messageResponseRegistry, latch);
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            // Handle timeouts
            if (latch.getCount() > 0) {
                log.error("Latch timed out: {}", latch);
            }
            return messageResponseRegistry.getResults(clientServiceRequests);
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
