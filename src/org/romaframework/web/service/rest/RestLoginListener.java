package org.romaframework.web.service.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.romaframework.aspect.authentication.LoginListener;
import org.romaframework.core.exception.UserException;

public class RestLoginListener implements LoginListener {

	private Object		service;
	private Method		invokeOperation;
	private Object[]	params;

	public RestLoginListener(Object service, Method invokeOperation, Object[] params) {
		this.invokeOperation = invokeOperation;
		this.params = params;
		this.service = service;
	}

	public void onError(Throwable t) {
		if (RuntimeException.class.equals(t.getClass()))
			t = t.getCause();
		throw new UserException(null, "$authentication.error", t);
	}

	public void onSuccess() {
		try {
			invokeOperation.invoke(service, params);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object getService() {
		return service;
	}

	public Method getInvokeOperation() {
		return invokeOperation;
	}

	public Object[] getParams() {
		return params;
	}

}
