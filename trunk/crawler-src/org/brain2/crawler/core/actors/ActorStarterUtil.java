package org.brain2.crawler.core.actors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.brain2.test.utils.JavaClasspathUtil;

public class ActorStarterUtil {
	public static void startChildActors() throws IOException{
		int min = 64, max = 1024;				
		JavaClasspathUtil util = new JavaClasspathUtil();
		File currentFolder = new File("");
		ArrayList<String> jarFileNames = util.lookupFiles(new JavaClasspathUtil.FileNameFilter() {			
			@Override
			public boolean check(String name) {			
				//System.out.println(name);
				return name.startsWith("agent-") && name.endsWith(".jar");
			}
		},currentFolder.getAbsolutePath(), "", false);			
		for (String jarFileName : jarFileNames) {			
			String cmd = "java -jar -Xms"+min+"m -Xmx"+max+"m -XX:-UseParallelGC " + jarFileName;
			System.out.println("... exec: " + cmd);
			Runtime.getRuntime().exec(cmd);
		}
	}
}
