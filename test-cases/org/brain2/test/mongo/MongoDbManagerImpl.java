package org.brain2.test.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

public class MongoDbManagerImpl implements MongoDbManager {
	private DB dbInstance;
	private static MongoDbManager instance;
	
	
	public static MongoDbManager startInstance(String mongodbBinDir, String dbPathDir) throws Exception {
		if(instance == null){
			
		}
		return instance;
	}

	public static MongoDbManager getInstance(String host, String dbName) throws Exception {
		if(instance == null){
			instance = new MongoDbManagerImpl(host, dbName);
		}
		return instance;
	}
	
	protected MongoDbManagerImpl() {}
	
	public MongoDbManagerImpl(String host, String dbName) throws Exception{
		this.dbInstance = Mongo.connect(new DBAddress(host, dbName));		
	}
	
	/* (non-Javadoc)
	 * @see org.brain2.test.mongo.MongoDbManager#getDBCollection(java.lang.String)
	 */
	@Override
	public DBCollection getDBCollection(String name){		
		return this.dbInstance.getCollection(name);		
	}
	
	/* (non-Javadoc)
	 * @see org.brain2.test.mongo.MongoDbManager#insert(com.mongodb.BasicDBObject, java.lang.String)
	 */
	@Override
	public boolean insert(BasicDBObject dbObject, String collectionName){		
		WriteResult result =  getDBCollection(collectionName).insert(dbObject);
		return result.getN() > 0;
	}

}
