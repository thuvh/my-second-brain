package org.brain2.test.vneappcrawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class VnExpressDaoJUnit {
	private volatile static VnExpressDao _vnExpressDao = null;
	
	public VnExpressDaoJUnit() throws Exception {
		if(_vnExpressDao == null){
			_vnExpressDao = VnExpressDao.getInstance();
		}
		
	}

	@Test
	public void testDate() {
		try {
			Article article = _vnExpressDao.getOldSubjectByPath("/gl/suc-khoe/2011/10/tim-ra-thu-pham-khien-co-gai-26-tuoi-hoa-ba-lao");
			
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
			
			String sDate = formater.format(article.getCreationDate());
			System.out.println(sDate);
			java.util.Date d =  formater.parse(sDate);
			assertEquals(d.getTime(),article.getCreationDate().getTime());
			System.out.println(d.getTime());
			
			System.out.println(formater.format(new Date(1319994000000L)));
			System.out.println(formater.format(new Date(1320638940L)));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
