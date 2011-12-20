package com.vnexpress.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brain2.ws.core.utils.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vnexpress.manager.VnExpressUtils;
import com.vnexpress.model.Article;
import com.vnexpress.model.Topic;

public class VneMapiParser {
	
	private static String accessTokens = "fd27e65094f068812fa326c83a4a2ed9";
	
	public static Article parseArticleFromAPI(long id){
		String url = "http://mapi.vnexpress.net/articles?method=get&ids="+id;
		String jsonStr = HttpClientUtil.executeGet(url);
		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			
			System.out.println("\n #articles:");
			JSONArray articles = jsonObject.getJSONObject("body").getJSONArray("articles");
			for (int i = 0; i < articles.length(); i++) {
				final JSONObject articleJson = articles.getJSONObject(i);
				long article_id = articleJson.getLong("article_id");
				String share_url = articleJson.getString("share_url");
				System.out.println(article_id);
				System.out.println(share_url);
				String html = HttpClientUtil.executeGet(share_url);
				boolean shouldUpdateArticle = articleJson.getString("content").isEmpty();
				
				if (html.isEmpty()||html.equals("500")) {	
					System.err.println("http get fail, 500 server error");				
				} else if(html.equals("404")){
					System.err.println("Link die!!!");
				} else {
					Parser parser = VnExpressUtils.getParser(share_url.replace("http://vnexpress.net", ""));
					if(parser!=null){
						Article oldArticle = new Article();
						oldArticle.setID(article_id);
						try {
							final Article newArticle = parser.parseHtmlToArticle(share_url, html, oldArticle , null);							
							System.out.println( "content:" + newArticle.getContent());
							List<Topic> topics = newArticle.getRefTopics();
							StringBuilder topicIds = new StringBuilder();
							for (Topic topic : topics) {
								topicIds.append(topic.getId()).append(",");
								System.out.println( "topic:" + topicIds);
							}
							updateArticle(id, newArticle.getContent(), topicIds.toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void updateArticle(long article_id, String content, String topics){
		String url = "http://trieunt.mapi.vnexpress.net/articles/?method=update";
		Map<String, String> params = new HashMap<String, String>(3);		
		params.put("article_id", article_id+"");
		params.put("content", "test ' aa chúc mừng năm mới");
		params.put("topics", topics);	
		
		String json = HttpClientUtil.executePost(url, params , accessTokens );
		System.out.println(json);
		
	}
	
	public static void main(String[] args) {
		parseArticleFromAPI(1000000037);
		
		System.exit(1);
		
		String url = "http://mapi.vnexpress.net/articles?method=get";
		String jsonStr = HttpClientUtil.executeGet(url);
		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			
			System.out.println(" #hot_articles:");
			JSONArray hot_articles = jsonObject.getJSONObject("body").getJSONArray("hot_articles");
			for (int i = 0; i < hot_articles.length(); i++) {
				JSONObject hot_article = hot_articles.getJSONObject(i);
				long article_id = hot_article.getLong("article_id");
				System.out.println(article_id);
				parseArticleFromAPI(article_id);
				//return;
			}
			
			System.out.println("\n #articles:");
			JSONArray articles = jsonObject.getJSONObject("body").getJSONArray("articles");
			for (int i = 0; i < articles.length(); i++) {
				JSONObject article = articles.getJSONObject(i);
				long article_id = article.getLong("article_id");
				System.out.println(article_id);
				parseArticleFromAPI(article_id);
			}			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
