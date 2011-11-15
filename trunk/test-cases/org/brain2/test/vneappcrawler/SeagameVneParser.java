package org.brain2.test.vneappcrawler;

import java.util.Date;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.ReferenceObject.ReferenceType;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class SeagameVneParser extends MainParser{

	public static Article parseHtmlToArticle(String theLink, String html,
			Article article, VnExpressDao _vnExpressDao) {
		final Document doc = Jsoup.parse(html, HTTP.UTF_8);
		
		Elements contents = doc.select(".ctdt");

		if (contents.size() > 0) {
			Element content = contents.get(0);
			
			/**
			 * split related link from lead
			 */
			
			processLead(content,article);
			

			/**
			 * remove script links in content
			 */
			content.select("script").remove();
			content.select(".bortnd").remove();
			content.select(".sharend").remove();
			content.select(".time").remove();
			content.select(".Title").remove();
			content.select(".Lead").remove();
			
			
			content.select(".tkt").remove();
			content.select(".tag").remove();
			content.select(".ctkw").remove();
			
			/**
			 * Extract content
			 */
			
			/**
			 * Comment
			 */
			Elements commentws = content.select(".commentw");
			if(commentws.size()>0)
				getComment(commentws.get(0),article,_vnExpressDao,theLink);
			commentws.remove();
			
			/**
			 * Process page_2.asp and page_1.asp is a link in content
			 * Get Image from page_2.asp ,remove * Clip + video link
			 */
			Article exArticle = new Article();
			exArticle.setId(article.getId());
			processExtraPageLink(content,exArticle,".bodypage",".ctdt");
			
			/**
			 * Images
			 */
			
			extractIMG(content,article);
			article.addAll(exArticle.getRefObj());
			exArticle=null;

			/**
			 * process page_1.asp
			 * Get thumbnail 
			 * TODO : case : page_2.asp luu thumnail
			 */
			getThumbnail(theLink,article,".ctdt",130);
			
			/**
			 * Remove all , just get <p>
			 */
			for (Element p : content.select("p")) {
				p.html(Jsoup.parse(p.html()).text());
			}
			Whitelist whiteList = new Whitelist();
			whiteList.addTags("p");
			String newContent = Jsoup.clean(content.html(), whiteList);
			article.setContent(newContent);
			
		}
		return article;
	}
	public static void processLead(Element content,Article article){
		try {
			Elements leads = content.select("h2.Lead");
			
			if(leads.size()>0){
				
				String leadHtml = leads.get(0).html();
				String[] parts = leadHtml.split("<br",2);
				if(parts.length>0)
					leadHtml = parts[0];
				if(parts.length>=2){
					Document docPart2= Jsoup.parse(parts[1]);
					Elements relatedLinks= docPart2.select("a.Lead");
					if(relatedLinks.size()>0){
						for(Element reElement: relatedLinks){
							String href =  reElement.attr("href");
							ReferenceObject obj = new ReferenceObject(article.getId(), StringUtil.md5(href),href.replaceAll("http://vnexpress.net", ""), ReferenceType.RELATED_LINK, new Date());
							article.addRefObj(obj);
						}
						relatedLinks.remove();
					}
					String htmlPart2=docPart2.text().replaceAll("/", "");
					htmlPart2 = htmlPart2.replaceAll(">", "");
					leadHtml += htmlPart2;
				}
				
				article.setAbstractS(Jsoup.parse(leadHtml).text());
			}
		} catch (Exception e) {
			System.out.println("::::::PROCESS LEAD EXCEPTION::::::");
			e.printStackTrace();
		}
	}
	public static void processExtraPageLink(Element element,Article article,String parentContent,String parent){
		try {
			Elements exPageLinks= element.select("a[href~=page_[1,2,3].asp]");
			System.out.println("exPageLinks: "+exPageLinks.size());
			if(exPageLinks.size()>0){
				for(Element exPageLink:exPageLinks){
				
					String pageHtml = HttpClientUtil.executeGet(exPageLink.attr("href"));
					if (!pageHtml.isEmpty()&& !pageHtml.equals("500")&&!pageHtml.equals("404")) {
						Document docPage2 = Jsoup.parse(pageHtml, HTTP.UTF_8);
						Elements contents = docPage2.select(parentContent);
						if(contents.size()>0){
							Elements cpmsExPage = contents.select(parent);
							
							if(cpmsExPage.size()>0){
								Element cpmExPage = cpmsExPage.get(0);
								cpmExPage = removeElement(cpmExPage);
								Article extraArticle = new Article();
								extraArticle.setId(article.getId());
								
								processExtraPageLink(cpmExPage,extraArticle,parentContent,parent);
								
								extractIMG(cpmExPage,article);
								article.addAll(extraArticle.getRefObj());
								extraArticle=null;
								
							}
						}
						
					}
					exPageLink.parent().remove();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("::::processExtraPageLink EXCEPTION ::::: ");
		}
	}
	public static Element removeElement(Element element) {
		element.select("script").remove();
		element.select(".bortnd").remove();
		element.select(".sharend").remove();
		element.select(".time").remove();
		element.select(".Title").remove();
		element.select(".Lead").remove();
		
		element.select(".tkt").remove();
		element.select(".tag").remove();
		element.select(".ctkw").remove();
		element.select(".commentw").remove();
		return element;
	}

}
