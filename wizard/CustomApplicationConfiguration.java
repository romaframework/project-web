package @project.package@;

import @project.package@.view.domain.HomePage;

import org.romaframework.aspect.flow.FlowAspect;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.command.impl.RedirectViewCommand;
import org.romaframework.core.Roma;
import org.romaframework.core.config.AbstractApplicationConfiguration;

/**
 * Application's configuration class valued by the Component Engine. <br/>Use the 'configuration' field map to store application specific parameters.
 * @author #{author}
 *
 */
public class CustomApplicationConfiguration extends AbstractApplicationConfiguration {
	public void startup() {
		// INSERT APPLICATION STARTUP HERE
	}

	public void shutdown() {
		// INSERT APPLICATION SHUTDOWN HERE
	}

	/**
	 * Callback called on every user connected to the application
	 */
	public void startUserSession() {
		Roma.aspect(FlowAspect.class).forward(new HomePage());
	}

	/**
	 * Callback called on every user disconnected from application
	 */
	public void endUserSession() {
	  Roma.aspect(ViewAspect.class).pushCommand(new RedirectViewCommand("dynamic/logout.jsp"));
	}

	public String getStatus() {
		return STATUS_UNKNOWN;
	}
}

