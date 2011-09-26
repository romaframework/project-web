package org.romaframework.web.session.domain.view;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.frontend.view.domain.activesession.ActiveSessionListable;

public class HttpActiveSessionListable extends ActiveSessionListable {

	public HttpActiveSessionListable(SessionInfo iEntity) {
		super(iEntity);
	}

	@Override
	@ViewField(render = ViewConstants.RENDER_DATETIME)
	public Date getLastAccessed() {
		try {
			return new Date(((HttpSession) session.getSystemSession()).getLastAccessedTime());
		} catch (Exception e) {
			// SESSION INVALIDATED, RETURN NO DATE
			return null;
		}
	}
}
