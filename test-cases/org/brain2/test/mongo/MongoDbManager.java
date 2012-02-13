package org.brain2.test.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public interface MongoDbManager {

	public abstract DBCollection getDBCollection(String name);

	public abstract boolean insert(BasicDBObject dbObject, String collectionName);

}