package org.brain2.test.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

public class luceneIndexIncrement {

	public static void main(String[] args) {
		try {
			luceneIndexIncrement index = new luceneIndexIncrement();
			String path = "d:\\index";// Index file storage path
			String storeIdPath = "d:\\storeId.txt";// id storage path
			String storeId = "";
			storeId = index.getStoreId(storeIdPath);
			ResultSet rs = index.getResult(storeId);
			index.indexBuilding(path, storeIdPath, rs);
			storeId = index.getStoreId(storeIdPath);
			System.out.println(storeId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResultSet getResult(String storeId) throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		String url = "jdbc:mysql://localhost:3306/ding";
		String userName = "root";
		String password = "ding";
		Connection conn = DriverManager.getConnection(url, userName, password);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from newitem where id > '" + storeId + "'order by id");
		return rs;
	}

	public boolean indexBuilding(String path, String storeIdPath, ResultSet rs) {

		try {
			Analyzer luceneAnalyzer = new StandardAnalyzer(SimpleLuceneTest.VERSION);
			// Get stored in the ID, to determine is the incremental index or
			// re-index
			boolean isEmpty = true;
			try {
				File file = new File(storeIdPath);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileReader fr = new FileReader(storeIdPath);
				BufferedReader br = new BufferedReader(fr);
				if (br.readLine() != null) {
					isEmpty = false;
				}
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SimpleLuceneTest.VERSION, luceneAnalyzer);			
			IndexWriter writer = new IndexWriter(SimpleLuceneTest.getIndexDirectory(), indexWriterConfig);
			
			// IndexWriter writer = new IndexWriter(path,
			// luceneAnalyzer);//Argument 'isEmpty' is false that incremental
			// index
			String storeId = "";
			boolean indexFlag = false;
			String id;
			String title;
			while (rs.next()) {
				// for(Iterator it = list.iterator();it.hasNext();){
				id = rs.getString("id");
				title = rs.getString("title");
				writer.addDocument(Document(id, title));
				storeId = id;
				indexFlag = true;
			}
			writer.optimize();
			writer.close();
			if (indexFlag) {
				// save the last id to disk file
				this.writeStoreId(storeIdPath, storeId);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error" + e.getClass() + "\n   error info:   " + e.getMessage());
			return false;
		}

	}

	public static Document Document(String id, String title) {
		Document doc = new Document();
		doc.add(new Field("ID", id, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("TITLE", title, Field.Store.YES, Field.Index.ANALYZED));
		return doc;
	}

	// To obtain the ID stored in the disk
	public static String getStoreId(String path) {
		String storeId = "";
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			storeId = br.readLine();
			if (storeId == null || storeId == "")
				storeId = "0";
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return storeId;
	}

	// The ID is written to disk file
	public static boolean writeStoreId(String path, String storeId) {
		boolean b = false;
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(path);
			PrintWriter out = new PrintWriter(fw);
			out.write(storeId);
			out.close();
			fw.close();
			b = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
}
