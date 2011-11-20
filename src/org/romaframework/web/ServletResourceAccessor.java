package org.romaframework.web;

import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletContext;

import org.romaframework.core.config.ResourceAccessor;

public class ServletResourceAccessor implements ResourceAccessor {

	private ServletContext	servletContext;

	public ServletResourceAccessor(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public Set getResourcePaths(String name) {
		return servletContext.getResourcePaths(name);
	}

	public InputStream getResourceAsStream(String name) {
		return servletContext.getResourceAsStream(name);
	}

}
