package org.romaframework.web.view;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.view.ViewHelper;
import org.romaframework.core.util.FileUtils;

public class HttpUtils {

	protected static Log	log	= LogFactory.getLog(HttpUtils.class);

	public static void noCache(HttpServletResponse iResponse) {
		iResponse.setHeader("Pragma", "no-cache");
		iResponse.addHeader("Cache-Control", "must-revalidate");
		iResponse.addHeader("Cache-Control", "no-cache");
		iResponse.addHeader("Cache-Control", "no-store");
		iResponse.setDateHeader("Expires", 0);
	}

	public static StringBuilder loadUrlResource(String url, boolean propagateSession, HttpServletRequest request) {
		StringBuilder buffer = null;
		HttpURLConnection connection = null;
		try {
			if (url.startsWith("classpath:")) {
				url = url.substring("classpath:".length());
				try {
					buffer = FileUtils.readStreamAsText(ViewHelper.class.getResourceAsStream(url));
				} catch (Throwable e) {
					log.error("[URLRendering.setContent] Error on loading resource from classpath", e);
				}
			} else {
				URLConnection conn = new URL(url).openConnection();
				if (conn instanceof HttpURLConnection) {
					connection = (HttpURLConnection) conn;

					if (propagateSession) {
						// PROPAGATE ALL THE COOKIES (AND THEREFORE ALSO THE HTTP SESSION) ALLOWING THE SHARING OF OBJECTS BETWEEN POJO AND JSP
						for (Cookie c : request.getCookies()) {
							if (c.getName().equals("JSESSIONID")) {
								connection.setRequestProperty("Cookie", c.getName() + "=" + c.getValue());
								break;
							}
						}
					}

					connection.connect();
					buffer = FileUtils.readStreamAsText(connection.getInputStream());
				}

			}
		} catch (Exception e) {
			// DO NOTHING
			log.error("[URLRendering.setContent] Error on loading resource from URL", e);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return buffer;
	}

	public static final String	VAR_CLIENT				= "${client}";
	public static final String	VAR_LOCALHOST			= "${localhost}";
	public static final String	VAR_APPLICATION		= "${application}";
	public static final String	VAR_SESSION				= "${session}";
	public static final String	VAR_URL_CLASSPATH	= "classpath:";
	public static final String	VAR_URL_HTTP			= "http://";
}
