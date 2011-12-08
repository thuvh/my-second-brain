package org.brain2.ws.services.hello;

import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;

public class HelloWorldHandler extends ServiceHandler {

	@Override
	@RestHandler
	public String getServiceName(Map params) {		
		return this.getClass().getName();
	}
	
	@RestHandler
	public String sayHi(Map params) {	
		//to call: request http://localhost:10001/hello/sayHi?name=Trieu Nguyen
		return "Hello, " + params.get("name");
	}

}
