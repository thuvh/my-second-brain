package org.brain2.ws.core;

import java.util.HashMap;
import java.util.Map;

import org.brain2.ws.core.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceMapperLoader {
	private static final Map<String,Class> classMapper = new HashMap<String, Class>();
	private JSONObject mapperJSON;
	
	
	public ServiceMapperLoader(String filePath) {
		try {			
			mapperJSON = new JSONObject(FileUtils.readFileAsString(filePath));			
		} catch (Exception e) {
			e.printStackTrace();		
		}
	}
	
	public Class getMapperClass(String namespace) throws ClassNotFoundException {
		try {
			if(mapperJSON != null){
				Class clazz = classMapper.get(namespace);
				if(clazz != null){
					return clazz;
				} else {				
					clazz = Class.forName(mapperJSON.getString(namespace));
					classMapper.put(namespace, clazz);
					System.out.println("Class Mapper Key: "+ namespace + " => classpath: " + clazz.getName());
				}				
				return clazz;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Wrong config mapper syntax!");
	}
}
