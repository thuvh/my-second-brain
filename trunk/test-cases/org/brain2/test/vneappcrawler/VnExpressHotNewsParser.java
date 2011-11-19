package org.brain2.test.vneappcrawler;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.brain2.ws.core.utils.FileUtils;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class VnExpressHotNewsParser {
	public static List<Article> parseHotArticle(){
		List<Article> list = new ArrayList<Article>(100);
				
		try {
			String json = FileUtils.readFileAsString("/importer-hot-news.json");
			JSONArray arr = new JSONArray(json);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject item = arr.getJSONObject(i);
				String feed = item.getString("feed");
				int id = item.getInt("id");
				
				list.addAll(parseHotArticleByCategory(id,feed));				
			}
			
		} catch (Exception e) {			
			e.printStackTrace();
		}	
		
		
		return list;
	}
	
	public static List<Article> parseHotArticleByCategory(int catId, String feedLink)  {		
		List<Article> list = new ArrayList<Article>(5);
		try {			
			String xml = HttpClientUtil.executeGet(feedLink);
			if(xml.isEmpty()){
				return list;
			}
			//System.out.println(xml);
			JSONObject jsonObject = XML.toJSONObject(xml);
			JSONArray items =  jsonObject.getJSONObject("XML").getJSONArray("I");
			
			VnExpressDao dao = VnExpressDao.getInstance();			
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");//11/19/2011 7:30:00 AM
			
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				String path = item.getString("P");
				String id = item.getString("I");
				
				System.out.println("#next hot article id = " + id);
				if(!dao.isExistedArticleByID(id)){
					Date date = new Date(formatter.parse(item.getString("D")).getTime());				
					Article article = new Article(id, item.getString("T"), item.getString("L"), path, 0, date, date);
					list.add(article);
				}
			}
		}  catch (Exception e) {			
			e.printStackTrace();
		}	
		System.out.println("#feedLink: " + feedLink + ", fetched: " + list.size());
		return list;
	}
	
	public static void main(String[] args) {
		System.out.println(parseHotArticle());
	}
}
