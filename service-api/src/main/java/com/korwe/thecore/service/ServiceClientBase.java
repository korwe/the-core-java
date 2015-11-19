package com.korwe.thecore.service;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.korwe.thecore.client.ClientServiceRequest;
import com.korwe.thecore.client.CoreClient;
import com.korwe.thecore.client.ServiceResult;
import com.korwe.thecore.exception.CoreServiceException;
import com.korwe.thecore.messages.ServiceResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */

/*  PingServiceClient*/
public class ServiceClientBase {

    private Class serviceClass;
    private CoreClient coreClient;
    private long timeout = 3000L;

    public ServiceClientBase(CoreClient coreClient, Class serviceClass) {
        this.coreClient = coreClient;
        this.serviceClass = serviceClass;
    }




    public Object makeDataRequest(String methodName, Map<String, Object> params) {
            return makeDataRequest(methodName, params, timeout);
    }

    public Object makeDataRequest(String methodName, Map<String, Object> params, Long timeout) {


        List<ClientServiceRequest> requests = new ArrayList<ClientServiceRequest>();
        ClientServiceRequest createUserRequest = new ClientServiceRequest(serviceClass.getSimpleName(), methodName, params);

        requests.add(createUserRequest);

        Map<String, ServiceResult> results = coreClient.makeRequests(timeout, requests);

        String key = createUserRequest.getGuid();

        ServiceResult serviceResult = results.get(key);

        validateResponse(serviceResult);

        return serviceResult.getData();
    }
    public void makeRequest(String methodName, Map<String, Object> params){
            makeRequest(methodName, params, timeout);
    }

    public void makeRequest(String methodName, Map<String, Object> params, Long timeout){
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


}
