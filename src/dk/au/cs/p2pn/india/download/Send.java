package dk.au.cs.p2pn.india.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Send {

	/**
	 * Method for create a bytes array from a file
	 * 
	 * @param File to be converted to bytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] fileToBytes(File file) throws IOException {
		InputStream input = new FileInputStream(file);	    
	    long fileSize = file.length();
	        
	    byte[] bytesArray = new byte[(int)fileSize];	    

	    int offset = 0;
	    int bytesReaded = 0;
	    while (offset < bytesArray.length && (bytesReaded=input.read(bytesArray, offset, bytesArray.length-offset)) >= 0) {
	            offset += bytesReaded;
	        }	        
	        input.close();
	        return bytesArray;
	    }


}
