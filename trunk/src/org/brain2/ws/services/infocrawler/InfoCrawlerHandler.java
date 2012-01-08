package org.brain2.ws.services.infocrawler;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;
import org.brain2.ws.core.utils.ServletUtils;
import org.brain2.ws.core.utils.StringUtil;
import org.json.JSONObject;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

public class InfoCrawlerHandler extends ServiceHandler {
	public static final String JS_MIMETYPE = "application/javascript";

	// In the class declaration section:
	private DropboxAPI<WebAuthSession> mDBApi;
	final static private String APP_KEY = "8d2kln7x9xpchnj";
	final static private String APP_SECRET = "g3afhlbffx395ep";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	AccessTokenPair accessToken;
	String uid = null;
	Map<String, Entry> functorsCache = new java.util.HashMap<>();	
	List<String> functorsPaths = null;
	
	public InfoCrawlerHandler(){
		System.out.println("init InfoCrawlerHandler");
		// And later in some initialization function:
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
		mDBApi = new DropboxAPI<WebAuthSession>(session);
	}
	

	@RestHandler
	public String getServiceName(Map params) {
		return this.getClass().getName();
	}
	
	@RestHandler
	public List<String> getFunctors(Map params) throws Exception {
		if(accessToken == null){			
			initDropboxSession("getFunctorsCache/json");
		}
		WebAuthSession session = mDBApi.getSession();
		if(session.isLinked() && functorsPaths == null ){
								
			//list files in /Public/database/
			Entry existingEntry = mDBApi.metadata("/Public/database/", 1000, null, true, null);			
			List<Entry> entries = existingEntry.contents;
			functorsPaths = new ArrayList<>(entries.size());
			for (Entry entry : entries) {
				if(JS_MIMETYPE.equals(entry.mimeType)){		
					String fileName = entry.fileName();
					if(fileName.startsWith("functor-")){
						functorsCache.put(fileName, entry);
						String publicAccessUrl = "http://dl.dropbox.com/u/" + uid + "/database/" + entry.fileName();
						System.out.println("functors publicAccessUrl: " + publicAccessUrl);
						functorsPaths.add(publicAccessUrl);
					}
				}
			}					
			return functorsPaths;
		}
		return functorsPaths ;
	}
	
	public void initDropboxSession(String oauth_callback) {
		try {
			WebAuthSession session = mDBApi.getSession();

			WebAuthInfo authInfo = session.getAuthInfo();
			System.out.println("authInfo.url: " + authInfo.url);
			
			accessToken = session.getAccessTokenPair();
			System.out.println("accessToken.key "+accessToken.key);
			System.out.println("accessToken.secret "+accessToken.secret);

			String url = authInfo.url
					+ "&oauth_callback="
					+ URLEncoder.encode("http://localhost:10001/infocrawler/" + oauth_callback,"UTF-8");
			System.out.println("url: " + url);
			httpServletResponse.sendRedirect(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void requestDropboxSession(Map params) {

		try {
			WebAuthSession session = mDBApi.getSession();

			WebAuthInfo authInfo = session.getAuthInfo();
			System.out.println("authInfo.url: " + authInfo.url);
			
			accessToken = session.getAccessTokenPair();
			System.out.println("accessToken.key "+accessToken.key);
			System.out.println("accessToken.secret "+accessToken.secret);

			String url = authInfo.url
					+ "&oauth_callback="
					+ URLEncoder
							.encode("http://localhost:10001/infocrawler/postAuthorizeDropbox/html?path=parser-admin",
									"UTF-8");
			System.out.println("url: " + url);
			httpServletResponse.sendRedirect(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	ProgressListener dropboxFileListener = new ProgressListener(){
		@Override
		public void onProgress(long bytesSent, long total) {
			int percent = (int) ((bytesSent * 100)/total);			
			System.out.println(bytesSent + "-" + total + " %= " + percent);			
		}
	};
	
	public Entry addNewEntryToDropbox(Map params) {
		try {
			String functorsStr = URLDecoder.decode(httpServletRequest.getParameter("functors"),"UTF-8");
			JSONObject functorsObj = new JSONObject(functorsStr);
			System.out.println(functorsObj);
							
			JSONObject fPage = functorsObj.getJSONObject("F_Page");
			
			String url = fPage.getString("url");
			System.out.println("url: " + url );
			
			if(accessToken == null){
				httpServletResponse.sendRedirect("http://localhost:10001/infocrawler/requestDropboxSession/html?path=parser-admin");
				//TODO redirect to this action
				return null;
			}
			
			// Uploading a sample functor.
			if(mDBApi.getSession().isLinked()){						
				String fileContents = "functorsCallback( " + functorsObj.toString() + " );";
				byte[] data = fileContents.getBytes();
				String fileName = "functor-F_Page-"+ StringUtil.CRC32(url) +".js";
				Entry  entry = functorsCache.get(fileName);
				String path = "/Public/database/functor-F_Page-"+ StringUtil.CRC32(url) +".js";					
				if(entry != null){
					entry = mDBApi.putFile(path, new ByteArrayInputStream(data), data.length, entry.rev, dropboxFileListener);
					System.out.println("The uploaded file's rev is: " + entry.rev);	
				} else {									
					entry = mDBApi.putFile(path, new ByteArrayInputStream(data), data.length, null, dropboxFileListener);
					System.out.println("The uploaded file's rev is: " + entry.rev);					
				}				
				return entry;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return null;
	}

	public void postAuthorizeDropbox(Map params) throws DropboxException {
		try {	
			WebAuthSession session = mDBApi.getSession();
			if(accessToken == null){
				httpServletResponse.sendRedirect("http://localhost:10001/infocrawler/requestDropboxSession/html?path=parser-admin");
				return;
			}
			if(uid == null){				
				RequestTokenPair requestTokenPair = new RequestTokenPair(accessToken.key, accessToken.secret);
				uid = session.retrieveWebAccessToken(requestTokenPair);				
			}
			
			if(uid.equals(params.get("uid"))){
				if(session.isLinked()){
					
					//get and print the account info
					Account account = mDBApi.accountInfo();
					System.out.println(account.displayName);
										
					//list files in /Public/database/
					Entry existingEntry = mDBApi.metadata("/Public/database/", 100, null, true, null);
					List<Entry> entries = existingEntry.contents;
					for (Entry entry : entries) {
						if(JS_MIMETYPE.equals(entry.mimeType)){		
							String fileName = entry.fileName();
							if(fileName.startsWith("functor-")){
								functorsCache.put(fileName, entry);
								String publicAccessUrl = "http://dl.dropbox.com/u/" + uid + "/database/" + entry.fileName();
								System.out.println("functors publicAccessUrl: " + publicAccessUrl);
							}
						}
					}					
				} else {
					System.out.println("Dropbox is not linked!");
				}	
			} else {
				throw new IllegalArgumentException("uid = " + uid + " is not equal to " + params.get("uid"));
			}
			
		} catch (Exception e) {
			if( e instanceof DropboxUnlinkedException){
				System.err.println(e.getMessage());
			} else {
				e.printStackTrace();
			}
		}
	}

	@RestHandler
	public boolean beginCrawling(Map params) throws Exception {
		String href = URLDecoder.decode(params.get("href") + "", "utf-8");
		System.out.println("href: " + href);
		System.out.println("getBaseUrl: "
				+ ServletUtils.getBaseUrl(httpServletRequest));
		// System.out.println("req.getParameter(\"href\"): " +
		// req.getParameter("href") );
		// res.sendRedirect(req.getParameter("href"));
		return true;
	}
}
