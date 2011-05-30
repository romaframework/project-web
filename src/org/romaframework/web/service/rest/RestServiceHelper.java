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
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.command.impl.RedirectViewCommand;
import org.romaframework.core.Roma;
import org.romaframework.core.config.ApplicationConfiguration;
import org.romaframework.frontend.RomaFrontend;
import org.romaframework.web.session.HttpAbstractSessionAspect;

/**
 * @author molino
 * 
 */
public class RestServiceHelper {

	private static final Log	log	= LogFactory.getLog(RestServiceHelper.class);

	public static void invokeRestService(HttpServletRequest request, HttpServletResponse response) {
		if (request.getSession().getAttribute(RestServiceConstants.SESSION_I18N_ERROR_ATTRIBUTE_NAME) != null) {
			String errorMessage = Roma.i18n().getString(
					(String) request.getSession().getAttribute(RestServiceConstants.SESSION_I18N_ERROR_ATTRIBUTE_NAME));
			sendToServiceErrorPage(errorMessage, null, request, response);
		} else if (request.getSession().getAttribute(RestServiceConstants.SESSION_SERVICE_ATTRIBUTE_NAME) != null) {
			invokeService(request, response, (String) request.getSession().getAttribute(
					RestServiceConstants.SESSION_SERVICE_ATTRIBUTE_NAME), (String) request.getSession().getAttribute(
					RestServiceConstants.SESSION_METHOD_ATTRIBUTE_NAME), (String[]) request.getSession().getAttribute(
					RestServiceConstants.SESSION_PARAMETERS_ATTRIBUTE_NAME));
		}
	}

	public static boolean existsServiceToInvoke(HttpServletRequest request) {
		return (request.getSession().getAttribute(RestServiceConstants.SESSION_I18N_ERROR_ATTRIBUTE_NAME) != null || request
				.getSession().getAttribute(RestServiceConstants.SESSION_SERVICE_ATTRIBUTE_NAME) != null);
	}

	public static void forwardToForm(Object iForm) {
		forwardToForm(iForm, null);
	}

	public static void forwardToForm(Object iForm, String iRealmName) {
		RomaFrontend.flow().forward(iForm, null, null, Roma.session().getActiveSessionInfo());

		// SessionInfo session = Roma.session().getActiveSessionInfo();
		// if (iForm != null) {
		// if (session == null) {
		// authenticate(iForm);
		// redirectToApplication(iRealmName);
		// } else {
		// redirectToApplication(iRealmName);
		// RomaFrontend.flow().forward(iForm, "screen://body", null, session);
		// }
		// }
	}

	public static void redirectToApplication(String realmName) {
		// SEND REDIRECT TO BACK URL
		HttpServletRequest request = HttpAbstractSessionAspect.getServletRequest();
		HttpServletResponse response = HttpAbstractSessionAspect.getServletResponse();
		try {
			String responseURL = Roma.component(ApplicationConfiguration.class).getConfiguration("publicURL");
			if (responseURL == null) {
				responseURL = request.getServletPath();
			} else if (responseURL.startsWith(request.getContextPath()))
				responseURL = responseURL.substring(request.getContextPath().length());

			// CONSIDER THE REALM IF ANY
			if (realmName != null)
				responseURL += "/" + realmName;

			request.getRequestDispatcher(responseURL).forward(request, response);
		} catch (IOException ioe) {
			log.error(ioe);
		} catch (ServletException se) {
			log.error(se);
		}
	}

	public static void clearSession(HttpServletRequest request) {
		request.getSession().removeAttribute(RestServiceConstants.SESSION_I18N_ERROR_ATTRIBUTE_NAME);
		request.getSession().removeAttribute(RestServiceConstants.SESSION_METHOD_ATTRIBUTE_NAME);
		request.getSession().removeAttribute(RestServiceConstants.SESSION_PARAMETERS_ATTRIBUTE_NAME);
		request.getSession().removeAttribute(RestServiceConstants.SESSION_SERVICE_ATTRIBUTE_NAME);
	}

	protected static void authenticate(Object iFirstFormToDisplay) {
		HttpServletRequest request = HttpAbstractSessionAspect.getServletRequest();
		HttpSession httpSession = request.getSession(true);
		httpSession.setAttribute("_Login.firstFormToDisplay", iFirstFormToDisplay);
	}

	protected static void invokeService(HttpServletRequest request, HttpServletResponse response, String serviceName,
			String serviceOperation, String[] parameters) {
		clearSession(request);
		try {
			RestServiceModule restServiceAspect = Roma.component(RestServiceModule.class);
			restServiceAspect.invokeService(request, response, serviceName, serviceOperation, parameters);
		} catch (InstantiationException ie) {
			log.error("Unable to instantiate the service " + serviceName, ie);
			sendToServiceErrorPage(Roma.i18n().getString("RestServiceHelper.baseErrorMessage.error"), ie, request, response);
		} catch (IllegalAccessException iae) {
			log.error("Unable to access the service " + serviceName, iae);
			sendToServiceErrorPage(Roma.i18n().getString("RestServiceHelper.baseErrorMessage.error"), iae, request, response);
		} catch (IllegalArgumentException iae) {
			log.error("Unable to execute the operation " + serviceOperation + " in service " + serviceName, iae);
			sendToServiceErrorPage(Roma.i18n().getString("RestServiceHelper.baseErrorMessage.error"), iae, request, response);
		} catch (InvocationTargetException ite) {
			log.error("Unable to execute the operation " + serviceOperation + " in service " + serviceName, ite);
			sendToServiceErrorPage(Roma.i18n().getString("RestServiceHelper.baseErrorMessage.error"), ite, request, response);
		} catch (UnsupportedOperationException uoe) {
			log.error("Unable to found the operation " + serviceOperation + " in service " + serviceName, uoe);
			sendToServiceErrorPage(Roma.i18n().getString("RestServiceHelper.operationNotFound.error"), uoe, request, response);
		} catch (NoSuchMethodException e) {
			log.error("Unable to found the operation " + serviceOperation + " in service " + serviceName, e);
			sendToServiceErrorPage(Roma.i18n().getString("RestServiceHelper.operationNotFound.error"), e, request, response);
		}
	}

	protected static void sendToServiceErrorPage(String errorMessage, Exception exception, HttpServletRequest request,
			HttpServletResponse response) {
		clearSession(request);
		request.getSession().setAttribute("ErrorMessage", errorMessage);
		request.getSession().setAttribute("ExceptionThrown", exception);

		String url = request.getContextPath() + "/dynamic/common/serviceError.jsp";
		try {
			// TRY APP REDIRECT IF AVAILABLE
			Roma.aspect(ViewAspect.class).pushCommand(new RedirectViewCommand(url));
		} catch (Exception e) {
			// TRY HTTP REDIRECT
			try {
				response.sendRedirect(url);
			} catch (IOException e1) {
				log.error("Can't redirect to error page", e1);
			}
		} finally {
			request.getSession().invalidate();
		}
	}
}
