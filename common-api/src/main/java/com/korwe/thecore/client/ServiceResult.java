package com.korwe.thecore.client;

import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceResponse;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class ServiceResult {

    private ServiceResponse serviceResponse;
    private DataResponse dataResponse;
    private Object data;

    public void setServiceResponse(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    public void setDataResponse(DataResponse dataResponse) {
        this.dataResponse = dataResponse;
    }

    public ServiceResponse getServiceResponse() {
        return serviceResponse;
    }

    public DataResponse getDataResponse() {
        return dataResponse;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceResult{");
        sb.append("serviceResponse=").append(serviceResponse);
        sb.append(", dataResponse=").append(dataResponse);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
