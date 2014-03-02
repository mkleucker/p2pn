package dk.au.cs.p2pn.india.search;

import dk.au.cs.p2pn.india.Peer;

public class AdvancedWalkerSearch extends BasicSearch {

	private int walkerCount;
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
