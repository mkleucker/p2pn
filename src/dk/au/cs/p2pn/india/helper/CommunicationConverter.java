package dk.au.cs.p2pn.india.helper;

import dk.au.cs.p2pn.india.Peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;


public class CommunicationConverter {

	/**
	 * Creates request parameters for the current peer.
	 *
	 * @return Vector with all parameters
	 */
	public static Vector<Object> createVector(Peer peer){
		Vector<Object> params = new Vector<Object>();
		params.addElement(peer.getId());
		params.addElement(peer.getIP());
		params.addElement(peer.getPort());
		params.addElement(peer.getCapacity());

		return params;
	}
	/**
	 * Creates request parameters for the current peer.
	 *
	 * @return Vector with all parameters
	 */
	public static Vector<Object> createVector(Peer peer, boolean request){
		Vector<Object> params = new Vector<Object>();
		params.addElement(peer.getId());
		params.addElement(peer.getIP());
		params.addElement(peer.getPort());
		params.addElement(peer.getCapacity());
		params.addElement(request);
		return params;
	}

	/**
	 * Changes the format of the data for the sending process.
	 * From a Map<Integer,Peer> to a Hashtable<String, Vector>
	 *
	 * @param rawData Map of Peers in their Vector representation.
	 * @return Hashtable with the format: Hashtable<String, Vector>
	 */
	public static Hashtable<String, Vector> createVector(Map<Integer, Peer> rawData){
		Hashtable<String, Vector> result = new Hashtable<String, Vector>();

		for (Map.Entry<Integer, Peer> entry : rawData.entrySet()) {
			// TODO: fix depth parameter
			result.put(Integer.toString(entry.getKey()), CommunicationConverter.createVector(entry.getValue()));
		}

		return result;
	}


	public static Vector<Object> createSearchVector(Object peer, String fileName, int ttl, String ident){
		return createSearchSuccessVector(peer, fileName, ttl, ident, null);
	}

	public static Vector<Object> crateSearchKWalkerVector(Object peer, String fileName, int ttl, String id, int checkback){
		return createVector(peer, fileName, ttl, id, checkback);
	}

	public static Vector<Object> createVector(Object... objects){
		Vector<Object> params = new Vector<Object>();
		for (Object o : objects){
			params.add(o);
		}
		return params;
	}

	public static Vector<Object> createSearchSuccessVector(Object peer, String fileName, int ttl, String ident, Object source){
		Vector<Object> params = new Vector<Object>();
		if(peer instanceof Peer){
			params.add(createVector((Peer)peer));
		}else if(peer instanceof Vector){
			params.add(peer);
		}
		params.add(fileName);
		params.add(ttl);
		params.add(ident);
		if(source != null){
			if(source instanceof Peer){
				params.add(createVector((Peer)source));
			}else if(source instanceof Vector){
				params.add(source);
			}
		}
		return params;
	}

	public static Peer createPeer(Vector data) {
		return new Peer((Integer) data.get(0),
				(String) data.get(1),
				(Integer) data.get(2),
				(Integer) data.get(3));
	}


	/**
	 * Method for create a bytes array from a file
	 *
	 * @param file to be converted to bytes
	 * @return Byte array of the file.
	 * @throws java.io.IOException
	 */
	public static byte[] fileToBytes(File file) throws IOException {
		InputStream input = new FileInputStream(file);
		long fileSize = file.length();

		byte[] bytesArray = new byte[(int)fileSize];

		int offset = 0;
		int bytesRead = 0;
		while (offset < bytesArray.length && (bytesRead=input.read(bytesArray, offset, bytesArray.length-offset)) >= 0) {
			offset += bytesRead;
		}
		input.close();
		return bytesArray;
	}

}


