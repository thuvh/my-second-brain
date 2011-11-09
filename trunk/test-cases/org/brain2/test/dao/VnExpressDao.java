package org.brain2.test.dao;

import java.io.FileOutputStream;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.protocol.HTTP;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VnExpressDao {
	final PrintStream print; // declare a print stream object
	final PrintStream errorPrint; // declare a print stream object

	protected Connection conn = null;
	private static VnExpressDao _theInstance = null;
	private volatile static int workFinished = 0;
	private volatile static int jobCount = 0;
	private volatile static int totalJobCount = 0;
	private volatile List<String> errorLinks = Collections.synchronizedList(new ArrayList<String>());
	
	public synchronized List<String> getErrorLinks() {
		return errorLinks;
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
		// Connect print stream to the output stream
		print = new PrintStream(new FileOutputStream("import-data-log.txt"));
		errorPrint = new PrintStream(new FileOutputStream("import-error-log.txt"));
		initConnection();
	}

	public void closeConnection() {
		try {
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
				print.println("\n ==> theLink: " + theLink);				

				try {
					final String html = HttpClientUtil.executeGet(theLink);
					final Document doc = Jsoup.parse(html, HTTP.UTF_8);
					final String mainContentNodeId = "#content";
					final String baseURL = "http://vnexpress.net";

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
							System.out.println("descriptionTxt: " + descriptionTxt);
						} else if (metaName.equals("keywords")) {
							keywordsTxt = metaContent;
							// System.out.println("descriptionTxt: " + keywordsTxt);
						} else if (metaName.equals("robots")) {
							robotsTxt = metaContent;
							// System.out.println("robotsTxt: " + robotsTxt);
						}
					}

					Elements title = doc.select("title");
					String titleTxt = "";
					if (title.size() > 0) {
						titleTxt = title.get(0).text();
						print.println("titleTxt: " + titleTxt);
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

					VnExpressDao instance = VnExpressDao.getInstance();
					print.println(" END ### workcount = "	+ instance.workFinished());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();					
					errorPrint.println(theLink + " @@@ " + e.getMessage());
					errorLinks.add(theLink);
				}
			}
		};
		return thread;
	}

	private static final int NTHREDS = 10;

	public void masterWorker() throws Exception {
		final ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		int total = _theInstance.getTotalCount();
		print.println("total article:" + total);

		int mod = total % NTHREDS;
		print.println(" mod:" + mod);

		total = 1000;
		int startIndex = 0;

		totalJobCount = total;
		while (startIndex < total) {
			allowWorkersToPool(startIndex, NTHREDS, _theInstance, executor);
			startIndex += NTHREDS;
			System.out.println("sleeping zzz ...");
			Thread.sleep(500);			
		}

		// This will make the executor accept no new threads
		// and finish all existing threads in the queue
		executor.shutdown();

		// Wait until all threads are finish
		while (!executor.isTerminated()) {
		}
		System.out.println("Finished all threads");

		_theInstance.closeConnection();		
	}
	
	public PrintStream getPrintStream(){
		return this.print;
	}
		

	public void allowWorkersToPool(final int start, final int limit,
			final VnExpressDao vnExpressDao, final ExecutorService executor) {
		try {
			List<String> paths = vnExpressDao.getSubjectPath(start, limit);
			for (String path : paths) {
				System.out.println(path);
				Runnable worker = vnExpressDao.httpGetArticle(path);
				executor.execute(worker);
				jobCount++;
				print.println(" #executor count: " + jobCount);
			}
			print.println(" #Finished childWorker start: " + start);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private static boolean isWorking = false;
	
	public static void main(String[] args) {
		if( isWorking ){
			return;
		}
		long start = System.nanoTime();		

		try {
			PrintStream statisticsPrint = new PrintStream(new FileOutputStream("statistics-data-log.txt"));			
			statisticsPrint.println("#start-time: "+ (new SimpleDateFormat()).format(new Date()) );
			
			final VnExpressDao vnExpressDao = VnExpressDao.getInstance();
			isWorking = true;
			vnExpressDao.masterWorker();
			
			List<String> links = vnExpressDao.getErrorLinks();
			for (String link : links) {
				System.err.println(link);
			}
			
			long end = System.nanoTime();
			long elapsedTime = end - start;
						
			//convert to seconds			
			statisticsPrint.println(" \n === Test done === \n === in seconds: "	+ TimeUnit.NANOSECONDS.toSeconds(elapsedTime));
			statisticsPrint.println(" === in Minutes: "	+ TimeUnit.NANOSECONDS.toMinutes(elapsedTime));		
			statisticsPrint.println("#end-time: "+ (new SimpleDateFormat()).format(new Date()) );
			vnExpressDao.closeConnection();
		} catch (Exception e) {
			System.err.println("Error in writing to file");
		}	

	}
}
