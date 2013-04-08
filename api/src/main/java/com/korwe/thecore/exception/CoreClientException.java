package com.korwe.thecore.exception;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class CoreClientException extends CoreException {

    public CoreClientException(String errorCode, String... errorVars) {
        super(errorCode, errorVars);
        setErrorType(ErrorType.Client);
    }

    public CoreClientException(Throwable cause, String errorCode, String... errorVars) {
        super(cause, errorCode, errorVars);
        setErrorType(ErrorType.Client);
    }
}
