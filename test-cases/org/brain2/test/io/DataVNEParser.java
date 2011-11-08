package org.brain2.test.io;

import java.io.FileInputStream;
import java.util.List;

import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;

public class DataVNEParser {
	
	public static void main(String[] args) {
		String filePath = "D:/Researchs/Data VNE/FD/36/Body00.vne";
		try {
			//String data = FileUtils.loadFilePathToString(filePath);
			Source source=new Source(new FileInputStream(filePath));
			System.out.println(source.toString());
			displaySegments(source.getAllTags(StartTagType.SERVER_COMMON));			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void displaySegments(List<? extends Segment> segments) {
		for (Segment segment : segments) {
			System.out.println("-------------------------------------------------------------------------------");
			//System.out.println(segment.getDebugInfo());
			System.out.println(segment);
		}
		System.out.println("\n*******************************************************************************\n");
	}
}
