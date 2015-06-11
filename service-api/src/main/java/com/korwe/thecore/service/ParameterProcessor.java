package com.korwe.thecore.service;

import com.google.common.collect.Iterators;
import com.korwe.thecore.messages.ServiceRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ParameterProcessor {

    private ParameterHandler handlerChain;

    public ParameterProcessor(ParameterHandler handler) {
        this.handlerChain = handler;
    }

    public ParameterProcessor(ParameterHandler... handlers) {
        for (final ParameterHandler handler : handlers) {
            addHandler(handler);
        }
    }

    public void addHandler(ParameterHandler handler) {
        ParameterHandler chain = handlerChain;
        if (chain == null) {
            handlerChain = handler;
        }
        else {
            while (chain.hasNext()) {
                chain = chain.getNext();
            }
            chain.setNext(handler);
        }
    }

    public Object[] extractParameters(ServiceRequest request, GenericCoreService.ServiceFunction serviceFunction,
                                      Method method) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        String[] methodParamNames = serviceFunction.getParamNames();
        String[] requestParamNames = Iterators.toArray(request.getParameterNames(), String.class);
        Map<String, String> requestParams = new HashMap<>();
        for (final String name : requestParamNames) {
            requestParams.put(name, request.getParameterValue(name));
        }

        int paramCount = methodParamNames.length;
        Object[] params = new Object[paramCount];
        for (int i = 0; i < methodParamNames.length; i++) {
            final String paramName = methodParamNames[i];
            handlerChain.processParameter(i, params, requestParams, paramName, paramAnnotations[i], request);
        }
        return params;

    }
}
