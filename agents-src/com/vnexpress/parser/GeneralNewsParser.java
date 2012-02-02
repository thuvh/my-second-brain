package com.vnexpress.parser;

import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vnexpress.dao.VnExpressDao;
import com.vnexpress.model.Article;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class GeneralNewsParser extends MainParser{
	
	public Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) throws Exception {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
					
		article.setContent(ArticleExtractor.INSTANCE.getText(html));
				
		return article;
	}
	
}
