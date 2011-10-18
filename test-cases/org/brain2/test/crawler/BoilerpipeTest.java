package org.brain2.test.crawler;

import java.net.MalformedURLException;
import java.net.URL;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

public class BoilerpipeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final URL url = new URL("http://vnexpress.net/gl/vi-tinh/2011/10/nguoi-dung-than-phien-ve-chat-luong-man-hinh-iphone-4s/");

			// This can also be done in one line:
			System.out.println(DefaultExtractor.INSTANCE.getText(url));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
