package com.qualica.pixiedust.service;

import com.korwe.thecore.messages.DataResponse;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.messages.ServiceResponse;
import com.korwe.thecore.service.ping.CorePingService;
import com.qualica.pixiedust.domain.Person;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CorePersonService extends CorePingService {

    private static final Logger LOG = Logger.getLogger(PersonService.class);
    private static final String SAVE_ERROR = "Save person failed";

    private PersonService personService = new PersonServiceImpl();

    @Override
    protected void handleServiceRequest(ServiceRequest request) {
        String function = request.getFunction();
        if ("Ping".equalsIgnoreCase(function)) {
            handlePingRequest(request);
        }
        else if ("SavePerson".equalsIgnoreCase(function)) {
            handleSavePersonRequest(request);
        }
        else {
            handleUnsupportedFunctionRequest(request);
        }
    }

    private void handleSavePersonRequest(ServiceRequest request) {
        String firstName = request.getParameterValue("firstName");
        String lastName = request.getParameterValue("lastName");
        String phoneNumber = request.getParameterValue("phoneNumber");
        Person person = personService.savePerson(firstName, lastName, phoneNumber);
        if (null == person) {
            sendErrorResponse(request);
        }
        else {
            sendSuccessResponses(request, person.getId());
        }
    }


    private void sendSuccessResponses(ServiceRequest request, Long personId) {
        DataResponse dataResponse = new DataResponse(request.getSessionId(), request.getGuid(), personId.toString());
        sendData(dataResponse);
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), true, true);
        sendResponse(response);
    }


    private void sendErrorResponse(ServiceRequest request) {
        LOG.error(SAVE_ERROR);
        ServiceResponse response = new ServiceResponse(request.getSessionId(), request.getGuid(), false, false);
        response.setErrorCode("SaveFailed");
        response.setErrorMessage(SAVE_ERROR);
        sendResponse(response);
    }

}
