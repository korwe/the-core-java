package com.korwe.thecore.service;

import com.google.common.base.Strings;
import com.korwe.thecore.messages.ServiceRequest;
import com.thoughtworks.xstream.XStream;

import java.lang.annotation.Annotation;
import java.util.Map;

public class MatchingParameterHandler extends ParameterHandler {

    private XStream xStream;

    public MatchingParameterHandler(XStream xStream) {this.xStream = xStream;}

    @Override
    protected void process(int currentIndex, Object[] params, Map<String, String> requestParams, String methodParamName,
                           Annotation[] paramAnnotations, ServiceRequest request) {
        final String paramValue = requestParams.get(methodParamName);
        params[currentIndex] = Strings.isNullOrEmpty(paramValue) ? null : xStream.fromXML(paramValue);
    }

    @Override protected boolean willProcess(Map<String, String> requestParams, String methodParamName,
                                            Annotation[] paramAnnotations) {
        return requestParams.containsKey(methodParamName);
    }

}
