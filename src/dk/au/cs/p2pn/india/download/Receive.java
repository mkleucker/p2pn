package dk.au.cs.p2pn.india.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlrpc.XmlRpcClient;

public class Receive {

/*	TODO
	- bytes to file. 
	- store received file. 
	- request method. 
*/
	

//	XmlRpcClient client = new XmlRpcClient();
	
	public static void createFile(byte[] data, String fileName) throws IOException{

    	FileOutputStream output;
        File newFile=new File(fileName);
        newFile.createNewFile();
        output = new FileOutputStream(newFile);
        output.write(data);
        output.close();
        
    }
	
	
	
	
	
}
