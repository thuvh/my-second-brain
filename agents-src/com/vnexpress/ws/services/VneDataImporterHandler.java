package com.vnexpress.ws.services;

import java.io.IOException;
import java.util.Map;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.brain2.ws.core.ServiceHandler;
import org.brain2.ws.core.annotations.RestHandler;

import com.vnexpress.dao.VneSQLserverDao;

public class VneDataImporterHandler extends ServiceHandler {
	private static RecordManager linksDBManager;
	private static PrimaryTreeMap<String,String> linksDB;
	
	public VneDataImporterHandler() {
		if(linksDB == null || linksDBManager == null){
			try {
				/** create (or open existing) database */
				String fileName = "cache/vne_importer";
				linksDBManager = RecordManagerFactory.createRecordManager(fileName);
				
				/** Creates TreeMap which stores data in database.  
				 *  Constructor method takes recordName (something like SQL table name)*/
				String recordName = "vne_importer_cache";
				linksDB = linksDBManager.treeMap(recordName);
			} catch (IOException e) {			
				e.printStackTrace();
			}	
		}
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
	
	public String parseArticle(Map params){
		if( ! params.containsKey("path") ){
			return "";
		}
		final String path = params.get("path")+"";
		
		if(linksDB.containsKey(path)){
			System.out.println("get from cache: "+path );
			return linksDB.get(path);
		}
		
		long artilceId = 0;
		if(params.containsKey("id") ){
			artilceId = Long.parseLong(params.get("id")+"");		
			System.out.println("artilceId: "+artilceId);
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
			linksDBManager.commit();			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		return content;
	}

	@RestHandler
	public String getServiceName(Map params) {
		// TODO Auto-generated method stub
		return this.getClass().getName();
	}
	
	@Override
	protected void finalize() throws Throwable {		
		super.finalize();
		try {			
			linksDBManager.close();			
		} catch (Throwable e) {			
			e.printStackTrace();
		}
	}

}
