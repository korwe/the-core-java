package com.korwe.thecore.service;

import com.korwe.thecore.annotation.Location;
import com.korwe.thecore.messages.ServiceRequest;

import java.lang.annotation.Annotation;
import java.util.Map;

public class LocationParameterHandler extends ParameterHandler {
    @Override
    protected void process(int currentIndex, Object[] params, Map<String, String> requestParams, String methodParamName,
                           Annotation[] paramAnnotations, ServiceRequest request) {
        for (Annotation annotation : paramAnnotations) {
            if (Location.class.equals(annotation.annotationType())) {
                params[currentIndex] = request.getLocation();
            }
        }
    }

    @Override protected boolean willProcess(Map<String, String> requestParams, String methodParamName,
                                            Annotation[] paramAnnotations) {
        return !requestParams.containsKey(methodParamName);
    }
}
