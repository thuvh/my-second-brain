package org.brain2.test.vneappcrawler;

import java.io.IOException;

import org.brain2.ws.core.utils.FileUtils;

import com.google.gson.Gson;

public class ImporterConfigs {
	private String username;
	private String password;
	private String database;
	private String host;
	private String dbdriver;
	private String dbdriverclasspath;
	
	public static ImporterConfigs loadFromFile(String filePath){		
		try {
			String json = FileUtils.readFileAsString(filePath);			
			return new Gson().fromJson(json, ImporterConfigs.class);
		} catch (IOException e) {			
			e.printStackTrace();		
		}
		return null;
	}
	
	
	public String toConnectionUrl(){
		StringBuilder s = new StringBuilder();
		s.append("jdbc:").append(this.getDbdriver()).append("://");
		s.append(this.getHost());
		s.append("/");
		s.append(this.getDatabase());
		return s.toString();
	}
	
	public ImporterConfigs() {
		// TODO Auto-generated constructor stub
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getDbdriver() {
		return dbdriver;
	}
	public void setDbdriver(String dbdriver) {
		this.dbdriver = dbdriver;
	}
	public String getDbdriverclasspath() {
		return dbdriverclasspath;
	}
	public void setDbdriverclasspath(String dbdriverclasspath) {
		this.dbdriverclasspath = dbdriverclasspath;
	}
	
	
}
