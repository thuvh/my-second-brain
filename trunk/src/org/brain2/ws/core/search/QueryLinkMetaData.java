package org.brain2.ws.core.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;

public class QueryLinkMetaData {

	IndexSearcher searcher;	

	public QueryLinkMetaData() throws CorruptIndexException, IOException {		
		
	}

	public int queryDocIdByUrl(String href) throws ParseException, CorruptIndexException, IOException {
		Directory indexDirectory = MetaDataUtil.getIndexDirectory();
		if(indexDirectory.listAll().length == 0){
			return -1;
		}
		
		this.searcher = new IndexSearcher(indexDirectory, true);

		String querystr = "\"" + href + "\"";
		Query q = new QueryParser(MetaDataUtil.VERSION, "href", MetaDataUtil.getDefaultAnalyzer()).parse(querystr);

		// 3. search
		int hitsPerPage = 1;

		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		System.out.println("queryDocumentByUrl Found " + hits.length + " hits.");
		int docId = -1;
		if (hits.length == 1) {
			docId = hits[0].doc;			
		}
		this.searcher.close();	
		return docId;
	}
	
	public List<Document> queryDocsByField(String keywords, String fieldname) throws ParseException, CorruptIndexException, IOException {
		Directory indexDirectory = MetaDataUtil.getIndexDirectory();
		if(indexDirectory.listAll().length == 0){
			return new ArrayList<Document>(0);
		}
		
		this.searcher = new IndexSearcher(indexDirectory, true);

		String querystr =  keywords + "*";
		Query q = new QueryParser(MetaDataUtil.VERSION, fieldname, MetaDataUtil.getDefaultAnalyzer()).parse(querystr);

		// 3. search
		int hitsPerPage = 100;

		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		System.out.println("queryDocumentByUrl Found " + hits.length + " hits.");
		List<Document> docs = new ArrayList<Document>(hits.length);
		
		for (ScoreDoc hit : hits) {
			docs.add(this.searcher.doc(hit.doc));
		}		
		
		this.searcher.close();	
		return docs;
	}
	
	public List<Document> queryDocsByKeywords(String keywords) throws ParseException, CorruptIndexException, IOException {
		Directory indexDirectory = MetaDataUtil.getIndexDirectory();
		if(indexDirectory.listAll().length == 0){
			return new ArrayList<Document>(0);
		}
				
		this.searcher = new IndexSearcher(indexDirectory, true);
				
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
				MetaDataUtil.VERSION,
                new String[] {"title", "description"},
                MetaDataUtil.getDefaultAnalyzer());		

		// 3. search
		int hitsPerPage = 100;

		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(queryParser.parse(keywords+"*"), collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		System.out.println("queryDocumentByUrl Found " + hits.length + " hits.");
		List<Document> docs = new ArrayList<Document>(hits.length);
		
		for (ScoreDoc hit : hits) {
			docs.add(this.searcher.doc(hit.doc));
		}		
		
		this.searcher.close();	
		return docs;
	}
	
	
}
