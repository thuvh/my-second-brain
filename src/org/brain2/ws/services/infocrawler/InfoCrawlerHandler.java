package org.brain2.ws.services.infocrawler;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;
import org.brain2.ws.core.utils.ServletUtils;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

public class InfoCrawlerHandler extends ServiceHandler {

	// In the class declaration section:
	private DropboxAPI<WebAuthSession> mDBApi;
	final static private String APP_KEY = "8d2kln7x9xpchnj";
	final static private String APP_SECRET = "g3afhlbffx395ep";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	AccessTokenPair accessToken;
	String uid = null;
	
	public InfoCrawlerHandler(){
		System.out.println("init InfoCrawlerHandler");
	}
	

	@RestHandler
	public String getServiceName(Map params) {
		return this.getClass().getName();
	}

	public void requestDropboxSession(Map params) {

		try {
			// And later in some initialization function:
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
			mDBApi = new DropboxAPI<WebAuthSession>(session);

			WebAuthInfo authInfo = mDBApi.getSession().getAuthInfo();
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

	public void postAuthorizeDropbox(Map params) throws DropboxException {
		try {			
			if(uid == null){
				WebAuthSession session = mDBApi.getSession();
				RequestTokenPair requestTokenPair = new RequestTokenPair(accessToken.key, accessToken.secret);
				uid = session.retrieveWebAccessToken(requestTokenPair);
			}
			System.out.println(uid);
			
			if(mDBApi.getSession().isLinked()){
				Account account = mDBApi.accountInfo();
				System.out.println(account.displayName);
			} else {
				System.out.println("Dropbox is not linked!");
			}			
			
			Entry existingEntry = mDBApi.metadata("/Public", 100, null, true, null);
			List<Entry> entries = existingEntry.contents;
			for (Entry entry : entries) {
				System.out.println("entry: " + entry.fileName());	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
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
