package org.brain2.ws.core.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log {

	public static Logger get(Class clazz) {
		Logger logger = Logger.getLogger(clazz.getName());
		logger.setLevel(Level.INFO);

		return logger;
	}

	public static final int NO_LOG = 0;
	public static final int PRINT_CONSOLE = 1;

	public static volatile int MODE = PRINT_CONSOLE;

	public static void println(String s) {
		if (MODE == PRINT_CONSOLE) {
			System.out.println(s);
		}
	}

	public static void println(Object s) {
		if (MODE == PRINT_CONSOLE) {
			System.out.println(s);
		}
	}

	public static void setPropertyConfiguratorLog4J(String classname) {
		// Read properties file.
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("log4j.properties"));
			properties.put("log4j.appender.rollingFile.File", classname	+ ".log");
		} catch (IOException e) {
		}
		PropertyConfigurator.configure(properties);
	}
}
