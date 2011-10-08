package org.brain2.test.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.brain2.ws.core.search.QueryLinkMetaData;

public class TestQueryLinkMetaData {
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {
		QueryLinkMetaData theQuery = new QueryLinkMetaData();
		//int docId = theQuery.queryDocIdByUrl("http://www.youtube.com/watch?v=KWXZW-h7D0o&feature=related");
		
		List<Document> docs = theQuery.queryDocsByKeywords("iphone_dev");		
		for (Document doc : docs) {
			System.out.println("href:"+doc.get("href"));
			System.out.println("title:"+doc.get("title"));
			System.out.println("description:"+doc.get("description"));
			System.out.println("tags:"+doc.get("tags"));
			System.out.println("");
		}		
	}
}
