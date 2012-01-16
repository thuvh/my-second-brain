package com.vnexpress.ws.services;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;
import org.brain2.ws.core.utils.Log;

import com.vnexpress.dao.VneSQLserverDao;

public class VneDataImporterHandler extends ServiceHandler {
	private static RecordManager recordManager;
	private static PrimaryTreeMap<String,String> linksDB;
	private static PrimaryTreeMap<String,String> notifyDB;
	
	static final String recordManagerName = "cache/vne_importer";
	static final String linksDBName = "vne_importer_cache";
	static final String notifyDBName = "notify_log";
	
	static {
		clearLogDB();
		if(linksDB == null || recordManager == null){
			try {
				/** create (or open existing) database */				
				recordManager = RecordManagerFactory.createRecordManager(recordManagerName);
				
				/** Creates TreeMap which stores data in database.  
				 *  Constructor method takes recordName (something like SQL table name)*/
				
				linksDB = recordManager.treeMap(linksDBName);				
			} catch (IOException e) {			
				e.printStackTrace();
			}	
		}
	}
	
	protected static PrimaryTreeMap<String,String> getNotifyDB() {		
		try {
			/** create (or open existing) database */
			if(recordManager == null){
				recordManager = RecordManagerFactory.createRecordManager(recordManagerName);	
			}
	
			if(notifyDB == null){
				notifyDB = recordManager.treeMap(notifyDBName);
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}	
		
		return notifyDB;
	}
	
	protected static void clearLogDB() {
		File directory = new File("cache");		
		File[] files = directory.listFiles();
		for (File file : files)
		{
		   if (!file.delete())
		   {
			   // Failed to delete file
		       Log.println("Failed to delete "+file);
		   }
		}
	}
	
	public VneDataImporterHandler() {
		
	}

	@RestHandler
	public void editor(Map params ) throws Exception {
		Object action = params.get("action");
		//int limit = Integer.parseInt(params.get("limit")+"") ;
		if("parseOldArticle".equals(action)){
			int limit = 10;
			try {
				limit = Integer.parseInt(params.get("limit")+"");
			} catch (NumberFormatException e) {		
				System.err.println("running with default 10 limit of VneSQLserverDao.parseOldArticle");
			}
			VneSQLserverDao.parseOldArticle(limit, Boolean.parseBoolean(params.get("forceupdate")+""));
		} else if("resumeImportErrorLinks".equals(action)){
			System.out.println("TODO here");
		} else if("importHotArticles".equals(action)){
			System.out.println("TODO here");
		} else if("parse".equals(action)){
			System.out.println("TODO here");
		}
	}
	
	
	public String parseAllsArticle(Map params){		
		int maxId = 1000510000;
		if(params.get("maxId") != null){
			try {
				maxId = Integer.parseInt(params.get("maxId")+"");
			} catch (NumberFormatException e) {}
		}	
		//org.apache.james.mime4j.message.Body
		
		try {
			org.brain2.test.dao.VneSQLserverDao.main(new String[]{maxId+""});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return "";
	}
	
	
	public String parseArticle(Map params){
		if( ! params.containsKey("path") ||  ! params.containsKey("id") ){
			return "need params[path,id] in Request";
		}
		final String path = params.get("path")+"";
		final long artilceId = Long.parseLong(params.get("id")+"");		
		
		if(linksDB.containsKey(path)){
			System.out.println("get from cache: "+path );
			System.out.println("### fetchAllCommentsArticle ...");
			//VneSQLserverDao.fetchAllCommentsArticle(artilceId);
			return linksDB.get(path);
		}		
		
		final String content = VneSQLserverDao.parseArticle(path, artilceId , Boolean.parseBoolean(params.get("forceupdate")+""));
		if( ! content.isEmpty() ){			
			new Thread(new Runnable() {								
				@Override
				public void run() {						
					System.out.println("add cache: "+path + " length " + content.length());
					linksDB.put(path, content);																
				}
			}).start();	
		}
		try {
			recordManager.commit();			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		return content;
	}
	
	public String parseCommentArticle(Map params){
		if( ! params.containsKey("path") ){
			return "";
		}
		final String path = params.get("path")+"";
		
		if(linksDB.containsKey(path)){
			System.out.println("get from cache: "+path );
			return linksDB.get(path);
		}
		
		long commentId = 0;
		if(params.containsKey("id") ){
			commentId = Long.parseLong(params.get("id")+"");		
			System.out.println("artilceId: "+commentId);
		}
		final String content = VneSQLserverDao.parseCommentArticle(path, commentId );
		if( ! content.isEmpty() ){
			new Thread(new Runnable() {								
				@Override
				public void run() {						
					System.out.println("add cache: "+path + " length " + content.length());
					linksDB.put(path, content);	
					try {
						recordManager.commit();			
					} catch (IOException e) {			
						e.printStackTrace();
					}
				}
			}).start();	
		}
		return content;
	}
	
	@RestHandler
	public String processNotification(final Map params ) throws Exception {
		final String object = URLDecoder.decode(params.get("object")+"","utf-8").toLowerCase();
		final long id =  Long.parseLong(""+params.get("id"));
		final String verb = URLDecoder.decode(params.get("verb")+"","utf-8").toLowerCase();
		final String path = URLDecoder.decode(params.get("path")+"","utf-8").toLowerCase();
		final String k = object +"-"+ verb +"-"+ id;
		
		if(! "".equals(path) ){
			System.out.println("processNotification parseArticle");
			new Thread(new Runnable() {								
				@Override
				public void run() {						
					boolean processOk = ! VneSQLserverDao.parseArticle(path, id , Boolean.parseBoolean(params.get("forceupdate")+"")).isEmpty();		
					try {
						getNotifyDB().put(k, ""+processOk);
						recordManager.commit();			
					} catch (Exception e) {			
						e.printStackTrace();
					}
				}
			}).start();	
			
			return "true";
		}		
		boolean logNotify = "true".equals(params.get("log"));
		
		if(logNotify){
			System.out.println("processNotification logNotify " + k);
			
			boolean processOk = true;//TODO
			
			if("article".equals(object)&& ("insert".equals(verb)||"update".equals(verb)) && id > 0 ){				
				System.out.println("processNotification fetchArticle");
				new Thread(new Runnable() {
					@Override
					public void run() {						
						try {
							VneSQLserverDao.fetchArticle(id, 1);
						} catch (Exception e) {							
							e.printStackTrace();
						}															
					}
				}).start();	
			}else if("comment".equals(object)&& ("insert".equals(verb)) && id > 0 ){				
				System.out.println("processNotification parseCommentById");
				new Thread(new Runnable() {								
					@Override
					public void run() {						
						try {
							VneSQLserverDao.parseCommentById(id);
						} catch (Exception e) {							
							e.printStackTrace();
						}
					}
				}).start();	
			}			
			
			try {
				getNotifyDB().put(k, ""+processOk);
				recordManager.commit();			
			} catch (Exception e) {			
				e.printStackTrace();
			}
		}		
		return "true";
	}
	
	@RestHandler
	public String getNotificationLog(Map params ) throws Exception {		
		final String pass = URLDecoder.decode(params.get("pass")+"","utf-8");
		if(pass.equals("fosp@123")){
			//FIXME
			StringBuilder log = new StringBuilder();			
			Set<String> keys = getNotifyDB().keySet();
			for (String k : keys) {
				log.append(k).append(" : ").append(getNotifyDB().get(k)).append("___");
			}		
			String s = log.toString();
			//System.out.println(s);
			return s;
		}
		return "";
	}
	
	@RestHandler
	public String makeIReportThumbnail(Map params ) throws Exception {
		Runtime.getRuntime().exec( new String[] {
	      "java",
	      "-jar",
	      "-Xms128m",
	      "-Xmx1024m",
	      "make-thumbnail-image.jar",
	      params.get("src")+"",
	      params.get("w")+"",
	      params.get("h")+""
	    });	
		return "";
	}
	

	@RestHandler
	public String getServiceName(Map params) {		
		return this.getClass().getName();
	}
	
	@Override
	protected void finalize() throws Throwable {		
		super.finalize();
		try {			
			recordManager.close();			
		} catch (Throwable e) {			
			e.printStackTrace();
		}
	}

}
