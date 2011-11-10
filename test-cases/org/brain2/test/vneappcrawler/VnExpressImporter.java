package org.brain2.test.vneappcrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class VnExpressImporter {
	
	public static final int TIME_TO_SLEEP = 600;
	private static final int NTHREDS = 5;
	public static int SAMPLE_TEST_NUM = 1000;
	
	//dblog
	private final RecordManager linksDBManager;
	private final PrimaryTreeMap<String,Integer> linksDB;
	
	final PrintStream print; // declare a print stream object
	final PrintStream errorPrint; // declare a print stream object
	
	private static boolean isWorking = false;
	private volatile static VnExpressImporter _theInstance = null;
	private volatile static VnExpressDao _vnExpressDao = null;
	
	private volatile static int workFinished = 0;
	private volatile static int jobCount = 0;
	private volatile static int totalJobCount = 0;
	private volatile static int totalJobFailed = 0;
	private volatile static int totalDieLinks = 0;
	private volatile static Queue<String> errorLinks = new ConcurrentLinkedQueue<String>();
	private volatile static Queue<Article> articleQueue = new ConcurrentLinkedQueue<Article>();

	public synchronized Queue<String> getErrorLinks() {
		return errorLinks;
	}
	
	public synchronized static int getTotalJobFailed() {
		return totalJobFailed;
	}
	
	public synchronized static int getTotalDieLinks() {
		return totalDieLinks;
	}

	protected synchronized static int workFinished() {
		workFinished++;
		return workFinished;
	}

	public synchronized static int getJobCount() {
		return jobCount;
	}

	public synchronized static int getWorkFinished() {
		return workFinished;
	}

	public synchronized static int getTotalJobCount() {
		return totalJobCount;
	}

	public synchronized static VnExpressImporter getInstance() throws Exception {
		if (_theInstance == null) {
			_theInstance = new VnExpressImporter();
		}
		return _theInstance;
	}

	protected VnExpressImporter() throws Exception {
		if(_vnExpressDao == null){
			_vnExpressDao = VnExpressDao.getInstance();
		}
		
		/** clear all current database */
		clearLogDB();
		
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

	public void closeConnection() {
		try {
			linksDBManager.close();
			print.close();
			finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	protected void finalize() throws Throwable {		
		super.finalize();		
	}
	

	public Runnable processArticle(final String theLink, final Article oldArticle) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {				

				try {
					String html = HttpClientUtil.executeGet("http://vnexpress.net"+theLink);
					if (html.isEmpty()||html.equals("500")) {
						totalJobFailed++;
						print.println(" 500 ### FAIL LINK, => theLink: " + theLink);
						print.flush();
						saveLogDB(theLink, ImportStatus.SERVER_ERROR);
						throw new IllegalArgumentException("http get fail, 500 server error");
					} else if(html.equals("404")){
						totalDieLinks++;
						print.println(" 404 ### skip, workcount = " + VnExpressImporter.workFinished()+ " => theLink: " + theLink);
						print.flush();
						saveLogDB(theLink, ImportStatus.DEAD_LINK);
						return;
					}					
					Article newArticle = VnExpressParser.parseHtmlToArticle(theLink, html, oldArticle, _vnExpressDao);
					articleQueue.add(newArticle);
					
					//TODO save DB here		
					
					if(articleQueue.size()>=10){
						_vnExpressDao.saveArticle(articleQueue);
						articleQueue.clear();
					}
					
					saveLogDB(theLink, ImportStatus.PARSED_OK);
					print.println(" 200 ### workcount = " + VnExpressImporter.workFinished()+" => theLink: " + theLink);					
					print.flush();
				} catch (Exception e) {
					e.printStackTrace();
					errorPrint.println(theLink + " @@@ " + e.getMessage());
					errorLinks.add(theLink);
				}
			}
		};
		return thread;
	}	

	public void masterWorker() throws Exception {
		final ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		int total = _vnExpressDao.getTotalCount();
		print.println("## total article ## = " + total);

		int mod = total % NTHREDS;
		//print.println(" mod:" + mod);

		total = SAMPLE_TEST_NUM;//FIXME
		int startIndex = 0;

		totalJobCount = total;
		while (startIndex < total) {
			allowWorkersToPool(startIndex, NTHREDS, _theInstance, executor);
			startIndex += NTHREDS;
			System.out.println("sleeping zzz ...");
			Thread.sleep(TIME_TO_SLEEP);
		}

		// This will make the executor accept no new threads
		// and finish all existing threads in the queue
		executor.shutdown();

		// Wait until all threads are finish
		while (!executor.isTerminated()) {
			//wait here
		}
		System.out.println("Finished all jobs");
		linksDBManager.commit();
		totalJobFailed =  errorLinks.size();
		
		System.out.println(" === total linksDB size: " + linksDB.size());
		System.out.println(" === total errorLinks size: " + errorLinks.size());
		
		checksumAllLinks();		
		//retry 		

		_theInstance.closeConnection();
	}

	public PrintStream getPrintStream() {
		return this.print;
	}

	public synchronized void allowWorkersToPool(final int start, final int limit, final VnExpressImporter vnExpressDao, final ExecutorService executor) {
		try {
			ResultSet resultSet = _vnExpressDao.getSubjectPath(start, limit);
			while (resultSet.next()) {
				String theLink = resultSet.getString("Path");
				System.out.println("#Fetching: "+theLink );				
				Article article =new Article(resultSet.getString("ID"), resultSet.getString("Title"), resultSet.getString("Lead"),resultSet.getString("Path"),0,resultSet.getDate("Date"),resultSet.getDate("Modified"));
				if(article != null){
					Runnable worker = this.processArticle(theLink, article);
					executor.execute(worker);
					jobCount++;
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
					totalJobFailed--;
					Article article = _vnExpressDao.getArticleByPath(theLink);
					Runnable worker = this.processArticle(theLink,article);
					executor.execute(worker);
					errorLinks.remove(theLink);
				}
				Thread.sleep(TIME_TO_SLEEP);
			}
			//print.println(" #Finished childWorker start: " + start);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}	

	public static void importVnExpressArticles() {
		if (isWorking) {
			return;
		}
		long start = System.nanoTime();
		try {
			PrintStream statisticsPrint = new PrintStream(new FileOutputStream("statistics-data-log.txt"));
			statisticsPrint.println("#start-time: " + (new SimpleDateFormat()).format(new Date()));
			isWorking = true;
			final VnExpressImporter vnExpressDao = VnExpressImporter.getInstance();
			vnExpressDao.masterWorker();

			Queue<String> links = vnExpressDao.getErrorLinks();
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
		} catch (Exception e) {
			System.err.println("Error in writing to file");
		} finally {
			isWorking = false;
		}
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
		       System.out.println("Failed to delete "+file);
		   }
		}
	}
	
	protected void checksumAllLinks() throws Exception{
		int c1 = 0, c2 =0, c3 =0, c4 =0;
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
				Article article = _vnExpressDao.getArticleByPath(link);
				if(article != null){
					executor.execute(this.processArticle(link, article));
				}
				c4++;
			}			
		}
		System.out.println("FETCHED = "+ c1);
		System.out.println("PARSED_OK = "+ c2);
		System.out.println("DEAD_LINK = "+ c3);
		System.out.println("SERVER_ERROR = "+ c4);
		
		executor.shutdown();
		while(!executor.isTerminated()){}		
		int total = c2+c3+c4;
		System.out.println("total = "+ total);
	}
	
}
