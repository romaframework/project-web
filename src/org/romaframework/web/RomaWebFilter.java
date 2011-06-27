/*
 * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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

package org.romaframework.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.core.GlobalConstants;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.config.RomaApplicationContext;
import org.romaframework.core.exception.ConfigurationException;
import org.romaframework.core.flow.ObjectContext;
import org.romaframework.web.session.HttpAbstractSessionAspect;

/**
 * Echo2Server implementation.
 */
public class RomaWebFilter implements Filter {

	private static final String	USER_AGENT	= "User-Agent";
	protected static Log				log					= LogFactory.getLog(RomaWebFilter.class);

	public void init(FilterConfig iConfig) throws ServletException {
		synchronized (getClass()) {

			long timeMillis = System.currentTimeMillis();

			log.info("[RomaWebFilter.init] Starting up Roma v." + GlobalConstants.VERSION + "...");

			// SET APPLICATION ABSOLUTE PATH
			String absolutePath = iConfig.getServletContext().getRealPath(Utility.PATH_SEPARATOR_STRING);
			log.info("[RomaWebFilter.init] ContextRoot: " + absolutePath);

			RomaApplicationContext.setApplicationPath(absolutePath);

			// CONFIGURE ROMA APPLICATION CONTEXT
			RomaApplicationContext.getInstance().startup();

			timeMillis = System.currentTimeMillis() - timeMillis;

			log.info("[RomaWebFilter.init] Startup completed in " + new SimpleDateFormat("mm:ss.S").format(new Date(timeMillis)) + ".");
		}
	}

	public void destroy() {
		log.warn("[RomaWebFilter.destroy] Shutdowing Roma...");
		RomaApplicationContext.getInstance().shutdown();
		log.warn("[RomaWebFilter.destroy] Shutdown completed.");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			ObjectContext.getInstance().setContextComponent(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR, request);
			ObjectContext.getInstance().setContextComponent(HttpAbstractSessionAspect.CONTEXT_RESPONSE_PAR, response);

			HttpSession sess = ((HttpServletRequest) request).getSession(false);
			if (sess == null) {
				sess = ((HttpServletRequest) request).getSession(true);

				SessionAspect sessionAspect = Roma.session();
				if (sessionAspect == null) {
					throw new ConfigurationException("No view aspect installed, install a view aspect!");
				}
				SessionInfo info = sessionAspect.addSession(sess);
				info.setSource(((HttpServletRequest) request).getHeader(USER_AGENT));
				info.setUserAgent(((HttpServletRequest) request).getRemoteHost() + ":" + ((HttpServletRequest) request).getRemotePort());
			}

			chain.doFilter(request, response);
		} finally {

			ObjectContext.getInstance().setContextComponent(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR, null);
			ObjectContext.getInstance().setContextComponent(HttpAbstractSessionAspect.CONTEXT_RESPONSE_PAR, null);
		}
	}
}
