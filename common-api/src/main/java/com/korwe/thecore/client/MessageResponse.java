package com.korwe.thecore.client;

import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
* @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
*/
public class MessageResponse {
    private CountDownLatch latch;
    private final ServiceResult result;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public MessageResponse(CountDownLatch latch) {
        this.latch = latch;
        result = new ServiceResult();
    }

    public ServiceResult getResult() {
        return result;
    }

    public ServiceResponse getServiceResponse() {
        return result.getServiceResponse();
    }

    public void setServiceResponse(ServiceResponse serviceResponse) {
        synchronized (result) {
            result.setServiceResponse(serviceResponse);
            if (serviceResponse.isSuccessful()) {
                if (serviceResponse.hasData()) {
                    if (hasDataResponse()) {
                        latch.countDown();
                        log.debug("setServiceResponse: latch countdown: success & has data response");
                    }
                    else {
                        log.debug("setServiceResponse: no latch countdown: success and no data");
                    }
                }
                else {
                    latch.countDown();
                    log.debug("setServiceResponse: latch countdown: success & no data expected");
                }
            }
            else {
                latch.countDown();
                log.debug("setServiceResponse: latch countdown: unsuccessful");
            }
        }
    }

    public boolean hasServiceResponse() {
        return result.getServiceResponse() != null;
    }

    public DataResponse getDataResponse() {
        return result.getDataResponse();
    }

    public void setDataResponse(DataResponse dataResponse) {
        synchronized (result) {
            result.setDataResponse(dataResponse);
            if (hasServiceResponse()) {
                log.debug("setDataResponse: latch countdown: has service response");
                latch.countDown();
            }
            else {
                log.debug("setDataResponse: no latch countdown: no service response");
            }
        }
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

}
