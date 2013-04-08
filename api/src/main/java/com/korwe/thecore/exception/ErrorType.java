package com.korwe.thecore.exception;

/**
 * @author <a href="mailto:tjad.clark@korwe.com>Tjad Clark</a>
 */
public enum ErrorType {
    System(1001),
    Validation(1002),
    Service(1003),
    Client(1004);

    private final int errorCode;

    private ErrorType(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static ErrorType fromErrorCode(int errorCode) {
        switch (errorCode) {
            case 1001:
                return ErrorType.System;
            case 1002:
                return ErrorType.Validation;
            case 1003:
                return ErrorType.Service;
            case 1004:
                return ErrorType.Client;
            default:
                return null;
        }
    }

}
