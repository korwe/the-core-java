package com.korwe.thecore.exception;

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
}
