package org.brain2.ws.core.utils;

import java.security.MessageDigest;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;

public class StringUtil {
	public static String CRC32(String s){
		try {
			CRC32 crc32 = new CRC32();
			crc32.update(s.getBytes());	
			byte[] bytesOfMessage = s.getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			return new String(Hex.encodeHex(thedigest));
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return "";
	}
	
	public static String html2text(String html) {
	    return Jsoup.parse(html).text();
	}
	
	
	
	public static void main(String[] args) {
		
		String hex = Integer.toHexString(2000000184);
		StringBuilder sb = new StringBuilder();
		int l = hex.length();
		for(int i= 0; i < l; i++){
			sb.append(hex.charAt(i));			
			if(i>0 && ((i+1) % 2 == 0)){
				sb.append("/");
			}			
		}
		
		String url ="/Files/Subject/" + sb.toString() +"12_12_Cliton.jpg";
		System.out.println(url);
	}

	public static String replace(String value, String returnNewLine, String newLine) {
		if(value == null) return "";
		return value.replace(returnNewLine, newLine);
	}
}
