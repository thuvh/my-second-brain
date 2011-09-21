package org.brain2.ws.services.linkmarking;

import java.net.URLDecoder;
import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;


public class LinkDataHandler extends ServiceHandler{
	
	public LinkDataHandler() {
		
	}
	
	@RestHandler
	public boolean save(Map params ) throws Exception {
		String href = URLDecoder.decode(params.get("href").toString(),"utf-8");
		System.out.println("href: " + href );
		System.out.println("title: " + params.get("title"));
		System.out.println("description: " + params.get("description"));
		System.out.println("tags: " + params.get("tags"));
				
		return true;
	}
	
	

	
}
