package com.korwe.thecore.exception;

import com.korwe.thecore.service.PingService;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class CoreServiceException extends CoreException {

    public CoreServiceException(String errorCode, String... errorVars) {
        super(errorCode, errorVars);
        setErrorType(ErrorType.Service);
    }

    public CoreServiceException(Throwable cause, String errorCode, String... errorVars) {
        super(cause, errorCode, errorVars);
        setErrorType(ErrorType.Service);
    }

    public CoreServiceException(Class<? extends PingService> serviceInterface, String errorCode, String... errorVars) {
        super(serviceInterface.getName()+"."+errorCode, errorVars);
        setErrorType(ErrorType.Service);
    }
}
