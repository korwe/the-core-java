package com.korwe.thecore.exception;

import com.korwe.thecore.exception.CoreException;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class CoreSystemException extends CoreException {

    public CoreSystemException(String errorCode, String... errorVars) {
        super(errorCode, errorVars);
        setErrorType(ErrorType.System);
    }

    public CoreSystemException(Throwable cause, String errorCode, String... errorVars) {
        super(cause, errorCode, errorVars);
        setErrorType(ErrorType.System);
    }
}
