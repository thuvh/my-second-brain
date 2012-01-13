package com.vnexpress.cronjob;


import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;



public class UrlDownload {
	final static int size = 2048;

	public static String fileUrl(String fileUrl, String localFileName,String destinationDir) {
		OutputStream outStream = null;
		URLConnection uCon = null;
		String filePath = destinationDir + "\\" + localFileName;
		InputStream is = null;
		try {
			URL Url;
			byte[] buf;
			int ByteRead, ByteWritten = 0;
			Url = new URL(fileUrl);
			outStream = new BufferedOutputStream(new FileOutputStream(filePath));

			uCon = Url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
			}
			System.out.println("Downloaded Successfully.");
			System.out.println("File name:\"" + localFileName
					+ "\"\nNo ofbytes :" + ByteWritten);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return filePath;
	}

	public static String fileDownload(String fAddress, String destinationDir) {

		int slashIndex = fAddress.lastIndexOf('/');
		int periodIndex = fAddress.lastIndexOf('.');

		String fileName = fAddress.substring(slashIndex + 1);

		if (periodIndex >= 1 && slashIndex >= 0
				&& slashIndex < fAddress.length() - 1) {
			return fileUrl(fAddress, fileName, destinationDir);
		} else {
			System.err.println("path or file name.");
		}
		return "";
	}

	public static void main(String[] args) {
		if(args.length != 3){
			return;
		} 	
		
		try {
			String fileUrl = args[0];//"http://mapi.vnexpress.net/photo_files/458/0d5b1c4c7f720f698946c7f6ab08f687_1326440538.jpg";
			int w = Integer.parseInt(args[1]);//320
			int h = Integer.parseInt(args[2]);//480
			String dir = "file_temp";
			String filePath = fileDownload(fileUrl, dir);	
			
			BufferedImage image = ImageIO.read(new File(filePath));
			
			image = ImageUtil.blurImage(image);
			
			BufferedImage newImage = ImageUtil.scaleImage(image, w, h);
			ImageIO.write(newImage, "JPG", new File(filePath));
			image.flush();
			newImage.flush();
			image = null;
			newImage = null;
			System.out.println("scaleImage done: " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}