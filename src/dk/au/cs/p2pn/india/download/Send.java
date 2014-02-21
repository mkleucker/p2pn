package dk.au.cs.p2pn.india.download;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import dk.au.cs.p2pn.india.Peer;

public class Send {
	
	/*
	 * TODO
	 * 
	- open file from system. 
	- file to bytes
	- sending method.
	*/

	XmlRpcClient client;
	
	Peer peer;
	File pFile;	
	
	public File openFile(){
		File file = null;		
		return file;		
	}
	
	/*
	 * file request 
	 * 
	 * */
	public String getFile(String fileDir) {
		
		Vector<Object> params = new Vector<Object>();
		params.add(pFile);	
		String result = null;
		
		try {
			result = (String) client.execute("Trans.getFile", params);
			this.client = new XmlRpcClient("http://" + peer.getIP() + ':' + peer.getId() + '/');
			this.client.execute("download.getFile", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	

}
