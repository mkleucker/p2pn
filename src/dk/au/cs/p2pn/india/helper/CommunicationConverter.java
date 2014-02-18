package dk.au.cs.p2pn.india.helper;

import dk.au.cs.p2pn.india.Peer;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Created by max on 18/02/14.
 */
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

	public static Peer createPeer(Vector data) {
		return new Peer((Integer) data.get(0),
				(String) data.get(1),
				(Integer) data.get(2),
				(Integer) data.get(3));
	}
}
