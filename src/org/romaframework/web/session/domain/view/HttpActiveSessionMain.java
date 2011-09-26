package org.romaframework.web.session.domain.view;

import java.util.Collection;

import org.romaframework.aspect.core.annotation.CoreClass;
import org.romaframework.aspect.flow.FlowAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.core.Roma;
import org.romaframework.frontend.view.domain.activesession.ActiveSessionListable;
import org.romaframework.frontend.view.domain.activesession.ActiveSessionMain;

@CoreClass(orderFields = "info sessions", orderActions = "view refresh sendMessage shutdown selectAll deselectAll")
public class HttpActiveSessionMain extends ActiveSessionMain {
	@Override
	protected void fillSessions() {
		Collection<SessionInfo> activeSessions = Roma.session().getSessionInfos();

		synchronized (activeSessions) {

			authenticatedSessions = 0;
			sessions.clear();
			for (SessionInfo s : activeSessions) {
				sessions.add(new HttpActiveSessionListable(s));

				if (s.getAccount() != null)
					authenticatedSessions++;
			}
		}

		Roma.fieldChanged(this, "authenticatedSessions");
		Roma.fieldChanged(this, "totalSessions");

		Roma.fieldChanged(this, "sessions");
	}

	@Override
	public void view() {
		Object[] sel = getSelection();
		if (sel == null || sel.length == 0)
			return;

		Roma.aspect(FlowAspect.class).forward(new HttpActiveSessionInstance(((ActiveSessionListable) sel[0]).getSession()));
	}
}
