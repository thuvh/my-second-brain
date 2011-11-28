package org.brain2.test.vneappcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.brain2.ws.core.utils.HttpClientUtil;
import org.brain2.ws.core.utils.Log;

public class VnExpressImporter {
	
	public static final int TIME_TO_SLEEP = 680;
	private static int NTHREDS = 8;
	public static int SAMPLE_TEST_NUM = 200;
	public static boolean CLEAN_LOG_DB = true;
	
	//dblog
	private final RecordManager linksDBManager;
	private final PrimaryTreeMap<String,Integer> linksDB;
	
	final PrintStream print; // declare a print stream object
	final PrintStream errorPrint; // declare a print stream object
	
	
	private volatile static VnExpressImporter _theInstance = null;
	private volatile static VneDataManager _vneDataManager = null;
	private final ExecutorService saveDBExecutor = Executors.newSingleThreadExecutor();
	
	private volatile static boolean isWorking = false;
	private volatile static int workFinished = 0;
	private volatile static int jobCount = 0;
	private volatile static int totalJobCount = 0;	
	private volatile static int totalDieLinks = 0;
	private volatile static Queue<String> errorLinks = new ConcurrentLinkedQueue<String>();
	

	public synchronized Queue<String> getErrorLinks() {
		return errorLinks;
	}
	
	public synchronized static int getTotalJobFailed() {
		return errorLinks.size();
	}
	
	public synchronized static int getTotalDieLinks() {
		return totalDieLinks;
	}
	
	public synchronized static boolean isWorking() {
		return isWorking;
	}
	
	public synchronized static boolean masterJobStarted() {
		isWorking = true;
		return isWorking;
	}	
	public synchronized static boolean masterJobDone() {
		isWorking = false;
		return isWorking;
	}

	protected synchronized static int workFinished() {
		workFinished++;
		return workFinished;
	}

	public synchronized static int getJobCount() {
		return jobCount;
	}
	
	protected synchronized static void setJobCount(int jobCount) {
		VnExpressImporter.jobCount = jobCount;
	}

	public synchronized static int getWorkFinished() {
		return workFinished;
	}

	public synchronized static int getTotalJobCount() {
		return totalJobCount;
	}
	
	protected synchronized static void setTotalJobCount(int totalJobCount) {
		VnExpressImporter.totalJobCount = totalJobCount;
	}

	public synchronized static VnExpressImporter getInstance() throws Exception {
		if (_theInstance == null) {
			_theInstance = new VnExpressImporter();
		}
		return _theInstance;
	}

	protected VnExpressImporter() throws Exception {
		if(_vneDataManager == null){
			_vneDataManager = VnExpressDao.getInstance();
		}
		
		if(CLEAN_LOG_DB){	
			/** clear all current database */
			clearLogDB();
		}
		
		/** create (or open existing) database */
		String fileName = "log/vne_importer";
		linksDBManager = RecordManagerFactory.createRecordManager(fileName);		
		
		/** Creates TreeMap which stores data in database.  
		 *  Constructor method takes recordName (something like SQL table name)*/
		String recordName = "vne_importer_log";
		linksDB = linksDBManager.treeMap(recordName);
		
		// Connect print stream to the output stream
		print = new PrintStream(new FileOutputStream("import-data-log.txt"));
		errorPrint = new PrintStream(new FileOutputStream("import-error-log.txt"));
		
	}

	public void cleanResources() {
		try {
			_vneDataManager.closeConnection();
			linksDBManager.close();
			print.close();
			finalize();
		} catch (Throwable e) {			
			e.printStackTrace();
		}
	}
	

	@Override
	protected void finalize() throws Throwable {
		Log.println("finalizing ...");
		super.finalize();		
	}
	

