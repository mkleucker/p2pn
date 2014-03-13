package dk.au.cs.p2pn.india.search;

import dk.au.cs.p2pn.india.Peer;

public class WalkerSearch extends BasicSearch {

	private int walkerCount;

	public WalkerSearch(String id, String filename, int ttl, Peer source){
		super(id, filename, ttl, source);
		this.setType(SearchTypes.K_WALKER_SEARCH);
	}

	public WalkerSearch(String id, String filename, int ttl, Peer source, int walkerCount){
		this(id, filename, ttl, source);
		this.walkerCount = walkerCount;
	}

	public int getWalkerCount() {
		return walkerCount;
	}
}
