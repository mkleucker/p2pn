package dk.au.cs.p2pn.india.search;

import dk.au.cs.p2pn.india.Peer;
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

	private int walkerCount;
	/** Each time a search is not success, the weight is divided by DEC. */
	public static final double DEC = 1.2;

	public AdvancedWalkerSearch(String id, String filename, int ttl, Peer source){
		super(id, filename, ttl, source);
		this.setType(SearchTypes.AK_WALKER_SEARCH);
		this.walkerCount = 5;
	}

	public AdvancedWalkerSearch(String id, String filename, int ttl, Peer source, int walkerCount){
		this(id, filename, ttl, source);
		this.walkerCount = walkerCount;
	}

	public int getWalkerCount() {
		return walkerCount;
	}
}
