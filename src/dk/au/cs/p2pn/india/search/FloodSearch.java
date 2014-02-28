package dk.au.cs.p2pn.india.search;


import dk.au.cs.p2pn.india.Peer;

public class FloodSearch extends BasicSearch{

	public FloodSearch(String id, String filename, int ttl, Peer source){
		super(id, filename, ttl, source);
		this.setType(SearchTypes.FLOOD_SEARCH);
	}
}
