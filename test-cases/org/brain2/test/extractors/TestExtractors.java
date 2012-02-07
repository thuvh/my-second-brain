package org.brain2.test.extractors;


import java.util.Vector;

import org.brain2.ws.core.extractors.HTMLLinkExtractor;
import org.brain2.ws.core.extractors.HTMLLinkExtractor.HtmlLink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExtractors {
	private HTMLLinkExtractor htmlLinkExtrator;

	@Before
	public void init() {
		htmlLinkExtrator = new HTMLLinkExtractor();
	}

	public Object[][] HTMLContentProvider() {
		return new Object[][] {
				new Object[] { "abc hahaha <a href='http://www.google.com'>google</a>" },
				new Object[] { "abc hahaha <a HREF='http://www.google.com'>google</a>" },
				new Object[] { "abc hahaha <A HREF='http://www.google.com'>google</A> , "
						+ "abc hahaha <A HREF='http://www.google.com' target='_blank'>google</A>" },
				new Object[] { "abc hahaha <A HREF='http://www.google.com' target='_blank'>google</A>" },
				new Object[] { "abc hahaha <A target='_blank' HREF='http://www.google.com'>google</A>" },
				new Object[] { "abc hahaha <a HREF=http://www.google.com>google</a>" }, };
	}

	@Test
	public void test() {
		Object[][] tests = HTMLContentProvider();
		for (Object[] objects : tests) {
			for (Object html : objects) {
				System.out.println(html);
				Vector<HtmlLink> links = htmlLinkExtrator.grabHTMLLinks(html.toString());
				Assert.assertTrue(links.size()!=0);
				 
				for(int i=0; i<links.size() ; i++){
					HtmlLink htmlLinks = links.get(i);
					System.out.println(htmlLinks);
				}
			}
			
		}
		
		//
	}

}
