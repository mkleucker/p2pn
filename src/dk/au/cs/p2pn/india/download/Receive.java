package dk.au.cs.p2pn.india.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.PeerApp;
import dk.au.cs.p2pn.india.communication.ClientRequestFactory;
import dk.au.cs.p2pn.india.tasks.SearchTask;

public class Receive implements Runnable{

/*	TODO
	- bytes to file & - store received file.  
	- request method   
*/
	

//	XmlRpcClient client = new XmlRpcClient();
	
	String fileName;
	PeerApp app;
	Peer peer;
	XmlRpcClient client;	
	String ip; 
	int port; 


	private static final Logger logger = LogManager.getLogger(SearchTask.class.getSimpleName());
	
	public Receive(PeerApp app, String fileName, String ip, int port) {
		this.fileName = fileName;
		this.app = app;
		this.ip = ip;
		this.port = port; 
		this.fileName = fileName;
		
	}
	
	/**
	 * method to create a file from a bytes array
	 * 
	 * @param data bytes of the file
	 * @param fileName name of the file 
	 * @throws IOException
	 */
	public static void createFile(byte[] data, String fileName) throws IOException{
    	FileOutputStream output;
        File newFile=new File(fileName);
        newFile.createNewFile();
        output = new FileOutputStream(newFile);
        output.write(data);
        output.close();        
    }
	
	
	public void run() {
		
		Vector<Object> params = new Vector<Object>();		
		String fileName = null; 
		params.add(fileName);
		byte[] res = null;
		
		try {
			logger.info("Inside receive thread, ready to ask peer with IP {} and port {}",  ip, port);
			this.client = ClientRequestFactory.getClient("http://" + ip + ':' + port + '/');
			res = (byte[])this.client.execute("communication.getFile", params);			
			logger.info("Inside the receive thread, ready to execute");
			createFile(res,fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
}
