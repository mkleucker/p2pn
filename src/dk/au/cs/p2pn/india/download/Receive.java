package dk.au.cs.p2pn.india.download;

import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class Receive implements Runnable{

/*	TODO
	- bytes to file & - store received file.  
	- request method   
*/
	


	String fileName;
	PeerApp app;
	XmlRpcClient client;
	String ip; 
	int port; 


	private static final Logger logger = LogManager.getLogger(Receive.class.getSimpleName());
	
	/**
	 * @param app PeerApp 
	 * @param fileName Name of the file to be downloaded
	 * @param ip IP direction of the Peer
	 * @param port Port number of the Peer
	 */
	public Receive(PeerApp app, String fileName, String ip, int port) {
		this.fileName = fileName;
		this.app = app;
		this.ip = ip;
		this.port = port; 
		this.fileName = fileName;
		
	}
	
	/**
	 * Method to create a file from a bytes array
	 * 
	 * @param data bytes of the file
	 * @param fileName name of the file 
	 * @throws IOException
	 */
	public static void createFile(byte[] data, String fileName) throws IOException{
    	FileOutputStream output;
    	String path = "downloads/";
    	// Folder creation
    	File folder = new File("downloads");
    	folder.mkdirs();
    	path += fileName;
    	// File creation 
        File newFile=new File(path);
        newFile.createNewFile();
        output = new FileOutputStream(newFile);
        output.write(data);
        output.close();        
    }
	
	
	public void run() {
		
		Vector<Object> params = new Vector<Object>();				 
		params.add(fileName);
		byte[] res = null;		
		
		try {
			logger.info("Inside receive thread, ready to ask peer with IP {} and port {}",  ip, port);
			this.client = ClientRequestFactory.getClient("http://" + ip + ':' + port + '/');
			logger.info("Inside the receive thread, ready to execute");
			res = (byte[])this.client.execute("communication.getFile", params);			
			createFile(res,fileName);
			logger.info("File downloaded. (Name: {} )", fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
}
