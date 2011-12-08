package org.brain2.ws.services.infocrawler;

import java.net.URLDecoder;
import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;

public class InfoCrawlerHandler extends ServiceHandler{
	
	@RestHandler
	public String getServiceName(Map params) {		
		return this.getClass().getName();
	}

	@RestHandler
	public boolean beginCrawling(Map params ) throws Exception {
		String href = URLDecoder.decode(params.get("href")+"","utf-8");
		System.out.println("href: " + href );
		return true;
	}
}
