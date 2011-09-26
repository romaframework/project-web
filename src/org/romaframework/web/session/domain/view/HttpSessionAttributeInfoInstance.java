package org.romaframework.web.session.domain.view;

import javax.servlet.http.HttpSession;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.frontend.view.domain.activesession.SessionAttributeInfoInstance;

public class HttpSessionAttributeInfoInstance extends SessionAttributeInfoInstance {

  public HttpSessionAttributeInfoInstance(HttpSessionAttributeInfo iAttribute) {
    super(iAttribute);
  }

  @Override
  @ViewAction(bind = AnnotationConstants.TRUE)
  public void save() {
    super.save();

    HttpSession session = ((HttpSessionAttributeInfo) entity).getSession();
    session.setAttribute(entity.getName(), entity.getValue());
  }

}
