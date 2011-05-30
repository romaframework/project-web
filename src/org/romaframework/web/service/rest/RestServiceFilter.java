/*
 *
 * Copyright 2009 Luca Molino (luca.molino--AT--assetdata.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.romaframework.web.service.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.core.Roma;

/**
 * @author molino
 * 
 */
public class RestServiceFilter implements Filter {

	protected RestServiceModule	restService;
	protected static Log				log										= LogFactory.getLog(RestServiceFilter.class);

	public void init(FilterConfig iConfig) throws ServletException {
		restService = Roma.component(RestServiceModule.class);
	}

	public void destroy() {
		restService.shutdown();
		restService = null;
	}

	public void doFilter(ServletRequest iRequest, ServletResponse iResponse, FilterChain iChain) throws IOException, ServletException {
		if (restService != null) {
			HttpServletRequest request = (HttpServletRequest) iRequest;

			String requestURI = request.getRequestURI();

			String path = request.getServletPath();
			if (path.length() > 1) {
				int pos = path.indexOf("/", 1);
				if (pos > -1) {
					path = path.substring(0, pos);
				}
			}

			String baseURI = request.getContextPath() + path;

			String serviceURI = getServiceCall(requestURI, baseURI);
			if (serviceURI != null && !(serviceURI.length() <= 0 || serviceURI.equals("/")) && request.getQueryString() == null
					&& serviceURI.length() > 0 && !serviceURI.endsWith(".jsp") && !serviceURI.endsWith(".html")
					&& !serviceURI.endsWith(".htm")) {

				// Service URL Syntax Check
				if (serviceURI.startsWith("/"))
					serviceURI = serviceURI.substring(1);
				String[] serviceCallParameters = serviceURI.split("/");
				if (serviceCallParameters.length < 2) {
					log.error("[RestServiceFilter] Error on service call syntax: " + baseURI + "/" + serviceURI + ". It should be " + baseURI
							+ "/<serviceName>/<operationName>/<parameter1>/.../<parameterN>");
					request.getSession().setAttribute(RestServiceConstants.SESSION_I18N_ERROR_ATTRIBUTE_NAME,
							"RestService.baseErrorMessage.error");
					return;
				}

				String serviceName = serviceCallParameters[0];
				String serviceOperation = serviceCallParameters[1];

				if (restService.existsServiceName(serviceName)) {
					request.getSession().setAttribute(RestServiceConstants.SESSION_SERVICE_ATTRIBUTE_NAME, serviceName);
					request.getSession().setAttribute(RestServiceConstants.SESSION_METHOD_ATTRIBUTE_NAME, serviceOperation);
					request.getSession().setAttribute(RestServiceConstants.SESSION_PARAMETERS_ATTRIBUTE_NAME,
							getParameters(serviceCallParameters));
				} else {
					request.getSession().setAttribute(RestServiceConstants.SESSION_I18N_ERROR_ATTRIBUTE_NAME,
							"RestService.serviceNotFound.error");
				}

				// iChain.doFilter(iRequest, iResponse);

				// } else {
				// RestServiceHelper.clearSession(request);
				// return;
			}
		} // else
		iChain.doFilter(iRequest, iResponse);
	}

	protected String getServiceCall(String requestURI, String baseURI) {
		int pos = requestURI.indexOf(baseURI);
		if (pos < 0)
			return null;
		return requestURI.substring(pos + baseURI.length());
	}

	protected String[] getParameters(String[] serviceCallParameters) {
		String[] parameters = new String[] {};
		List<String> parametersList = new ArrayList<String>();
		for (int i = 2; i < serviceCallParameters.length; i++) {
			parametersList.add(serviceCallParameters[i]);
		}
		return parametersList.toArray(parameters);
	}

}
