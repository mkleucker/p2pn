package dk.au.cs.p2pn.india.helper;


public class Reporter {

	/**
	 * Number of incoming messages this per has processed.
	 */
	private int messagesReceived;
	/**
	 * Number of search queries received from other peers.
	 */
	private int searchesReceived;
	/**
	 * Number of other peers' search queries processed.
	 */
	private int searchesProcessed;
	/**
	 * Number of search request that have been answered successful ("I have this file").
	 */
	private int searchesRespondedSuccessful;

	/**
	 * Searches started by this peer.
	 */
	private int searchesStarted;
	/**
	 * Searches started by this peer that were answered successful.
	 */
	private int searchesSuccessfull;

}
