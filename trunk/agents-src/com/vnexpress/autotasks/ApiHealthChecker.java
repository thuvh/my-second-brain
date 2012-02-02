package com.vnexpress.autotasks;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.brain2.ws.core.autotasks.AutoTasks;

public class ApiHealthChecker extends AutoTasks{

	@Override
	public void run() {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
		System.out.println("#ApiHealthChecker up! " +  "It's " + dateFormat.format(new Date()));
		
	}

}
