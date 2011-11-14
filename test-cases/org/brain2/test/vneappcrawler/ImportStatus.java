package org.brain2.test.vneappcrawler;

public class ImportStatus {
	public static final int FETCHED = 0;
	public static final int PARSED_OK = 200;
	
	public static final int SAVED_OK = 1;
	public static final int UPDATE_OK = 2;	
	public static final int UNCOMPLETE_PARSED = 3;
	public static final int SAVE_FAIL = 4;
	
	public static final int DEAD_LINK = 404;
	public static final int SERVER_ERROR = 500;
}
