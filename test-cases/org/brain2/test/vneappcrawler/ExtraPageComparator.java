package org.brain2.test.vneappcrawler;

import java.util.Comparator;

public class ExtraPageComparator implements Comparator<String>{

	@Override
	public int compare(String o1, String o2) {
		int order = o1.length() - o2.length();
		if(order==0)
			order = o1.compareToIgnoreCase(o2);
		return order;
	}
	
}