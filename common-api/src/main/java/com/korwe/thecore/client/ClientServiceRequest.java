package com.korwe.thecore.client;

import com.google.common.collect.Maps;
import com.korwe.thecore.messages.ServiceRequest;

import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class ClientServiceRequest {

    private final ServiceRequest serviceRequest;
    private String coreServiceName;
    private String functionName;
    private String location;
    private Map<String, Object> parameters = Maps.newHashMap();

    public ClientServiceRequest(String coreServiceName, String functionName) {
        this.coreServiceName = coreServiceName;
        this.functionName = functionName;
        this.serviceRequest = new ServiceRequest(null, functionName);
        this.serviceRequest.setChoreography(coreServiceName);
    }

    public ClientServiceRequest(String coreServiceName, String functionName, String location) {
        this(coreServiceName, functionName);
        this.location = location;
        serviceRequest.setLocation(location);

    }

    public ClientServiceRequest(String coreServiceName, String functionName,
                                Map<String, Object> parameters) {
        this(coreServiceName, functionName);
        this.parameters = parameters;
    }

    public ClientServiceRequest(String coreServiceName, String functionName, String location,
                                Map<String, Object> parameters) {
        this(coreServiceName, functionName, location);
        this.parameters = parameters;
    }

    public String getCoreServiceName() {
        return coreServiceName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public String getGuid() {
        return serviceRequest.getGuid();
    }

    public ServiceRequest getServiceRequest(String clientId, ParamSerializationStrategy paramSerializationStrategy) {
        if (serviceRequest.getSessionId() == null) serviceRequest.setSessionId(clientId);
        for (String name : parameters.keySet()) {
            serviceRequest.setParameter(name, paramSerializationStrategy.serialize(parameters.get(name)));
        }
        return serviceRequest;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientServiceRequest{");
        sb.append("coreServiceName='").append(coreServiceName).append('\'');
        sb.append(", functionName='").append(functionName).append('\'');
        sb.append(", location='").append(location).append('\'');
        sb.append(", parameters=").append(parameters);
        sb.append('}');
        return sb.toString();
    }
}
