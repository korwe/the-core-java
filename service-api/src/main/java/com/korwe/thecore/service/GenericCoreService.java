package com.korwe.thecore.service;

import com.google.common.collect.ImmutableMap;
import com.korwe.thecore.annotation.ParamNames;
import com.korwe.thecore.api.CoreFactory;
import com.korwe.thecore.exception.CoreException;
import com.korwe.thecore.exception.CoreSystemException;
import com.korwe.thecore.messages.ServiceRequest;
import com.korwe.thecore.service.ping.CorePingService;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
public class GenericCoreService<S> extends CorePingService {

    private S delegate;
    private Map<String, ServiceFunction> functions;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Class<S> serviceClass;
    private String serviceName;
    protected ParameterProcessor parameterProcessor;


    @SuppressWarnings("unchecked")
    public GenericCoreService(S delegate, String serviceName, int maxThreads, CoreFactory coreFactory) {
        this(delegate, serviceName, maxThreads, null, coreFactory);
    }

    @SuppressWarnings("unchecked")
    public GenericCoreService(S delegate, String serviceName, int maxThreads, XStream xStream, CoreFactory coreFactory) {
        super(maxThreads, xStream, coreFactory);
        this.delegate = delegate;
        this.serviceName = serviceName;
        this.serviceClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.functions = createFunctionMap();
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public Class getServiceClass(){
        return serviceClass;
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
                Object[] params = parameterProcessor.extractParameters(request, serviceFunction, method);
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

    @Override protected void startUp() throws Exception {
        super.startUp();
        this.parameterProcessor = new ParameterProcessor(new MatchingParameterHandler(getXStream()),
                                                         new LocationParameterHandler());
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

    public static class ServiceFunction {

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

    }

}
