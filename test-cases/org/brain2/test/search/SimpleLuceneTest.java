package org.brain2.test.search;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SimpleLuceneTest {
	public static final Version VERSION = Version.LUCENE_33;
	private static String indexDirectoryPath = "D:/data/my-second-brain";
	private static Directory indexDirectory;

	private static int mode =1;

	public static String getIndexDirectoryPath() {
		if (indexDirectoryPath == null) {
			StringBuilder sBuilder = new StringBuilder();
			// sBuilder.append(JiveGlobals.getJiveHome());//FIXME
			sBuilder.append(File.separator);
			indexDirectoryPath = sBuilder.toString();
		}
		return indexDirectoryPath;
	}

	public static Directory getIndexDirectory() {
		if (indexDirectory == null) {
			try {
				File dir = new File(getIndexDirectoryPath());
				indexDirectory = FSDirectory.open(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return indexDirectory;
	}

	public static void main(String[] args) throws IOException, ParseException {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer(VERSION);

		// 1. create the index
		Directory index = getIndexDirectory();

		// start indexing
		if (mode == 0) {
			// the boolean arg in the IndexWriter ctor means to
			// create a new index, overwriting any existing index
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(VERSION, analyzer);
			
			IndexWriter indexWriter = new IndexWriter(index, indexWriterConfig);
			CrawlerFileTraversal crawler = new CrawlerFileTraversal(indexWriter, new File("D:/EBOOKS"));
			crawler.start();
			indexWriter.optimize();
			indexWriter.close();
		}
		// else just query

		// 2. query
		String querystr = "*lucene*";
		//Query q = new WildcardQuery(new Term("title", querystr));		
		
		querystr = "\"D:/EBOOKS/Manning.Lucene.in.Action.2nd.Edition.Jun.2010.pdf\"";
		Query q = new QueryParser(VERSION, "uri", analyzer).parse(querystr);	
		

		// 3. search
		int hitsPerPage = 10;
		IndexSearcher searcher = new IndexSearcher(index, true);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		System.out.println("Found " + hits.length + " hits.");
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println(" *docId= " + docId);
			System.out.println((i + 1) + ". " + d.get("title") + " at URI " + d.get("uri"));
			Explanation explanation  = searcher.explain(q, docId);
			System.out.println(explanation.toString());
		}

		// searcher can only be closed when there
		// is no need to access the documents any more.
		searcher.close();

		try {
			String computername = InetAddress.getLocalHost().getHostName();
			System.out.println(computername);
		} catch (Exception e) {
			System.out.println("Exception caught =" + e.getMessage());
		}
		try {
			InetAddress address = InetAddress.getLocalHost();
			//InetAddress address = InetAddress.getByName("192.168.46.53");

			/*
			 * Get NetworkInterface for the current host and then read the
			 * hardware address.
			 */
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			if (ni != null) {
				byte[] mac = ni.getHardwareAddress();
				if (mac != null) {
					/*
					 * Extract each array of mac address and convert it to hexa
					 * with the following format 08-00-27-DC-4A-9E.
					 */
					for (int i = 0; i < mac.length; i++) {
						System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
					}
				} else {
					System.out.println("Address doesn't exist or is not accessible.");
				}
			} else {
				System.out.println("Network Interface for the specified address is not found.");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	protected static void addDoc(IndexWriter w, String title, String uri) throws IOException {
		Document doc = new Document();
		doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("uri", title, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
		w.addDocument(doc);
	}

	public static class CrawlerFileTraversal {
		private IndexWriter idxWriter;
		private File rootDir;

		public CrawlerFileTraversal(IndexWriter idxWriter, File rootDir) {
			this.idxWriter = idxWriter;
			this.rootDir = rootDir;
		}

		public IndexWriter getIdxWriter() {
			return idxWriter;
		}

		public void setIdxWriter(IndexWriter idxWriter) {
			this.idxWriter = idxWriter;
		}

		public File getRootDir() {
			return rootDir;
		}

		public void setRootDir(File rootDir) {
			this.rootDir = rootDir;
		}

		public void start() {
			if (this.idxWriter != null && rootDir != null) {
				if (rootDir.isDirectory())
					try {
						this.traverse(rootDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}

		protected final void traverse(final File f) throws IOException {
			if (f.isDirectory()) {
				onDirectory(f);
				final File[] childs = f.listFiles();
				for (File child : childs) {
					traverse(child);
				}
				return;
			}
			onFile(f);
		}

		protected void onDirectory(final File d) {
		}

		protected void onFile(final File f) {
			System.out.println("Indexing : " + f.getAbsolutePath());
			try {
				Document doc = new Document();
				doc.add(new Field("title", f.getName(), Field.Store.YES, Field.Index.ANALYZED));
				doc.add(new Field("uri", f.getAbsolutePath().replace("\\","/"), Field.Store.YES, Field.Index.ANALYZED));
				this.idxWriter.addDocument(doc);
				
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
