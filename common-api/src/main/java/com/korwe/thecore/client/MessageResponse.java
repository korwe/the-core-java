package com.korwe.thecore.client;

import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceResponse;

import java.util.concurrent.CountDownLatch;

/**
* @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
*/
public class MessageResponse {
    private CountDownLatch latch;
    private ServiceResult result;

    public MessageResponse(CountDownLatch latch) {
        this.latch = latch;
        result = new ServiceResult();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public ServiceResult getResult() {
        return result;
    }

    public void setResult(ServiceResult result) {
        this.result = result;
    }

    public ServiceResponse getServiceResponse() {
        return result.getServiceResponse();
    }

    public void setServiceResponse(ServiceResponse serviceResponse) {
        result.setServiceResponse(serviceResponse);
    }

    public boolean hasServiceResponse() {
        return result.getServiceResponse() != null;
    }

    public DataResponse getDataResponse() {
        return result.getDataResponse();
    }

    public void setDataResponse(DataResponse dataResponse) {
        result.setDataResponse(dataResponse);
    }

    public boolean hasDataResponse() {
        return result.getDataResponse() != null;
    }

    public void setData(Object data) {
        result.setData(data);
    }

    public Object getData() {
        return result.getData();
    }

    public void countDown() {
        latch.countDown();
    }
}
