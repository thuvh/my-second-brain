package org.brain2.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FolderUnZiper {
	public static void main(String[] args) throws Exception {
		String destinationname = "D:/data/";
		byte[] buf = new byte[1024];
		ZipInputStream zipinputstream = null;
		ZipEntry zipentry;
		zipinputstream = new ZipInputStream(new FileInputStream("D:/data/my-second-brain.zip"));
		zipentry = zipinputstream.getNextEntry();
		while (zipentry != null) {
			String entryName = zipentry.getName();
			FileOutputStream fileoutputstream;
			File newFile = new File(entryName);
			String directory = newFile.getParent();

			if (directory == null) {
				if (newFile.isDirectory())
					break;
			}
			fileoutputstream = new FileOutputStream(destinationname + entryName);
			int n;
			while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
				fileoutputstream.write(buf, 0, n);
			}
			fileoutputstream.close();
			zipinputstream.closeEntry();
			zipentry = zipinputstream.getNextEntry();
		}
		zipinputstream.close();
	}
}