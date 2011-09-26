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
package org.romaframework.web.session.domain.view;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.romaframework.aspect.flow.FlowAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.core.Roma;
import org.romaframework.frontend.view.domain.activesession.ActiveSessionInstance;
import org.romaframework.frontend.view.domain.activesession.SessionAttributeInfo;

public class HttpActiveSessionInstance extends ActiveSessionInstance {
	public HttpActiveSessionInstance(SessionInfo iEntity) {
		super(iEntity);

		HttpSession sess = (HttpSession) entity.getSystemSession();

		// FILL ATTRIBUTES
		String attrName;
		attributes = new HashMap<String, SessionAttributeInfo>();
		Enumeration<?> e = sess.getAttributeNames();
		while (e.hasMoreElements()) {
			attrName = (String) e.nextElement();
			attributes.put(attrName, new HttpSessionAttributeInfo(sess, attrName));
		}

		Roma.fieldChanged(this, "attributes");
	}

	public void onAttributesAdd() {
		HttpSession session = (HttpSession) entity.getSystemSession();

		HttpSessionAttributeInfo instance = new HttpSessionAttributeInfo(session, "");

		Roma.aspect(FlowAspect.class).forward(new HttpSessionAttributeInfoInstance(instance));
	}

	public void onAttributesView() {
		onAttributesUpdate();
	}

	public void onAttributesUpdate() {
		if (attributeSelected == null)
			return;

		Roma.aspect(FlowAspect.class).forward(new HttpSessionAttributeInfoInstance((HttpSessionAttributeInfo) attributeSelected));
	}

	public void onAttributesRemove() {
		if (attributeSelected == null)
			return;

		HttpSession session = (HttpSession) entity.getSystemSession();
		session.removeAttribute(attributeSelected.getName());
	}
}
