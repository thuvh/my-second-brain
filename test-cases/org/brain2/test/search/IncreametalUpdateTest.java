package org.brain2.test.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;

public class IncreametalUpdateTest {

	public static void main(String[] args) {

		try {
			StandardAnalyzer analyzer = new StandardAnalyzer(SimpleLuceneTest.VERSION);

			Directory directory = SimpleLuceneTest.getIndexDirectory();

			// 1. find doc by docId, keep in handle and delete it
			IndexReader reader = IndexReader.open(directory, false);
			int docId = 149;// Manning.Lucene.in.Action.2nd.Edition.Jun.2010.pdf
			Document d = reader.document(docId);
			System.out.println(d.get("title") + " at URI " + d.get("uri"));
			reader.deleteDocument(docId);
			reader.close();

			// 2. create new doc, set field by
			Document doc2 = new Document();
			doc2.add(new Field("title", d.get("title") + " update", Field.Store.YES, Field.Index.ANALYZED));
			doc2.add(new Field("uri", d.get("uri"), Field.Store.YES, Field.Index.ANALYZED));

			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SimpleLuceneTest.VERSION, analyzer);
			IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
			indexWriter.addDocument(doc2);			
			indexWriter.optimize();
			indexWriter.close();
			

			// 3. recheck
			Query query = new QueryParser(SimpleLuceneTest.VERSION, "uri", analyzer).parse("\""+d.get("uri")+"\"");			
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
			IndexSearcher searcher = new IndexSearcher(directory, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				docId = hits[i].doc;
				Explanation explanation = searcher.explain(query, docId);
				System.out.println("----------");
				Document doc = searcher.doc(docId);
				System.out.println(docId);
				System.out.println(doc.get("title"));
				System.out.println(explanation.toString());
			}
			searcher.close();
			
			directory.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