	public Runnable processArticle(final String theLink, final Article oldArticle) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {				

				try {
					String fulLink = VnExpressUtils.getFullLink(theLink);
					if(!fulLink.isEmpty()){
						String html = HttpClientUtil.executeGet(fulLink);
						if (html.isEmpty()||html.equals("500")) {						
							print.println("500 ### FAIL LINK, => theLink: " + theLink);
							print.flush();
							saveLogDB(theLink, ImportStatus.SERVER_ERROR);
							throw new IllegalArgumentException("http get fail, 500 server error");
						} else if(html.equals("404")){
							totalDieLinks++;
							print.println("404 ### skip, workcount = " + VnExpressImporter.workFinished()+ " => theLink: " + theLink);
							print.flush();
							saveLogDB(theLink, ImportStatus.DEAD_LINK);
							return;
						}
						//Log.MODE = Log.NO_LOG;
						
						Parser parser = VnExpressUtils.getParser(theLink);
						if(parser!=null){
							final Article newArticle = parser.parseHtmlToArticle(fulLink, html, oldArticle, (VnExpressDao) _vneDataManager);
							
							Log.MODE = Log.PRINT_CONSOLE;
							saveLogDB(theLink, ImportStatus.PARSED_OK);
							
							saveDBExecutor.execute(new Runnable() {						
								@Override
								public void run() {
									try {
										if( ! _vneDataManager.isExistedArticle(newArticle)){
											_vneDataManager.saveArticle(newArticle);
											saveLogDB(theLink, ImportStatus.SAVED_OK);
										} else {
											_vneDataManager.updateArticle(newArticle);						
											Log.println(" => theLink: " + theLink + " isExistArticle = true");
											saveLogDB(theLink, ImportStatus.UPDATE_OK);
										}
										if(newArticle.isGeneralParsed()){
											saveLogDB(theLink, ImportStatus.UNCOMPLETE_PARSED);	
										}
										print.println("200 ### workcount = " + VnExpressImporter.workFinished()+" SAVE OK => theLink: " + theLink);
										print.flush();
									} catch (SQLException e) {
										errorPrint.println(theLink + " @@@ SQLException: " + e.getClass().getName()+ "-" + e.getMessage() );
										e.printStackTrace();
										saveLogDB(theLink, ImportStatus.SAVE_FAIL);	
										//System.exit(1);
									}							
								}
							});
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					errorPrint.println(theLink + " @@@ " + e.getClass().getName()+ "-" + e.getMessage());
					errorLinks.add(theLink);					
				}
			}
		};
		return thread;
	}	

