package org.brain2.test.vneappcrawler.junit;

import static org.junit.Assert.fail;

import org.brain2.test.vneappcrawler.VnExpressDao;
import org.junit.Assert;
import org.junit.Test;

public class TestVnExpressDao {

	@Test
	public void getTotalOldSubject() {
		
		try {
			VnExpressDao _vnExpressDao = VnExpressDao.getInstance();
			int total = _vnExpressDao.getTotalCountInVnExpress();
			System.out.println(total);
			Assert.assertTrue(total > 0);
		} catch (Exception e) {			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
