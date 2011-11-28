package org.brain2.test.vneappcrawler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Pattern;

public class VnExpressUtils {
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
	public static String getFullLink(String url){
		String tempURL = url.toLowerCase();
		if(Pattern.matches("^/gl/(.)*", tempURL)){
			if(Pattern.matches("^/gl/ebank/(.)*", tempURL))
				return DomainName.EBANKVNEXPRESS+url;
			if(Pattern.matches("^/gl/nha-dep/(.)*", tempURL))
				return DomainName.NHADEPVNEXPRESS+url;
			return DomainName.VNEXPRESS + url;
		}else{
			if(Pattern.matches("^/tin/(.)*", tempURL))
				return DomainName.SEAGAMEVNEXPRESS+url;
		}
		return "";
		
	}
	public static Parser getParser(String url){
		String tempURL = url.toLowerCase();
		if(Pattern.matches("^/gl/(.)*", tempURL)){
			if(Pattern.matches("^/gl/ebank/(.)*", tempURL))
				return new EbankVneParser();
			if(Pattern.matches("^/gl/nha-dep/(.)*", tempURL))
				return new NhaDepVneParser();
			return new VnExpressParser();
		}else{
			if(Pattern.matches("^/tin/(.)*", tempURL))
				return new SeagameVneParser();
		}
		return null;
	}
	public static int getIntTimeInSecond(Long timeMS){
		if(timeMS == null)
			return 0;
		return Long.valueOf(timeMS/1000L).intValue();
	}
	public static void main(String[] args) {
//		String hexMD5 = VnExpressUtils.md5("http://vnexpress.net/Files/Subject/3b/bb/c0/47/ket_xe_top1.jpg");
//		System.out.println("HEx String MD5 : " + hexMD5);
		
		String url ="/GL/Cuoi/Video/2008/05/3BA02B6F";
		System.out.println("Get Full link :"+VnExpressUtils.getFullLink(url));
		Parser parser = VnExpressUtils.getParser(url);
		boolean rightParser = parser instanceof VnExpressParser;
		System.out.println("Test Parser:"+rightParser);	
		
		url ="/gl/ebank/thi-truong/2011/11/sot-ty-gia-ngan-hang-dang-ha-nhiet";
		System.out.println("Get Full link :"+VnExpressUtils.getFullLink(url));
		parser = VnExpressUtils.getParser(url);
		rightParser = parser instanceof EbankVneParser;
		System.out.println("Test Parser:"+rightParser);	
		
		url ="/GL/Nha-dep/Khong-gian-song/2009/01/3BA0C7E1/";
		System.out.println("Get Full link :"+VnExpressUtils.getFullLink(url));
		parser = VnExpressUtils.getParser(url);
		rightParser = parser instanceof NhaDepVneParser;
		System.out.println("Test Parser:"+rightParser);	
		
		url ="/tin/hinh-anh-dep/2011/11/da-tiec-am-thanh-anh-sang-o-le-khai-mac-sea-games-26/";
		System.out.println("Get Full link :"+VnExpressUtils.getFullLink(url));
		parser = VnExpressUtils.getParser(url);
		rightParser = parser instanceof SeagameVneParser;
		System.out.println("Test Parser:"+rightParser);
		
	 long before = System.currentTimeMillis();
	 int after = VnExpressUtils.getIntTimeInSecond(before);
	 System.out.println("Before :"+before+"ms");
     System.out.println("After: "+after+"s");
     System.out.println("Before :"+new Date(before).toString());
     long restore = after *1000L;
     System.out.println("After :"+new Date(restore).toString());
		
	}

}
