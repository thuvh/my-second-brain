package org.brain2.ws.core.utils;

public class Log {
	public static final int NO_LOG = 0;
	public static final int PRINT_CONSOLE = 1;
	
	public static volatile int MODE = PRINT_CONSOLE;
	
	public static void println(String s) {
		if(MODE == PRINT_CONSOLE){
			System.out.println(s);
		}
	}
	
	public static void println(Object s) {
		if(MODE == PRINT_CONSOLE){
			System.out.println(s);
		}
	}
}
