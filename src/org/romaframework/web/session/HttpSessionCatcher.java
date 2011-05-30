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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.core.Roma;

/**
 * Catch HTTP session lifecycle.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public class HttpSessionCatcher implements HttpSessionListener {

	/**
	 * Catch when the session is created by the container.
	 */
	public void sessionCreated(HttpSessionEvent iEvent) {
	}

	/**
	 * Catch when the session is destroyed by the container.
	 */
	public void sessionDestroyed(HttpSessionEvent iEvent) {
		Roma.session().removeSession(iEvent.getSession());
	}
}
