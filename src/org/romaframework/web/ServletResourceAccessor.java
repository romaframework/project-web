package org.romaframework.web;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.servlet.ServletContext;

import org.romaframework.core.config.ResourceAccessor;

public class ServletResourceAccessor implements ResourceAccessor {

	private ServletContext	servletContext;

	public ServletResourceAccessor(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@SuppressWarnings("rawtypes")
	public Set getResourcePaths(String name) {
		return servletContext.getResourcePaths(name);
	}

	public InputStream getResourceAsStream(String name) {
		return servletContext.getResourceAsStream(name);
	}

	public URL getResource(String name) {
		try {
			return servletContext.getResource(name);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
