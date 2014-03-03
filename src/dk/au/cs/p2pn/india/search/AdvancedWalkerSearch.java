package dk.au.cs.p2pn.india.search;

import java.util.Vector;

import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;
/**
 * 
 * @author johnny
 *
 * Advanced walker search, automatically updates the weights of each neighbor so that 
 * the searching message is most likely sent to the neighbor with the highest probability
 * of success.
 * 
 */
public class AdvancedWalkerSearch extends BasicSearch {

	private int walkerCount = 5;
	/** Each time a search is not success, the weight is divided by DEC. */

	public static final double DEC = 1.2;
	private Vector<Peer> path = new Vector<Peer>();

	public AdvancedWalkerSearch(String id, String filename, int ttl, Peer source){
		super(id, filename, ttl, source);
		this.setType(SearchTypes.AK_WALKER_SEARCH);
	}

	public AdvancedWalkerSearch(String id, String filename, int ttl, Peer source, int walkerCount){
		this(id, filename, ttl, source);
		this.walkerCount = walkerCount;
	}

	public void addToPath(Peer peer){
		this.path.add(peer);
	}

	public void setPath(Vector<Peer> path){
		this.path = path;
	}

	public Vector<Peer> getPath(){
		return this.path;
	}

	public int getWalkerCount() {
		return walkerCount;
	}

	/**
	 * Return a vector representation of the search containing:
	 *   * Source Peer
	 *   * Filename
	 *   * TTL
	 *   * ID
	 *   * SearchType
	 * @return Vector to be sent to other peers
	 */
	public Vector<Object> toVector(){
		Vector<Object> params = new Vector<Object>();
		params.add(CommunicationConverter.createVector(this.getSource()));
		params.add(this.getFilename());
		params.add(this.getTtl());
		params.add(this.getId());
		params.add(this.getType().getValue());
		if(this.getSuccess() != null){
			params.add(CommunicationConverter.createVector(this.getSuccess()));
		}
		params.add(getPathAsVectors());
		return params;
	}
	
	@SuppressWarnings("rawtypes")
	private Vector<Vector> getPathAsVectors(){
		Vector<Vector> vPeers = new Vector<Vector>();
		for (Peer p : this.path){
			vPeers.add(CommunicationConverter.createVector(p));
		}
		return vPeers;
	}
}
