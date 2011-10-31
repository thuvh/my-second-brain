package org.brain2.test.crawler;

import java.net.MalformedURLException;
import java.net.URL;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class BoilerpipeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final URL url = new URL("http://www.zing.vn/news/nhip-song-tre/khi-truong-dai-hoc-dao-tao-nguoi-nam-cham/a131530.html");

			// This can also be done in one line:
			System.out.println(ArticleExtractor.INSTANCE.getText(url));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
