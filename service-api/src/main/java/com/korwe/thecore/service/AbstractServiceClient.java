package com.korwe.thecore.service;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.*;
import com.korwe.thecore.client.AsyncClient;
import com.korwe.thecore.client.ClientServiceRequest;
import com.korwe.thecore.client.CoreClient;
import com.korwe.thecore.client.ServiceResult;
import com.korwe.thecore.exception.CoreServiceException;
import com.korwe.thecore.messages.ServiceResponse;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */

/*  PingServiceClient*/
public abstract class AbstractServiceClient<C> implements AsyncClient<C> {

    private static long ASYNC_TIMEOUT = 300000L;
    private Logger logger = LoggerFactory.getLogger(AbstractServiceClient.class);
    private Class serviceClass;
    private CoreClient coreClient;
    private long timeout = 3000L;
    private boolean isAsync = false;
    private FutureCallback callback;
    private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public AbstractServiceClient(CoreClient coreClient, Class serviceClass) {
        this.coreClient = coreClient;
        this.serviceClass = serviceClass;
    }




    public Object makeDataRequest(String methodName, Map<String, Object> params) {
            return makeDataRequest(methodName, params, timeout);
    }

    public Object makeDataRequest(String methodName, Map<String, Object> params, Long timeout) {
        if(isAsync){
            ListenableFuture<Object> data = executorService.submit(() -> doDataRequest(methodName, params, timeout));
            if(callback!=null){
                Futures.addCallback(data, callback);
            }
            return null;
        }
        else{
            return doDataRequest(methodName, params, timeout);
        }
    }

    private Object doDataRequest(String methodName, Map<String, Object> params, Long timeout) {
        List<ClientServiceRequest> requests = new ArrayList<ClientServiceRequest>();
        ClientServiceRequest createUserRequest = new ClientServiceRequest(serviceClass.getSimpleName(), methodName, params);

        requests.add(createUserRequest);

        Map<String, ServiceResult> results = coreClient.makeRequests(timeout, requests);

        String key = createUserRequest.getGuid();

        ServiceResult serviceResult = results.get(key);

        validateResponse(serviceResult);

        reset();

        return serviceResult.getData();
    }

    public void makeRequest(String methodName, Map<String, Object> params){
            makeRequest(methodName, params, timeout);
    }

    public void makeRequest(String methodName, Map<String, Object> params, Long timeout){
        if(isAsync){
            ListenableFuture<Object> data = executorService.submit(() -> doDataRequest(methodName, params, timeout));
            if(callback!=null){
                Futures.addCallback(data, callback);
            }
        }
        else{
            doRequest(methodName, params, timeout);
        }

    }

    public void doRequest(String methodName, Map<String, Object> params, Long timeout){
        List<ClientServiceRequest> requests = new ArrayList<ClientServiceRequest>();
        ClientServiceRequest createUserRequest = new ClientServiceRequest(serviceClass.getSimpleName(), methodName, params);

        requests.add(createUserRequest);

        Map<String, ServiceResult> results = coreClient.makeRequests(timeout, requests);

        String key = createUserRequest.getGuid();

        ServiceResult serviceResult = results.get(key);

        validateResponse(serviceResult);
        reset();

    }

    private void validateResponse(ServiceResult serviceResult) {
        ServiceResponse serviceResponse = serviceResult.getServiceResponse();
        if (serviceResponse == null){
            throw new CoreServiceException("noResponse");

        }else if(!serviceResponse.isSuccessful()) {
            String errorCode = serviceResponse.getErrorCode() == null ? "" : serviceResponse.getErrorCode();
            throw new CoreServiceException(serviceClass, errorCode, Collections2.transform(serviceResponse.getErrorVars(), new Function<Object, String>() {
                        @Override
                        public String apply(Object input) {
                            return String.valueOf(input);
                        }
                    }

            ).toArray(new String[]{}));
        }
    }

    public void reset(){
        this.callback = null;
        this.timeout = 3000L;
        this.isAsync = false;
    }

    public boolean ping(){
        return false;
    }

    public C withTimeout(Long timeout){
        logger.debug("Setting timeout to: {} ms", timeout);
        this.timeout = timeout;
        return (C)this;
    }

    @Override
    public C async(){
        this.isAsync = true;
        logger.debug("In async mode");
        return withTimeout(ASYNC_TIMEOUT);
    }

    @Override
    public C async(java.util.function.Function callback) {
        this.isAsync = true;

        this.callback = new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                logger.debug("Request succeeded, applying callback with result {}", result);
                callback.apply(result);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("Request failed, skipping callback");
                t.printStackTrace();
            }
        };

        return withTimeout(ASYNC_TIMEOUT);
    }
}
