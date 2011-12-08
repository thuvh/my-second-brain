package org.brain2.ws.core;

import java.util.Map;

import org.brain2.ws.core.annotations.RestHandler;

public abstract class ServiceHandler {
	@RestHandler
	public abstract String getServiceName(Map params );

}
