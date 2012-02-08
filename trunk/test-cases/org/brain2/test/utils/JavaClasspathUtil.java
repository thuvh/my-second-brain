package org.brain2.test.utils;

import java.io.File;
import java.util.ArrayList;

public class JavaClasspathUtil {
	
	private ArrayList<String> jarPaths = null;
	
	public static interface FileNameFilter {
		public boolean check(String name);
	}
	
	private void listFiles(FileNameFilter filter, String basePath, String folderPath, boolean recursive){
		File folder = new File(basePath + "/" + folderPath);
		File[] files = folder.listFiles();
		for (File file : files) {			
			String path = folderPath+"/"+file.getName();
			if(file.isDirectory() && recursive){				
				listFiles(filter, basePath, path, recursive);
			}else if( filter.check(file.getName())){
				if(folderPath.isEmpty()){
					jarPaths.add(file.getName());
				} else {
					jarPaths.add(path);	
				}				
			}
		}
	}
	
	public ArrayList<String> lookupFiles(FileNameFilter filter, String basePath, String folderPath, boolean recursive){
		jarPaths = null;
		jarPaths = new ArrayList<String>();
		listFiles(filter,basePath, folderPath, recursive);
		return jarPaths;
	}
	
	
	public static void main(String[] args) {
		File currentFolder = new File("");
		
		JavaClasspathUtil util = new JavaClasspathUtil();
		
		ArrayList<String> jarPaths2 = util.lookupFiles(new JavaClasspathUtil.FileNameFilter() {			
			@Override
			public boolean check(String name) {				
				return name.endsWith(".jar");
			}
		},currentFolder.getAbsolutePath(), "lib", true);			
		for (String jarPath : jarPaths2) {
			System.out.println("<zipfileset excludes=\"META-INF/*.SF\" src=\""+jarPath+"\" />");
		}
		
		ArrayList<String> jarPaths3 = util.lookupFiles(new JavaClasspathUtil.FileNameFilter() {			
			@Override
			public boolean check(String name) {			
				//System.out.println(name);
				return name.startsWith("agent-slave-") && name.endsWith(".jar");
			}
		},currentFolder.getAbsolutePath(), "", false);			
		for (String jarPath : jarPaths3) {
			System.out.println(jarPath);
		}
	}
}
