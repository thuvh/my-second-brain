package org.brain2.test.parser.pdf;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFTextStripperByArea;
import org.brain2.ws.core.utils.FileUtils;

/**
 * This is an example on how to access the bookmarks that are part of a pdf
 * document.
 * 
 */
public class PrintBookmarks {
	/**
	 * This will print the documents data.
	 * 
	 * @param args The command line arguments.
	 * 
	 * @throws Exception
	 *             If there is an error parsing the document.
	 */
	public static void main(String[] args) throws Exception {
		//String filePath = "D:/EBOOKS/Packt.Solr 1.4 Enterprise Search Server.pdf";
		
		String strURI = "file:///D:/EBOOKS/OReilly,2011,Learning Android.pdf";		
		strURI = strURI.replace(" ", "%20");
		URI uri = new URI(strURI);		
		
		PDDocument document = null;
		FileInputStream file = null;
		try {
			file = new FileInputStream(new File(uri));
			PDFParser parser = new PDFParser(file);
			parser.parse();
			document = parser.getPDDocument();
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (InvalidPasswordException e) {
					System.err.println("Error: Document is encrypted with a password.");
					System.exit(1);
				}
			}
			PrintBookmarks meta = new PrintBookmarks();
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			PDDocumentOutline outline = catalog.getDocumentOutline();
			
			PDFTextStripper stripper = new PDFTextStripper("utf-8");
			String content = stripper.getText(document);
			System.out.println(content);
			FileUtils.writeStringToFile("D:/pdf-content.txt", content);
			System.exit(1);
			
			System.out.println("### Analysing bookmark ### of PDF file: " + uri);			
			System.out.println("number Pages: " + document.getNumberOfPages());
			List allPages = catalog.getAllPages();
			if (outline != null) {
				meta.printBookmark(outline, "", document, allPages);
			} else {
				System.out.println("This document does not contain any bookmarks");
			}
		} finally {
			if (file != null) {
				file.close();
			}
			if (document != null) {
				document.close();
			}
		}

	}

	/**
	 * This will print the documents bookmarks to System.out.
	 * 
	 * @param bookmark  The bookmark to print out.
	 * @param indentation  A pretty printing parameter	
	 * @throws IOException If there is an error getting the page count.
	 */
	public void printBookmark(PDOutlineNode bookmark, String indentation,
			PDDocument document, List allPages) throws IOException {
		PDOutlineItem current = bookmark.getFirstChild();
		PDFTextStripperByArea tripper = new PDFTextStripperByArea();

		
		tripper.setSortByPosition(true);
		Rectangle rectangle = new Rectangle(100, 100, 200, 500);
		tripper.addRegion("class1", rectangle);
		
		while (current != null) {			
			PDPage page = current.findDestinationPage(document);
			tripper.extractRegions(page);
			System.out.println("\n#\n" + tripper.getTextForRegion("class1") + "\n#\n");				
						
			//int pageNum = allPages.indexOf(page) + 1;
			//System.out.println(indentation + current.getTitle() + " " + pageNum );
			printBookmark(current, indentation + "  ", document, allPages);
			current = current.getNextSibling();
		}

	}
}
