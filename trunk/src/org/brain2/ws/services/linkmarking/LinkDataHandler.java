package org.brain2.ws.services.linkmarking;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;
import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;
import org.brain2.ws.core.search.IndexMetaData;


public class LinkDataHandler extends ServiceHandler{
	
	public LinkDataHandler() {
		
	}
	
	@RestHandler
	public boolean save(Map params ) throws Exception {
		String href = URLDecoder.decode(params.get("href").toString(),"utf-8").toLowerCase();
		System.out.println("href: " + href );
		
		CRC32 crc32 = new CRC32();
		crc32.update(href.getBytes());
		
		System.out.println("CRC32: " + crc32.getValue() );
		
		byte[] bytesOfMessage = href.getBytes("UTF-8");

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(bytesOfMessage);
		 final String result = new String(Hex.encodeHex(thedigest));
		System.out.println("MD5: " + result );
		
		System.out.println("title: " + params.get("title"));
		System.out.println("description: " + params.get("description"));
		System.out.println("tags: " + params.get("tags"));
		//TODO 
		
		IndexMetaData indexMetaData = new IndexMetaData();
		indexMetaData.indexLink(href, params.get("title").toString(), params.get("description").toString(),  params.get("tags").toString());
				
		return true;
	}
	
	

	
}
