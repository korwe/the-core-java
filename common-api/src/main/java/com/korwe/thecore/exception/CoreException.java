package com.korwe.thecore.exception;

import com.korwe.thecore.exception.ErrorType;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */

public abstract class CoreException extends RuntimeException {

    private String errorCode;
    private String[] errorVars;
    private ErrorType errorType;

    private String[] stringify(Object[] obs) {
        String[] strung = new String[obs.length];
        for(int i=0; i < strung.length; i++)
            strung[i]=String.valueOf(obs[i]);
        return strung;
    }

    public CoreException(String errorCode, String... errorVars) {
        super();
        this.errorCode = errorCode;
        this.errorVars = errorVars;
    }

    public CoreException(String errorCode, Object... errorVars) {
        super();
        this.errorCode = errorCode;
        this.errorVars = stringify(errorVars);
    }


    public CoreException(Throwable cause, String errorCode, String... errorVars) {
        super(cause);
        this.errorCode = errorCode;
        this.errorVars = errorVars;
    }

    public CoreException(Throwable cause, String errorCode, Object... errorVars) {
        super(cause);
        this.errorCode = errorCode;
        this.errorVars = stringify(errorVars);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String[] getErrorVars() {
        return errorVars;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    protected void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }
}
