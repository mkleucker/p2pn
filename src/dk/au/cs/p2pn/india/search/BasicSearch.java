package dk.au.cs.p2pn.india.search;


import dk.au.cs.p2pn.india.Peer;
import dk.au.cs.p2pn.india.helper.CommunicationConverter;

import java.util.Vector;

public abstract class BasicSearch {
	private int ttl = 0;
	private String id;
	private String filename;
	private Peer source;
	private SearchTypes type;



	private Peer success;

	public BasicSearch(String id, String filename, int ttl, Peer source){
		this.id = id;
		this.filename = filename;
		this.ttl = ttl;
		this.source = source;
	}

	public int getTtl() {
		return ttl;
	}

	public String getId() {
		return id;
	}

	public String getFilename() {
		return filename;
	}

	public Peer getSource() {
		return source;
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

	public Peer getSuccess() {
		return success;
	}

	public void setSuccess(Peer success) {
		this.success = success;
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
		params.add(CommunicationConverter.createVector(this.source));
		params.add(this.filename);
		params.add(this.ttl);
		params.add(this.id);
		params.add(this.type.getValue());
		if(this.success != null){
			params.add(CommunicationConverter.createVector(this.success));
		}
		return params;
	}
}


