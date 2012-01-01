package org.brain2.ws.core;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brain2.ws.core.annotations.RestHandler;

public abstract class ServiceHandler {
	protected HttpServletRequest httpServletRequest;
	protected HttpServletResponse httpServletResponse;
	
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}
	
	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}
	
	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}
	
	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}
	
	@RestHandler
	public abstract String getServiceName(Map params );

}