	public void masterWorker() throws Exception {
//		final int numberOfCores = Runtime.getRuntime().availableProcessors();
//		System.out.println(numberOfCores);
//		final double blockingCoefficient = 0.9;
//		final int poolSize = (int)(numberOfCores / (1 - blockingCoefficient));
//		NTHREDS = poolSize;
		
		masterJobStarted();	
		final ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		int totalArticle = _vneDataManager.getTotalCountInVnExpress();
		print.println("## total article ## = " + totalArticle);

		//int mod = total % NTHREDS;
		//print.println(" mod:" + mod);

						
		int startIndex = 0;
		startIndex = totalArticle - SAMPLE_TEST_NUM;
//		startIndex = 422000; //TODO
		
		int totalJob = totalArticle - startIndex;
		Log.println("## total article ## = " + totalArticle);
		Log.println("jobcount/total: "+getJobCount() + " - " + totalJob);
		
		//TODO
		if(totalJob > SAMPLE_TEST_NUM){			
			totalJob = SAMPLE_TEST_NUM;
		}
		
		setJobCount(0);
		setTotalJobCount(totalJob);
		Log.println("jobcount/total: "+getJobCount() + " - " + getTotalJobCount());
		
		double poolNum = Math.floor( totalJob / NTHREDS );
		int mod = totalJob%NTHREDS;
		int poolIndex = 0, jobAllowcated = 0;
		while ( poolIndex <= poolNum ) {
			jobAllowcated = 0;
			
			jobAllowcated = allowWorkersToPool(startIndex, NTHREDS, _theInstance, executor);
			poolIndex++;
												
			if(startIndex < totalArticle ){
				startIndex += NTHREDS;	
			} 
			Thread.sleep(TIME_TO_SLEEP);
			
			if(mod > 0 && poolIndex>poolNum){
				Log.println("#mod: "+mod);
				Log.println("#mod/poolNum: "+poolIndex+"-"+poolNum);
				jobAllowcated += allowWorkersToPool(startIndex+mod, mod, _theInstance, executor);
				linksDBManager.commit();
			}
			
			Log.println("#jobcount/total: "+getJobCount() + " - " + getTotalJobCount());
			Log.println("#jobAllowcated: "+jobAllowcated);
			Log.println("#startIndex/totalArticle: " + startIndex + " - " + totalArticle);			
		}
		Log.println("### out the queue jobs");

		// This will make the executor accept no new threads
		// and finish all existing threads in the queue
		//FIXME
		executor.shutdown();		

		// Wait until all threads are finish
		while (!executor.isTerminated()) {} 		
		Log.println("Finished all jobs");
		linksDBManager.commit();		
		
		Log.println(" === total linksDB size: " + linksDB.size());
		Log.println(" === total errorLinks size: " + errorLinks.size());
		
		Thread.sleep(2500);
				
		//retry
		checksumAllLinks();
		
		//flush all		
		masterJobDone();
		
		new Thread(new Runnable() {			
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
				_theInstance.cleanResources();//FIXME
			}
		}).start();	
	}

	public PrintStream getPrintStream() {
		return this.print;
	}

	public synchronized int allowWorkersToPool(final int start, final int limit, final VnExpressImporter vnExpressDao, final ExecutorService executor) {
		int jobAllocated = 0;
		try {
			ResultSet resultSet = _vneDataManager.getSubjectPathInVnExpress(start, limit);
			while (resultSet.next()) {
				String theLink = resultSet.getString("Path");
				Log.println("#Fetching: "+theLink );				
				Article article =new Article(resultSet.getString("ID"),resultSet.getString("PostBy"), resultSet.getString("Title"), resultSet.getString("Lead"),resultSet.getString("Path"),0,resultSet.getDate("Date"),resultSet.getDate("Modified"));
				if(article != null){
					Runnable worker = this.processArticle(theLink, article);
					executor.execute(worker);
					jobCount++; jobAllocated++;
					saveLogDB(theLink, ImportStatus.FETCHED);
				}
				//print.println(" #executor count: " + jobCount);
			}
			
			if(jobCount%100 == 0){
				linksDBManager.commit();
			}
			resultSet.close();
						
			//try to resume error links
			final Queue<String> errorLinks = this.getErrorLinks();
			if(errorLinks.size()>0){
				for (String theLink : errorLinks) {					
					Article article = _vneDataManager.getOldSubjectByPath(theLink);
					Runnable worker = this.processArticle(theLink,article);
					executor.execute(worker);
					jobAllocated++;
					errorLinks.remove(theLink);
				}
				Thread.sleep(TIME_TO_SLEEP);
			}
			//print.println(" #Finished childWorker start: " + start);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return jobAllocated;
	}	

	public static void importVnExpressArticles() {
		if (isWorking()) {
			return;
		}
		Log.MODE = Log.PRINT_CONSOLE;
		long start = System.nanoTime();
		try {								
			PrintStream statisticsPrint = new PrintStream(new FileOutputStream("statistics-data-log.txt"));
			statisticsPrint.println("#start-time: " + (new SimpleDateFormat()).format(new Date()));			
			final VnExpressImporter importer = VnExpressImporter.getInstance();
			
			//trigger the master
			importer.masterWorker();

			Queue<String> links = importer.getErrorLinks();
			for (String link : links) {
				System.err.println(link);
			}

			long end = System.nanoTime();
			long elapsedTime = end - start;

			// convert to seconds
			statisticsPrint.println(" \n === Test done === \n === in seconds: "
					+ TimeUnit.NANOSECONDS.toSeconds(elapsedTime));
			statisticsPrint.println(" === in Minutes: "
					+ TimeUnit.NANOSECONDS.toMinutes(elapsedTime));
			statisticsPrint.println("#end-time: "
					+ (new SimpleDateFormat()).format(new Date()));		
			statisticsPrint.flush();			
		} catch (Exception e) {
			System.err.println("Error in writing to file");
		}		
		System.gc();
	}	
	
	public static void importHotArticles() {
		if (isWorking()) {
			return;
		}
		Log.MODE = Log.PRINT_CONSOLE;
		long start = System.nanoTime();
		try {								
			PrintStream statisticsPrint = new PrintStream(new FileOutputStream("statistics-data-log.txt"));
			statisticsPrint.println("#start-time: " + (new SimpleDateFormat()).format(new Date()));			
			final VnExpressImporter importer = VnExpressImporter.getInstance();
			final ExecutorService executor = Executors.newFixedThreadPool(1);
			//trigger the master
			//TODO
			masterJobStarted();
					
			List<Article> hotArticles = VnExpressHotNewsParser.parseHotArticle();
			
			setJobCount(0);
			setTotalJobCount(hotArticles.size());
			
			for (Article article : hotArticles) {								
				Runnable worker = importer.processArticle(article.getSharedURL(),article);
				executor.execute(worker);
				Thread.sleep(200);
			}
			executor.shutdown();	
			while (!executor.isTerminated()) {} 		
			Log.println("Finished all jobs");				

			long end = System.nanoTime();
			long elapsedTime = end - start;

			// convert to seconds
			statisticsPrint.println(" \n === Test done === \n === in seconds: "
					+ TimeUnit.NANOSECONDS.toSeconds(elapsedTime));
			statisticsPrint.println(" === in Minutes: "
					+ TimeUnit.NANOSECONDS.toMinutes(elapsedTime));
			statisticsPrint.println("#end-time: "
					+ (new SimpleDateFormat()).format(new Date()));		
			statisticsPrint.flush();	
			masterJobDone();
		} catch (Exception e) {
			System.err.println("Error in writing to file");
		}		
		System.gc();
	}	
	
	
	protected final synchronized boolean saveLogDB(final String link,final int status) {
		boolean ok = linksDB.put(link, status) != null;		
		return ok;
	}
	
	protected void clearLogDB() {
		File directory = new File("log");		
		File[] files = directory.listFiles();
		for (File file : files)
		{
		   if (!file.delete())
		   {
			   // Failed to delete file
		       Log.println("Failed to delete "+file);
		   }
		}
	}
	
	protected void checksumAllLinks() throws Exception{
		int c1 = 0, c2 =0, c3 =0, c4 =0, c5 =0, c6=0, c7=0, c8=0;
		Set<String> links = linksDB.keySet();
		final ExecutorService executor = Executors.newFixedThreadPool(1);
		for (String link : links) {
			int status = linksDB.get(link);
			if(status == ImportStatus.FETCHED){
				c1++;
			} else if(status == ImportStatus.PARSED_OK){
				c2++;
			} else if(status == ImportStatus.DEAD_LINK){
				c3++;
			} else if(status == ImportStatus.SERVER_ERROR){
				Article article = _vneDataManager.getOldSubjectByPath(link);
				if(article != null){
					executor.execute(this.processArticle(link, article));
				}
				c4++;
			} else if(status == ImportStatus.SAVED_OK){
				c5++;
			} else if(status == ImportStatus.UNCOMPLETE_PARSED){
				c6++;
			} else if(status == ImportStatus.SAVE_FAIL){
				c7++;
			} else if(status == ImportStatus.UPDATE_OK){
				c8++;
			}
		}
		Log.println("FETCHED = "+ c1);
		Log.println("PARSED_OK = "+ c2);
		Log.println("DEAD_LINK = "+ c3);
		Log.println("SERVER_ERROR = "+ c4);
		Log.println("SAVED_OK = "+ c5);
		Log.println("UNCOMPLETE_PARSED = "+ c6);
		Log.println("SAVE_FAIL = "+ c7);
		Log.println("UPDATE_OK = "+ c8);
		
		executor.shutdown();
		
		//wait checksum process
		while(!executor.isTerminated()){}
				
		int total = c2+c3+c4+c5+c6+c7+c8;
		Log.println("total = "+ total);		
	}
	
	public static void main(String[] args) throws Exception {		
		VnExpressImporter.CLEAN_LOG_DB = false;		
		VnExpressImporter.getInstance().checksumAllLinks();
		Log.println(VnExpressImporter.isWorking());
	}
	
}
