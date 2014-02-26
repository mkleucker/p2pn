package dk.au.cs.p2pn.india.search;


import dk.au.cs.p2pn.india.Peer;

import java.util.Vector;

public abstract class BasicSearch {
	private int ttl = 0;
	private String id;
	private String filename;
	private Peer source;
	private SearchTypes type;

	public BasicSearch(String id, String filename, int ttl, Peer source){
		this.id = id;
		this.filename = filename;
		this.ttl = ttl;
		this.source = source;
	}

	public void decreaseTtl(){
		this.ttl = this.ttl-1;
	}

	public SearchTypes getType() {
		return type;
	}

	public void setType(SearchTypes type) {
		this.type = type;
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
		params.add(this.source);
		params.add(this.filename);
		params.add(this.ttl);
		params.add(this.id);
		params.add(this.type.getValue());
		return params;
	}
}


