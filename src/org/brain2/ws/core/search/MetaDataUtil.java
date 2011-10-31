package org.brain2.ws.core.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class MetaDataUtil {
	
	public static final Version VERSION = Version.LUCENE_33;
	private static String indexDirectoryPath = "D:/data/my-second-brain";//TODO
	private static Directory indexDirectory;
	private static StandardAnalyzer analyzer;
	private static IndexWriter indexWriter;
	private static IndexReader indexReader;
	
	public static String getIndexDirectoryPath() {
		if (indexDirectoryPath == null) {
			StringBuilder sBuilder = new StringBuilder();
			// sBuilder.append(JiveGlobals.getJiveHome());//TODO
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
	
	public static StandardAnalyzer getDefaultAnalyzer(){
		if (analyzer == null) {
			analyzer = new StandardAnalyzer(VERSION);
		}
		return analyzer;
	}
	
	public static Document createDocumentForLink(String href, String title, String description, String tags) throws Exception{			
		Document doc = new Document();
		doc.add(new Field("href", href, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("description", description, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("tags", tags, Field.Store.YES, Field.Index.ANALYZED));		
		return doc;
	}
	
	public static Document createDocumentForLink(String href, String title, String description, String tags, String content) throws Exception{			
		Document doc = new Document();
		doc.add(new Field("href", href, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("description", description, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("tags", tags, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));
		return doc;
	}
	
	public static IndexReader getIndexReader(boolean readOnly) throws CorruptIndexException, IOException {
		if (indexReader == null) {
			indexReader = IndexReader.open(getIndexDirectory(), readOnly);;
		}
		return indexReader;
	}
	
	public static IndexWriter getIndexWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		if (indexWriter == null) {
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(VERSION, getDefaultAnalyzer());
			indexWriter = new IndexWriter(getIndexDirectory(), indexWriterConfig);			
		}
		return indexWriter;
	}
}
