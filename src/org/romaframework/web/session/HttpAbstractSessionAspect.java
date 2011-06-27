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

package org.romaframework.web.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.session.SessionAspectAbstract;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.session.SessionListener;
import org.romaframework.core.exception.ConfigurationException;
import org.romaframework.core.flow.Controller;
import org.romaframework.core.flow.ObjectContext;

/**
 * Uses HTTP to identify user session aspects by current thread.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class HttpAbstractSessionAspect extends SessionAspectAbstract {

	public static final String					CONTEXT_REQUEST_PAR		= "$#$Http_Request$#$";
	public static final String					CONTEXT_RESPONSE_PAR	= "$#$Http_Response$#$";

	protected Map<String, SessionInfo>	sessions;
	protected static Log								log										= LogFactory.getLog(HttpAbstractSessionAspect.class);

	public HttpAbstractSessionAspect() {
		super();
		sessions = new HashMap<String, SessionInfo>();
	}

	public Object getActiveSystemSession() {
		return getServletRequest() == null ? null : getServletRequest().getSession();
	}

	public static HttpServletRequest getServletRequest() {
		return (HttpServletRequest) ObjectContext.getInstance().getContextComponent(HttpAbstractSessionAspect.CONTEXT_REQUEST_PAR);
	}

	public static HttpServletResponse getServletResponse() {
		return (HttpServletResponse) ObjectContext.getInstance().getContextComponent(HttpAbstractSessionAspect.CONTEXT_RESPONSE_PAR);
	}

	public SessionInfo getSession(Object iSystemSession) {
		return sessions.get(((HttpSession) iSystemSession).getId());
	}

	public SessionInfo removeSession(Object iSession) {
		SessionInfo sessInfo = null;
		String sessionId = ((HttpSession) iSession).getId();

		if (log.isDebugEnabled())
			log.debug("[HttpSessionAspect.removeSession] Removing session " + sessionId + "...");

		synchronized (sessions) {
			sessInfo = sessions.remove(sessionId);
		}

		if (sessInfo != null) {
			if (log.isDebugEnabled())
				log.debug("[HttpSessionAspect.removeSession] Removed session created: account=" + sessInfo.getAccount() + ", source=" + sessInfo.getSource()
						+ ", created=" + sessInfo.getCreated());
		} else {
			log.warn("[HttpSessionAspect.removeSession] Can't remove session because it doesn't registered: " + sessionId);
			return null;
		}

		List<SessionListener> listeners = Controller.getInstance().getListeners(SessionListener.class);

		synchronized (listeners) {
			for (SessionListener listener : listeners) {
				listener.onSessionDestroying(sessInfo);
			}
		}

		return sessInfo;
	}

	public Collection<SessionInfo> getSessionInfos() {
		return sessions.values();
	}

	public void destroyCurrentSession() {
		destroyCurrentSession(getActiveSessionInfo().getSystemSession());
	}

	public void destroyCurrentSession(Object iSystemSession) {
		((HttpSession) iSystemSession).invalidate();
	}

	/**
	 * Read the property from active HttpSession
	 */
	public <T> T getProperty(String iKey) {
		return getProperty(null, iKey);
	}

	/**
	 * Read the property from a HttpSession
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(Object iSession, String iKey) {
		HttpSession sess = null;

		if (iSession == null) {
			sess = (HttpSession) getActiveSystemSession();
			if (sess == null)
				throw new ConfigurationException("No active session found");
		} else {
			if (iSession instanceof SessionInfo)
				sess = (HttpSession) ((SessionInfo) iSession).getSystemSession();
			else
				sess = (HttpSession) iSession;
		}

		if (sess != null)
			return (T) sess.getAttribute(iKey);

		return null;
	}

	/**
	 * Set the property inside current HttpSession
	 */
	public <T> void setProperty(String iKey, T iValue) {
		setProperty(getActiveSystemSession(), iKey, iValue);
	}

	/**
	 * Set the property inside a HttpSession
	 */
	public <T> void setProperty(Object iSession, String iKey, T iValue) {
		HttpSession sess = (HttpSession) iSession;
		if (sess != null)
			sess.setAttribute(iKey, iValue);
	}

}
