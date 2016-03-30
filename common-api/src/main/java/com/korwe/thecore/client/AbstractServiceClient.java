package com.korwe.thecore.client;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.*;
import com.korwe.thecore.exception.CoreClientException;
import com.korwe.thecore.exception.CoreServiceException;
import com.korwe.thecore.messages.AbstractAsyncMessageContext;
import com.korwe.thecore.messages.AbstractMessageContext;
import com.korwe.thecore.messages.ServiceResponse;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */

/*  PingServiceClient*/
public abstract class AbstractServiceClient<MC extends AbstractAsyncMessageContext> {

    private Logger logger = LoggerFactory.getLogger(AbstractServiceClient.class);
    private Class serviceClass;
    protected Class<MC> msgCtxClass;
    protected Class serviceClientClass;
    private CoreClient coreClient;
    private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public AbstractServiceClient(CoreClient coreClient, Class serviceClass) {
        this.coreClient = coreClient;
        this.serviceClass = serviceClass;
        this.msgCtxClass = (Class<MC>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.serviceClientClass = (Class) ((ParameterizedType) msgCtxClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }




    public Object makeDataRequest(MC msgContext, String methodName, Map<String, Object> params) {
            return makeDataRequest(msgContext, methodName, params, msgContext.getTimeout());
    }

    public Object makeDataRequest(MC msgContext, String methodName, Map<String, Object> params, Long timeout) {
        if(msgContext.isAsync()){
            ListenableFuture<Object> data = executorService.submit(() -> doDataRequest(methodName, params, timeout));
            if(msgContext.getCallback() != null){
                Futures.addCallback(data, msgContext.getCallback());
            }
            return null;
        }
        else{
            return doDataRequest(methodName, params, msgContext.getTimeout());
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

        return serviceResult.getData();
    }

    public void makeRequest(MC msgContext, String methodName, Map<String, Object> params){
            makeRequest(msgContext, methodName, params, msgContext.getTimeout());
    }

    public void makeRequest(MC msgContext, String methodName, Map<String, Object> params, Long timeout){
        if(msgContext.isAsync()){
            ListenableFuture<Object> data = executorService.submit(() -> doDataRequest(methodName, params, timeout));
            if(msgContext.getCallback() != null){
                Futures.addCallback(data, msgContext.getCallback());
            }
        }
        else{
            doRequest(methodName, params, msgContext.getTimeout());
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


    public boolean ping(){
        return false;
    }

    public MC newContext(){
        try {
            return msgCtxClass.getConstructor(serviceClientClass).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("Could not instantiate class[{}]", msgCtxClass.getName(), e);
            throw new CoreClientException("messageContext.instantiate.fail", msgCtxClass.getName());
        }
    }
}
