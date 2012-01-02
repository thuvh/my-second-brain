package org.brain2.ws.core.utils;

import java.security.MessageDigest;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;

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
}
