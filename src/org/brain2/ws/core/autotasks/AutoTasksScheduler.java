package org.brain2.ws.core.autotasks;

import java.util.Collection;
import java.util.Timer;

public class AutoTasksScheduler {
	
	Collection<AutoTaskConfigs> autoTaskConfigs;
	
	public AutoTasksScheduler() {
		autoTaskConfigs = AutoTaskConfigs.loadFromFile("/auto-tasks-configs.json");		
		System.out.println("AutoTasksScheduler started with "+autoTaskConfigs.size() + " tasks in queue");
		
	}
	
	public int startingAutoTasks(){
		int c = 0;
		Timer timer = new Timer();
		for (AutoTaskConfigs autoTaskConfig : autoTaskConfigs) {
			try {
				System.out.println("#process autoTaskConfig:"+autoTaskConfig);
				Class clazz = Class.forName(autoTaskConfig.getClasspath());
				timer.scheduleAtFixedRate((AutoTasks) clazz.newInstance(),autoTaskConfig.getDelay(),autoTaskConfig.getPeriod());
				c++;				
			}  catch (Exception e) {				
				e.printStackTrace();
			}
		}
		return c;
	}
}
