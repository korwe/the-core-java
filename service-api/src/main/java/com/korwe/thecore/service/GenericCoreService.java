package com.korwe.thecore.service;

import com.google.common.collect.ImmutableMap;
import com.korwe.thecore.annotation.Location;
import com.korwe.thecore.annotation.ParamNames;
import com.korwe.thecore.dto.syndication.SyndicationEntry;
import com.korwe.thecore.exception.CoreException;
import com.korwe.thecore.exception.CoreSystemException;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.service.ping.CorePingService;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class GenericCoreService<S> extends CorePingService {

    private S delegate;
    private Map<String, ServiceFunction> functions;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Class<S> serviceClass;
    private String serviceName;


    @SuppressWarnings("unchecked")
    public GenericCoreService(S delegate, String serviceName, int maxThreads) {
        super(maxThreads);
        this.delegate = delegate;
        this.serviceName = serviceName;
        this.serviceClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.functions = createFunctionMap();
    }


    public GenericCoreService(S delegate, String serviceName, int maxThreads, XStream xStream) {
        super(maxThreads, xStream);
        this.delegate = delegate;
        this.serviceName = serviceName;
        this.serviceClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.functions = createFunctionMap();
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    protected void handleServiceRequest(ServiceRequest request) {
        String requestFunction = request.getFunction();
        ServiceFunction serviceFunction = functions.get(requestFunction);
        try {
            if (serviceFunction == null) {
                log.debug("Unsupported request {}", requestFunction);
                handleUnsupportedFunctionRequest(request);
            }
            else {
                Method method = serviceFunction.getMethod();
                String[] paramNames = serviceFunction.getParamNames();
                Annotation[][] paramAnnotations = method.getParameterAnnotations();

                int paramCount = paramNames.length;
                Object[] params = new Object[paramCount];
                for (int i = 0; i < paramCount; i++) {
                    String requestParam = request.getParameterValue(paramNames[i]);
                    if (requestParam == null || requestParam.isEmpty()) {
                        for (Annotation annotation :paramAnnotations[i]){
                            if (Location.class.equals(annotation.annotationType())){
                                params[i] = request.getLocation();
                            }
                            else {
                                params[i] = null;
                            }
                        }
                    }
                    else {
                        params[i] = getXStream().fromXML(requestParam);
                    }
                }
                try {
                    Object returnValue = method.invoke(delegate, params);
                    sendSuccessResponse(request, method.getReturnType(), returnValue);
                }
                catch (InvocationTargetException ite) {
                    if (ite.getCause() != null && ite.getCause() instanceof CoreException) {
                        throw (CoreException) ite.getCause();
                    }
                    else {
                        throw ite;
                    }
                }
            }
        }
        catch (CoreException e) {
            sendErrorResponse(request, e);
        }
        catch (Exception e) {
            log.error("Unexpected error", e);
            sendErrorResponse(request, new CoreSystemException(e, "system.unexpected"));
        }
    }

    private void sendSuccessResponse(ServiceRequest request, Class returnType, Object returnValue) {
        if (Void.TYPE.equals(returnType)) {
            sendSuccessResponse(request);
        }
        else {
            sendSuccessDataResponses(request, returnValue);
        }
    }

    private Map<String, ServiceFunction> createFunctionMap() {
        ImmutableMap.Builder<String, ServiceFunction> builder = new ImmutableMap.Builder<String, ServiceFunction>();
        Method[] interfaceMethods = serviceClass.getMethods();
        for (Method method : interfaceMethods) {
            ParamNames annotation = method.getAnnotation(ParamNames.class);
            String[] paramNames = annotation != null ? annotation.value() : new String[0];
            ServiceFunction serviceFunction = new ServiceFunction(method.getName(), method, paramNames);
            builder.put(serviceFunction.getName(), serviceFunction);
        }
        return builder.build();
    }

    private static class ServiceFunction {

        private String name;
        private Method method;
        private String[] paramNames;

        private ServiceFunction(String name, Method method, String[] paramNames) {
            this.name = name;
            this.method = method;
            this.paramNames = paramNames;
        }

        public String getName() {
            return name;
        }

        public Method getMethod() {
            return method;
        }

        public String[] getParamNames() {
            return paramNames;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ServiceFunction)) {
                return false;
            }

            ServiceFunction that = (ServiceFunction) o;

            if (!method.equals(that.method)) {
                return false;
            }
            if (!name.equals(that.name)) {
                return false;
            }
            if (!Arrays.equals(paramNames, that.paramNames)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + method.hashCode();
            result = 31 * result + (paramNames != null ? Arrays.hashCode(paramNames) : 0);
            return result;
        }

        public static void main(String[] args){
            XStream xStream = new XStream();
            Map map = new HashMap();
            map.put("a",10);
            map.put("b", 20);
            map.put("c", 30);
            map.put("d", 50);
            map.put("e", 60);
            SyndicationEntry syndicationEntry = new SyndicationEntry();
            syndicationEntry.setTitle("hi");
            syndicationEntry.setLink("hi");
            map.put("f", syndicationEntry);
            System.out.println(xStream.toXML(map));
            System.out.println(xStream.toXML(syndicationEntry));
        }
    }

}
