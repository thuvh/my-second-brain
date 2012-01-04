package org.brain2.test.parser.pdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

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
		String filePath = "D:/DOCUMENTS/Bayesian_Artificial_Intelligence.pdf";
		
		
		
		PDDocument document = null;
		FileInputStream file = null;
		try {
			file = new FileInputStream(filePath);
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
			
			System.out.println("### Analysing bookmark ### of PDF file: " + filePath);			
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
		while (current != null) {			
			int pageNum = allPages.indexOf(current.findDestinationPage(document)) + 1;
			System.out.println(indentation + current.getTitle() + " " + pageNum );
			printBookmark(current, indentation + "    ", document, allPages);
			current = current.getNextSibling();
		}

	}
}
