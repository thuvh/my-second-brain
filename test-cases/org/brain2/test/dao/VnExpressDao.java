package org.brain2.test.dao;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import org.apache.http.protocol.HTTP;
import org.brain2.test.vneappcrawler.ImportStatus;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VnExpressDao {
		
	public static final int TIME_TO_SLEEP = 600;
	private static final int NTHREDS = 8;
	public static int SAMPLE_TEST_NUM = 3000;
	
	//dblog
	private final RecordManager linksDBManager;
	private final PrimaryTreeMap<String,Integer> linksDB;
	
	final PrintStream print; // declare a print stream object
	final PrintStream errorPrint; // declare a print stream object

	protected Connection conn = null;
	private static VnExpressDao _theInstance = null;
	private volatile static int workFinished = 0;
	private volatile static int jobCount = 0;
	private volatile static int totalJobCount = 0;
	private volatile static int totalJobFailed = 0;
	private volatile static int totalDieLinks = 0;
	private volatile List<String> errorLinks = Collections.synchronizedList(new ArrayList<String>(1000));

	public synchronized List<String> getErrorLinks() {
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

	public synchronized static VnExpressDao getInstance() throws Exception {
		if (_theInstance == null) {
			_theInstance = new VnExpressDao();
		}
		return _theInstance;
	}

	protected VnExpressDao() throws Exception {
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
		initConnection();
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

	protected Connection initConnection() throws Exception {
		String userName = "vnemobile";
		String password = "vnemobile@123";
		String url = "jdbc:mysql://10.254.53.216/vnemobile";
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		this.conn = DriverManager.getConnection(url, userName, password);
		System.out.println("Database connection established");
		return this.conn;
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		if (conn != null) {
			try {
				conn.close();
				System.out.println("Database connection terminated");
				conn = null;
			} catch (Exception e) { /* ignore close errors */
			}
		}
	}

	/**
	 * total records of vnexpress subject
	 * 
	 * @return int
	 * @throws Exception
	 */
	public int getTotalCount() throws Exception {
		String sql = "SELECT count(`ID`) as total FROM `vnemobile`.`subject0`";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		int total = 0;
		while (rs.next()) {
			total = rs.getInt("total");
		}
		rs.close();
		ps.close();
		return total;
	}

	public List<String> getSubjectPath(int begin, int total) throws Exception {
		String sql = "SELECT `ID`,`Path`  FROM `vnemobile`.`subject0` LIMIT ?,? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, begin);
		ps.setInt(2, total);

		System.out.println("begin: " + begin);
		System.out.println("total: " + total);

		ResultSet rs = ps.executeQuery();
		List<String> list = new ArrayList<String>(1000);
		while (rs.next()) {
			list.add("http://vnexpress.net" + rs.getString("Path"));
		}
		rs.close();
		ps.close();
		return list;
	}

	public Runnable httpGetArticle(final String theLink) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {				

				try {
					String html = HttpClientUtil.executeGet(theLink);
					if (html.isEmpty()||html.equals("500")) {
						totalJobFailed++;
						print.println(" 500 ### FAIL LINK, => theLink: " + theLink);
						print.flush();
						saveLogDB(theLink, ImportStatus.SERVER_ERROR);
						throw new IllegalArgumentException("http get fail, 500 server error");
					} else if(html.equals("404")){
						totalDieLinks++;
						print.println(" 404 ### skip, workcount = " + VnExpressDao.workFinished()+ " => theLink: " + theLink);
						print.flush();
						saveLogDB(theLink, ImportStatus.DEAD_LINK);
						return;
					}
					
					Document doc = Jsoup.parse(html, HTTP.UTF_8);
					String mainContentNodeId = "#content";
					String baseURL = "http://vnexpress.net";

					Elements metas = doc.select("meta");
					String descriptionTxt = "";
					String keywordsTxt = "";
					String robotsTxt = "";

					for (Element meta : metas) {
						String metaName = meta.attr("name");
						String metaContent = meta.attr("content");
						// System.out.println("meta name: " + metaName);
						if (metaName.equals("description")) {
							descriptionTxt = metaContent;
							System.out.println("descriptionTxt: "
									+ descriptionTxt);
						} else if (metaName.equals("keywords")) {
							keywordsTxt = metaContent;
							// System.out.println("descriptionTxt: " +
							// keywordsTxt);
						} else if (metaName.equals("robots")) {
							robotsTxt = metaContent;
							// System.out.println("robotsTxt: " + robotsTxt);
						}
					}

					Elements title = doc.select("title");
					String titleTxt = "";
					if (title.size() > 0) {
						titleTxt = title.get(0).text();
						//print.println("titleTxt: " + titleTxt);
					}

					System.out.println(" BEGIN #####################");
					Elements contentNode;
					if (mainContentNodeId.isEmpty()) {
						contentNode = doc.select("body");
					} else {
						contentNode = doc.select(mainContentNodeId);
					}

					// get images in content
					Elements imgs = contentNode.select("img[src]");
					for (Element img : imgs) {
						String src = img.attr("src");
						// FIXME
						if (src.startsWith("/Files/Subject/")) {
							System.out.println(" #img[src] = " + baseURL + src);
						}
					}

					Elements comments = contentNode.select("div.comment_ct");
					for (Element comment : comments) {
						String commentText = comment.html();
						// FIXME
						System.out.println(commentText + "\n");
					}

					final Elements linkNodes = contentNode.select("a[href]");
					for (Element linkNode : linkNodes) {
						String href = linkNode.attr("href");
						if (href.endsWith("#aComment")) {
							System.out.println(" #a[href] = " + href);
						}
					}

					final Elements cpms_content = contentNode
							.select("div[cpms_content]");
					System.out.println(" #cpms_content = "
							+ cpms_content.size());
					for (Element node : cpms_content) {
						String text = node.text();
						System.out.println(" #cpms_content = " + text);
					}
					
					//TODO save DB here
					
					
					saveLogDB(theLink, ImportStatus.PARSED_OK);
					print.println(" 200 ### workcount = " + VnExpressDao.workFinished()+" => theLink: " + theLink);					
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
		int total = _theInstance.getTotalCount();
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

	public void allowWorkersToPool(final int start, final int limit,
			final VnExpressDao vnExpressDao, final ExecutorService executor) {
		try {
			List<String> paths = vnExpressDao.getSubjectPath(start, limit);
			for (String theLink : paths) {
				System.out.println("#Fetching: "+theLink);
				Runnable worker = vnExpressDao.httpGetArticle(theLink);
				executor.execute(worker);
				jobCount++;
				saveLogDB(theLink, ImportStatus.FETCHED);
				//print.println(" #executor count: " + jobCount);
			}
			
			if(jobCount%100 == 0){
				linksDBManager.commit();
			}
			
			
			//try to resume error links
			List<String> errorLinks = vnExpressDao.getErrorLinks();
			if(errorLinks.size()>0){
				for (String link : errorLinks) {
					totalJobFailed--;
					Runnable worker = vnExpressDao.httpGetArticle(link);
					executor.execute(worker);
					errorLinks.remove(link);
				}
				Thread.sleep(TIME_TO_SLEEP);
			}
			//print.println(" #Finished childWorker start: " + start);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean isWorking = false;

	public static void importVnExpressArticles() {
		if (isWorking) {
			return;
		}
		long start = System.nanoTime();
		try {
			PrintStream statisticsPrint = new PrintStream(new FileOutputStream("statistics-data-log.txt"));
			statisticsPrint.println("#start-time: " + (new SimpleDateFormat()).format(new Date()));
			isWorking = true;
			final VnExpressDao vnExpressDao = VnExpressDao.getInstance();
			vnExpressDao.masterWorker();

			List<String> links = vnExpressDao.getErrorLinks();
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
	
	public static void resumeImportErrorLinks(){
		try {
			jobCount = 0;
			workFinished = 0;

			final FileInputStream fstream = new FileInputStream("import-error-log.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			List<String> paths = new ArrayList<String>();
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				String path  = strLine.split("@@@")[0].trim();				
				System.out.println(path);
				System.out.println("@@@ rusume link: " + path);
				paths.add(path);
			}
			totalJobCount = paths.size();
			// Close the input stream
			in.close();
			Thread.sleep(1000);			
									
			File yourFile = new File("import-error-log.txt");
			yourFile.delete();
			File yourNewFile = new File("import-error-log.txt");
			yourNewFile.createNewFile();
			
			Thread.sleep(1000);
			
			System.out.println(" @@@ starting rusume error link: ");
			final VnExpressDao vnExpressDao = VnExpressDao.getInstance();
			final ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
			int c = 0;
			for (String path : paths) {
				Runnable worker = vnExpressDao.httpGetArticle(path);
				executor.execute(worker);
				if(c % NTHREDS == 0){
					Thread.sleep(TIME_TO_SLEEP);
				}
			}
			executor.shutdown();

			// Wait until all threads are finish
			while (!executor.isTerminated()) {}
			System.out.println("Finished all threads");			
			
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
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
	
	protected void checksumAllLinks(){
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
				executor.execute(this.httpGetArticle(link));
				c4++;
			}			
		}
		System.out.println("c1 = "+ c1);
		System.out.println("c2 = "+ c2);
		System.out.println("c3 = "+ c3);
		System.out.println("c4 = "+ c4);
		importVnExpressArticles();
		executor.shutdown();
		while(!executor.isTerminated()){}		
		int total = c2+c3+c4;
		System.out.println("total = "+ total);
	}
	

	public static void main(String[] args) throws Exception {
		
		final RecordManager linksDBManager;
		final PrimaryTreeMap<String,Integer> linksDB;
		
		/** create (or open existing) database */
		String fileName = "log/vne_importer";
		linksDBManager = RecordManagerFactory.createRecordManager(fileName);		
		
		/** Creates TreeMap which stores data in database.  
		 *  Constructor method takes recordName (something like SQL table name)*/
		String recordName = "vne_importer_log";
		linksDB = linksDBManager.treeMap(recordName); 
		
		int c1 = 0, c2 =0;
		Set<String> links = linksDB.keySet();
		for (String link : links) {
			int status = linksDB.get(link);
			if(status == 1){
				c1++;
			} else if(status == 2){
				c2++;
			}
		}
		System.out.println("c1 = "+ c1);
		System.out.println("c2 = "+ c2);
		int total = c1 + c2;
		System.out.println("total = "+ total);
	}
}
