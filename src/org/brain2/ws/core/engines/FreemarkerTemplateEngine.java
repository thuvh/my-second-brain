package org.brain2.ws.core.engines;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.brain2.ws.core.utils.FileUtils;
import org.brain2.ws.core.utils.StringPool;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreemarkerTemplateEngine {
		
	static Logger logger = Logger.getLogger(FreemarkerTemplateEngine.class);
	
	private static final String TEMPLATE_CACHE_NAME = FreemarkerTemplateEngine.class.getName();
	private static Map<String, Template> cachePools = new HashMap<String, Template>();
		
	public static final String DEFAULT_ROOT_PATH = FileUtils.getRuntimeFolderPath() + "/resources/templates/";
	
	
	
	private static String _encodeCacheKey(String templatePath){
		StringBuilder sb = new StringBuilder();
		sb.append(TEMPLATE_CACHE_NAME);
		sb.append(StringPool.POUND);
		sb.append(templatePath);		
		return sb.toString();
	}
	
	public static void clearCaches() {		
		cachePools.clear();
	}
	
	public static void clearTemplateCache(String templatePath) {		
		cachePools.remove(_encodeCacheKey(templatePath));		
	}
	
	
	public static Template getTemplateFromCaches(String templatePath){
		String key = _encodeCacheKey(templatePath);
		Template template = cachePools.get(key);
		if(template != null){
			logger.info(" cache FOUND, templatePath =  " + templatePath);	
		}
		else {			
			logger.info(" cache NOT FOUND, templatePath =  " + templatePath);	
		}		
		return template;
	}
	
	public static boolean setTemplateToCaches(Template template, String templatePath){		
		if (template != null) {
			String key = _encodeCacheKey(templatePath);	
			cachePools.put(key, template);
			return true;
		}
		return false;
	}
	
	public static Template getTemplate(String relativePath, String templateName) throws IOException, IllegalArgumentException {		
		Configuration config = new Configuration();	
		config.setObjectWrapper(new DefaultObjectWrapper());
		String dirPath = DEFAULT_ROOT_PATH + relativePath;
		config.setDirectoryForTemplateLoading(new File(dirPath));		
						
		Template template = null;
		if(isDebugMode()){
			//In debug mode, we DO NOT get from caches
			template = config.getTemplate(templateName);
		}
		else {			
			String fullPath = dirPath + templateName;
			template = getTemplateFromCaches(fullPath);
			if(template == null){
				template = config.getTemplate(templateName);				
			}
			setTemplateToCaches(template, fullPath);//update cache to live longer
		}
		if(template == null){
			throw new IllegalArgumentException(" In " + relativePath + " NOT FOUND template " + templateName);
		}
		return template;			
	}

	private static boolean isDebugMode() {		
		//TODO
		return true;
	}


}
