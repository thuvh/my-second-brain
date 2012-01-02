package org.brain2.ws.services.linkmarking;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.brain2.test.vneappcrawler.VnExpressImporter;
import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;
import org.brain2.ws.core.search.QueryLinkMetaData;
import org.brain2.ws.core.utils.StringUtil;
import org.json.JSONObject;


public class LinkDataHandler extends ServiceHandler{
	
	public LinkDataHandler() {
		
	}
	
	@RestHandler
	public String getServiceName(Map params) {		
		return this.getClass().getName();
	}
	
	@RestHandler
	public void editor(Map params ) throws Exception {
		Object action = params.get("action");
		//int limit = Integer.parseInt(params.get("limit")+"") ;
		if("importVnExpressArticles".equals(action)){
			VnExpressImporter.importVnExpressArticles();
		} else if("resumeImportErrorLinks".equals(action)){
			//VnExpressDao.resumeImportErrorLinks();
			System.out.println("TODO here");
		} else if("importHotArticles".equals(action)){
			VnExpressImporter.importHotArticles();
		}
	}
			
	@RestHandler
	public boolean save(Map params ) throws Exception {
		String functors = URLDecoder.decode(httpServletRequest.getParameter("functors"),"UTF-8");
		
		JSONObject functorsObj = new JSONObject(functors);
		System.out.println(functorsObj);
		
		JSONObject fPage = functorsObj.getJSONObject("F_Page");
		
		
		String url = fPage.getString("url");
		System.out.println("url: " + url );
		

		System.out.println("MD5: " + StringUtil.CRC32(url) );
		
		System.out.println("title: " + fPage.get("title"));
		System.out.println("description: " + fPage.get("description"));
		System.out.println("tags: " + fPage.get("tags").toString());
		//TODO 
		
//		IndexMetaData indexMetaData = new IndexMetaData();
//		indexMetaData.indexLink(url, params.get("title").toString(), params.get("description").toString(),  params.get("tags").toString());
				
		return true;
	}
	
	@RestHandler
	public List<Map<String,String>> search(Map params ) throws Exception {
		String keywords = URLDecoder.decode(params.get("keywords").toString(),"utf-8").trim();
		System.out.println("keywords: "+keywords);
		
		List<Map<String,String>> rs = new ArrayList<Map<String,String>>();
		QueryLinkMetaData theQuery = new QueryLinkMetaData();		
		
		List<Document> docs = theQuery.queryDocsByKeywords(keywords);		
		for (Document doc : docs) {
			Map<String,String> obj = new HashMap<String, String>(4);			
			obj.put("href", doc.get("href"));
			obj.put("title", doc.get("title"));
			obj.put("description", doc.get("description"));
			obj.put("tags", doc.get("tags"));
			System.out.println(obj.get("title"));
			rs.add(obj);
		}		
		return rs;
	}
	
	

	
}
