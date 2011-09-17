package org.brain2.ws.services.linkmarking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;


public class LinkDataHandler extends ServiceHandler{
	
	public LinkDataHandler() {
		
	}
	
	@RestHandler
	public List list() {
		List<String> list = new ArrayList<String>();


		return list;
	}
	
	@RestHandler
	public boolean post(String name) {
		System.out.println("name: " + name);
		return true;
	}
	
	@RestHandler
	public boolean save(String description, String href, String tags,  String title ) {
		System.out.println("href: " + href);
		System.out.println("title: " + title);
		System.out.println("description: " + description);
		System.out.println("tags: " + tags);
		return true;
	}
	
	@RestHandler
	public boolean save(Map params ) {
		System.out.println("href: " + params.get("href").toString() );
		System.out.println("title: " + params.get("title"));
		System.out.println("description: " + params.get("description"));
		System.out.println("tags: " + params.get("tags"));
				
		return true;
	}
	
	

	
}
