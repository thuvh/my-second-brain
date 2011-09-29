package org.brain2.ws.core.search;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
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
	
	
}
