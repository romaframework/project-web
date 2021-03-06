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

import javax.servlet.http.HttpSession;

import org.romaframework.frontend.view.domain.activesession.SessionAttributeInfo;

/**
 * SessionAttributeInfo implementation tracking changes with underline HttpSession.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public class HttpSessionAttributeInfo extends SessionAttributeInfo {
  private HttpSession session;

  public HttpSessionAttributeInfo(HttpSession iSession, String name) {
    super(name);
    session = iSession;
  }

  @Override
  public Object getValue() {
    return session.getAttribute(name);
  }

  @Override
  public void setValue(Object value) {
    session.setAttribute(name, value);
  }

  public HttpSession getSession() {
    return session;
  }
}
