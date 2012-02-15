package org.brain2.crawler;

import java.net.URLDecoder;
import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;

public class CrawlerServiceHandler extends ServiceHandler{


	@RestHandler
	public boolean beginCrawling(Map params ) throws Exception {
		String href = URLDecoder.decode(params.get("href")+"","utf-8");
		System.out.println("href: " + href );
		return true;
	}


	@RestHandler
	public String getServiceName(Map params) {		
		return this.getClass().getName();
	}
}