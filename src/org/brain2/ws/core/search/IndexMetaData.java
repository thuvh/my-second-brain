package org.brain2.ws.core.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

public class IndexMetaData {
	public void indexLink(String href, String title, String description, String tags) {

		try {
			
			QueryLinkMetaData theQueryLink = new QueryLinkMetaData();
			int oldDocId = theQueryLink.queryDocIdByUrl(href);
			
			if(oldDocId >= 0){
				IndexReader reader = MetaDataUtil.getIndexReader(false);
				reader.deleteDocument(oldDocId);
				reader.close();				
			}

			// 2. create new doc, set fields
			Document newDoc = MetaDataUtil.createDocumentForLink(href, title, description, tags);
						
			IndexWriter indexWriter = MetaDataUtil.getIndexWriter();
			indexWriter.addDocument(newDoc);			
			indexWriter.optimize();
			indexWriter.close();	
			System.out.println("Add OK 1 doc for href:"+href);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
