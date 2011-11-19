package org.brain2.test.xml;

import org.brain2.test.vneappcrawler.VnExpressDao;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;


public class XmlParserTest {

	public static void main(String args[]) throws Exception {
		//String str = "http://maps.google.com/maps/api/geocode/json?address="+ URLEncoder.encode("F31 cư xá Phú Lâm B, Phuong 13, Quan 6, Hochiminh, Vietnam ", "UTF-8")  +"&sensor=true";
		String str = "http://vnexpress.net/listfile/subject/00/sd_ts.xml";	
		String xml = HttpClientUtil.executeGet(str);
		System.out.println(xml);
		JSONObject jsonObject = XML.toJSONObject(xml);
		JSONArray items =  jsonObject.getJSONObject("XML").getJSONArray("I");
		
		VnExpressDao dao = VnExpressDao.getInstance();
		
		
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			String path = item.getString("P");
			
			System.out.print( item.getString("T") );
			System.out.print("\t" + path );
			System.out.print("\t" + item.getString("L") );
			System.out.println();
			
			
			
			int id =  dao.getHotArticleId(path);
			System.out.println(id);
		}		
	}
}