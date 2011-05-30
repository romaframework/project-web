package org.romaframework.web.service.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.service.ServiceInfo;
import org.romaframework.aspect.service.UnmanagedServiceAspectAbstract;
import org.romaframework.aspect.service.feature.ServiceClassFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.config.ApplicationConfiguration;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassResolver;
import org.romaframework.core.schema.SchemaHelper;

public class RestServiceModule extends UnmanagedServiceAspectAbstract {

	protected Map<String, SchemaClass>	services;
	private static Log									log	= LogFactory.getLog(RestServiceModule.class);

	public RestServiceModule() {
		services = new HashMap<String, SchemaClass>();
	}

	public void startup() throws RuntimeException {
		discoveryServices(Utility.getApplicationAspectPackage(aspectName()));

		if (additionalPaths != null)
			for (String path : additionalPaths) {
				discoveryServices(path);
			}
	}

	public void shutdown() throws RuntimeException {
		if (services != null)
			services.clear();
		services = null;
	}

	public boolean existsServiceName(String serviceName) {
		return services.containsKey(serviceName);
	}

	/**
	 * Method to invoke a REST service using Roma Service Aspect
	 * 
	 * @param response
	 * @param request
	 * 
	 * @param serviceName
	 *          :- name of the service mapped by Annotation
	 * @param operation
	 *          :- name of the operation (method) to execute
	 * @param baseURI
	 *          :- the baseURI of the application
	 * @param parameters
	 *          :- the parameters needed by the requested operation
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * 
	 */
	public void invokeService(HttpServletRequest iRequest, HttpServletResponse iResponse, String serviceName, String operation, String... parameters)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnsupportedOperationException,
			SecurityException, NoSuchMethodException {
		Object service = createServiceInstance(services.get(serviceName));

		Method[] methods = service.getClass().getMethods();
		Method invokeOperation = null;

		int parameterLength = parameters.length;

		for (Method method : methods) {
			if (method.getName().equalsIgnoreCase(operation)) {
				if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0].isAssignableFrom(HttpServletRequest.class))
					parameterLength++;

				if (method.getParameterTypes().length > 1 && method.getParameterTypes()[1].isAssignableFrom(HttpServletResponse.class))
					parameterLength++;

				if (method.getParameterTypes().length == parameterLength) {
					// EXACT MATCH
					invokeOperation = method;
					break;
				}
			}
		}

		if (invokeOperation == null)
			throw new UnsupportedOperationException("Operation non existent");

		// CHECK FOR ADDITIONAL PARAMETERS (REQUEST & RESPONSE)
		List<Object> additionalParameters = new ArrayList<Object>();
		;
		if (invokeOperation.getParameterTypes().length > 0 && invokeOperation.getParameterTypes()[0].isAssignableFrom(HttpServletRequest.class)) {
			// ADD SERVLET REQUEST AS PARAMETER
			additionalParameters.add(iRequest);

			if (invokeOperation.getParameterTypes().length > 1 && invokeOperation.getParameterTypes()[1].isAssignableFrom(HttpServletResponse.class))
				additionalParameters.add(iResponse);
		}

		Class<?>[] parametersType = invokeOperation.getParameterTypes();
		Object[] params = getParameters(parametersType, parameters, additionalParameters);

		try {
			invokeOperation.invoke(service, params);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof SecurityException) {
				Roma.component(ApplicationConfiguration.class).login(new RestLoginListener(service, invokeOperation, params));
			} else
				log.error("[RestServiceModule.invokeService] Caught error on service execution", e.getCause());
		}
	}

	/**
	 * Method to transform the parameters form String to the operation requested type
	 * 
	 * @param iActionParameterTypes
	 * @param iServiceParameterValues
	 * @param iAdditionalParameters
	 * @return
	 */
	private Object[] getParameters(Class<?>[] iActionParameterTypes, String[] iServiceParameterValues, List<Object> iAdditionalParameters) {

		Object[] objectParameters = new Object[] {};

		if (iServiceParameterValues != null && iServiceParameterValues.length > 0) {
			for (int i = 0; i < iServiceParameterValues.length; i++) {
				iAdditionalParameters.add(SchemaHelper.assignValueToLiteral(iServiceParameterValues[i], iActionParameterTypes[i]));
			}
		}
		objectParameters = iAdditionalParameters.toArray(objectParameters);
		return objectParameters;
	}

	public Object getUnderlyingComponent() {
		return null;
	}

	public <T> T getClient(Class<T> iInterface, String iUrl) {
		throw new UnsupportedOperationException();
	}

	public HashMap<Class<?>, Object> getDefinitionMap() {
		throw new UnsupportedOperationException();
	}

	public List<Object> invokeDynamicService(String serviceURL, String operationName, List<Object> inputs) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public List<ServiceInfo> listOperations(String serviceURL) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	private void discoveryServices(String path) {
		Roma.component(SchemaClassResolver.class).addPackage(path);
		for (SchemaClass serviceClass : Roma.schema().getSchemaClassesByPackage(path)) {
			if (serviceClass.isInterface())
				continue;

			// Create our service implementation
			String serviceName = (String) serviceClass.getFeature(ServiceClassFeatures.SERVICE_NAME);
			Class<?> aspectImplementation = (Class<?>) serviceClass.getFeature(ServiceClassFeatures.ASPECT_IMPLEMENTATION);

			if (serviceName != null && serviceName.length() > 0 && (aspectImplementation == null || aspectImplementation.equals(getClass()))) {
				services.put(serviceName, serviceClass);
				log.info("[RestServiceModule] Registered service '" + serviceName + "' binded to class: " + serviceClass);
			}
		}
	}
}
