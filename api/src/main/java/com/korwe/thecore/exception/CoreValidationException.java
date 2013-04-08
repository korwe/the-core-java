package com.korwe.thecore.exception;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class CoreValidationException extends CoreException{

    public CoreValidationException(String errorCode, String... errorVars) {
        super(errorCode, errorVars);
        setErrorType(ErrorType.Validation);
    }

    public CoreValidationException(Throwable cause, String errorCode, String... errorVars) {
        super(cause, errorCode, errorVars);
        setErrorType(ErrorType.Validation);
    }
}
