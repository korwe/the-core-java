package com.korwe.thecore.service;

import com.korwe.thecore.messages.ServiceRequest;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class ParameterHandler {

    private ParameterHandler next;

    public void processParameter(int currentIndex, Object[] params, Map<String, String> requestParams, String methodParamName,
                                 Annotation[] paramAnnotations, ServiceRequest request) {
        if (willProcess(requestParams, methodParamName, paramAnnotations)) {
            process(currentIndex, params, requestParams, methodParamName, paramAnnotations, request);
        }
        else if (hasNext()) {
            next.processParameter(currentIndex, params, requestParams, methodParamName, paramAnnotations, request);
        }
    }

    protected abstract void process(int currentIndex, Object[] params, Map<String, String> requestParams, String methodParamName,
                                     Annotation[] paramAnnotations, ServiceRequest request);

    protected abstract boolean willProcess(Map<String, String> requestParams, String methodParamName,
                                           Annotation[] paramAnnotations);

    protected ParameterHandler getNext() {
        return next;
    }

    protected void setNext(ParameterHandler next) {
        this.next = next;
    }

    protected boolean hasNext() {
        return next != null;
    }
}
