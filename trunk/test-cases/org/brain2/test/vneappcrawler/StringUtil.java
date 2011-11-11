package org.brain2.test.vneappcrawler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {
	public static final String md5(final String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	 
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();
	 
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	public static void main(String[] args) {
		String hexMD5 = StringUtil.md5("http://vnexpress.net/Files/Subject/3b/bb/c0/47/ket_xe_top1.jpg");
		System.out.println("HEx String MD5 : " + hexMD5);
	}

}
