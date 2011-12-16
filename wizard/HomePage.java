package @project.package@.view.domain;

import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewClass;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.frontend.domain.page.HomePageBasic;

public class HomePage extends HomePageBasic {

	public HomePage() {
	}

	@ViewField(render = ViewConstants.RENDER_URL, label = "", layout = "header")
	public String getHeader() {
		return "${application}${session}/dynamic/common/header.jsp";
	}

}