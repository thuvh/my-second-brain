package org.brain2.test.parser.dynamic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.FileUtils;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.gson.Gson;
import com.vnexpress.manager.ImporterConfigs;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class MyParser {
	Function<Element, String> processRow = new Function<Element, String>() {
		public String apply(Element row) {
			String rs = "";
			Elements imgs = row.select("img");
			if (imgs.size() == 1) {
				rs = imgs.get(0).attr("src");
				System.out.println(rs);
			} else {
				rs = row.text();
				System.out.println(rs);
			}
			return rs;
		}
	};

	public String doParsing(String url) {

		String html = HttpClientUtil.executeGet(url);

		try {
			String plainContent = ArticleExtractor.INSTANCE.getText(html);
			System.out.println("#RS1: \n "+plainContent + "\n");
		} catch (BoilerpipeProcessingException e) {			
			e.printStackTrace();
		}

		Document doc = Jsoup.parse(html, HTTP.UTF_8);
		
		try {
			String filePath = "/q-paths/vnexpress.net.json";
			String json = FileUtils.readFileAsString(filePath);
			System.out.println(json);
			
			
			JSONObject jsonObject = new JSONObject(json);

			Iterator keys = jsonObject.keys();
			while(keys.hasNext()){
				String key = keys.next().toString();
				System.out.println(key);
				System.out.println(jsonObject.getString(key));
			}			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Elements contents = doc.select(".content");
		Elements tables = doc.select("table");
		System.out.println("tables.size():" + tables.size());
		Collection<String> arr = new ArrayList<String>();
		for (int i = 0; i < tables.size(); i++) {
			Element table = tables.get(i);
			Elements rows = table.select("tr");

			arr.addAll(Collections2.transform(rows, processRow));

			for (int j = 0; j < rows.size(); j++) {
				Element row = rows.get(j);

			}
		}
		System.out.println(arr);

		if (contents.size() > 0) {
			Element content = contents.get(0);
			return content.text();
		}
		return "";
	}

	// http://localhost:10001/vne-data/parseArticle/string?id=1000509759&forceupdate=true&path=/gl/ban-doc-viet/the-gioi/2011/04/ngam-hoa-anh-dao-o-tokyo/
	public static void main(String[] args) {

	

		// MyParser parser = new MyParser();
		// parser.doParsing("http://vnexpress.net/gl/vi-tinh/giai-tri/2011/12/10-clip-quang-cao-gay-sot-tren-youtube-nam-2011/");
		// parser.doParsing("http://vnexpress.net/gl/ban-doc-viet/the-gioi/2011/04/ngam-hoa-anh-dao-o-tokyo/");

	}
}
