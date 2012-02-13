package org.brain2.ws.core.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileCopyUtil {
	final static List<String> excludedDirList = new ArrayList<String>();
	final static List<String> excludedFileList = new ArrayList<String>();
	static int totalCopiedFile = 0;
	final static long now = (new Date()).getTime();

	/**
	 * This function will copy files or directories from one location to
	 * another. note that the source and the destination must be mutually
	 * exclusive. This function can not be used to copy a directory to a sub
	 * directory of itself. The function will also have problems if the
	 * destination files already exist.
	 * 
	 * @param src
	 *            -- A File object that represents the source for the copy
	 * @param dest
	 *            -- A File object that represnts the destination for the copy.
	 * @throws IOException
	 *             if unable to copy.
	 */
	public static void copyFiles(File src, File dest) throws IOException {
		if (excludedDirList.contains(src.getName()) && src.isDirectory()) {
			writeLog("- Exclude directory: " + src.getAbsolutePath());
			return;
		}
		if (excludedFileList.contains(src.getName()) && src.isFile()) {
			writeLog("- Exclude file: " + src.getAbsolutePath());
			return;
		}
		// if (src.isFile() && dest.isFile() && compareModifiedFile(src, dest)
		// <= 0) {
		// writeLog("- Skip file: " + src.getAbsolutePath());
		// return;
		// }

		// Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: "
					+ src.getAbsolutePath() + ".");
		} else if (!src.canRead()) { // check to ensure we have rights to the
										// source...
			throw new IOException("copyFiles: No right to source: "
					+ src.getAbsolutePath() + ".");
		}
		// is this a directory copy?
		if (src.isDirectory()) {
			if (!dest.exists()) { // does the destination already exist?
				// if not we need to make it exist if possible (note this is
				// mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException(
							"copyFiles: Could not create direcotry: "
									+ dest.getAbsolutePath() + ".");
				}
			}
			// get a listing of files...
			String list[] = src.list();
			// copy all the files in the list.
			for (int i = 0; i < list.length; i++) {
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFiles(src1, dest1);
			}
		} else {
			// This was not a directory, so lets just copy the file
			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[8192]; // Buffer 8K at a time (you can
											// change this).
			int bytesRead;
			try {
				// open the files for input and output
				fin = new FileInputStream(src);
				fout = new FileOutputStream(dest);
				// while bytesRead indicates a successful read, lets write...
				while ((bytesRead = fin.read(buffer)) >= 0) {
					fout.write(buffer, 0, bytesRead);
				}

				// synchronize 2 version is one
				src.setWritable(true);
				src.setLastModified(now);
				dest.setLastModified(now);
				totalCopiedFile++;

				writeLog("+ Copy " + src.getAbsolutePath() + " to "
						+ dest.getAbsolutePath());
			} catch (IOException e) { // Error copying file...
				IOException wrapper = new IOException(
						"copyFiles: Unable to copy file: "
								+ src.getAbsolutePath() + "to"
								+ dest.getAbsolutePath() + ".");
				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;
			} finally { // Ensure that the files are closed (if they were open).
				if (fin != null) {
					fin.close();
				}
				if (fout != null) {
					fout.close();
				}
			}
		}
	}

	static int compareModifiedFile(File src, File dest) {
		if (src.getUsableSpace() == dest.getUsableSpace()) {
			return 0;
		} else if (src.getUsableSpace() > dest.getUsableSpace()) {
			return 1;
		}
		System.out.println(src.getName() + " " + src.lastModified());
		System.out.println(dest.getName() + " " + dest.lastModified());
		return -1;
	}

	static StringBuilder log_content = new StringBuilder();

	static protected void writeLog(String s) {
		log_content.append(s).append("\n");
		System.out.println(s);
	}

	static public boolean writeLastBuildSummary(String build_dir) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("MMM_dd_yyyy_H.m.s");
			Date now = new Date();
			String formated_date = dateFormat.format(now);
			writeLog("\n----------------------------------------");
			writeLog("### totalCopiedFile: " + totalCopiedFile);
			writeLog("### Build succesfully at " + formated_date);

			Writer output = null;
			File file = new File(build_dir + "/build_date_" + formated_date
					+ ".txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			output = new BufferedWriter(new FileWriter(file));
			output.write(log_content.toString());
			output.close();

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	// static boolean isNewFile
	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static void main(String[] args) {
		buildCrawler();
	}

	static void buildCrawler() {
		excludedDirList.add(".svn");
		excludedDirList.add("agent.jar");
		excludedDirList.add("vne_impoter.jar");
		excludedDirList.add("import-data-log.txt");

		String src = "D:\\Researchs\\my-second-brain";
		String des = "D:\\Researchs\\news-crawler";
		try {
			log_content = new StringBuilder();
			log_content.append("Build Crawler from ").append(src)
					.append(" to ").append(des).append("\n\n");
			copyFiles(new File(src), new File(des));
			// writeLastBuildSummary(des);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
}
