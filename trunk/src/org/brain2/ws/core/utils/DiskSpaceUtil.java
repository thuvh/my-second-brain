package org.brain2.ws.core.utils;

import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;

public class DiskSpaceUtil {
	
	public static double currentDiskFreeSpaceGB(){
		try {			 
            //calculate free disk space
            double freeDiskSpace = FileSystemUtils.freeSpaceKb("."); 
 
            //convert the number into gigabyte
            double freeDiskSpaceGB = freeDiskSpace / 1024 / 1024;
            
            return freeDiskSpaceGB;
        } catch (IOException e) {
            e.printStackTrace();
        }
		return 0;
	}

}
