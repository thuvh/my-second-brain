package org.brain2.ws.services.linkmarking;

import java.util.ArrayList;
import java.util.List;

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
	
	
}
