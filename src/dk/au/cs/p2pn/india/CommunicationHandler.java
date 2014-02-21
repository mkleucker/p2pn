package dk.au.cs.p2pn.india;

import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.helper.ReporterMeasurements;

import dk.au.cs.p2pn.india.tasks.SearchSuccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CommunicationHandler {
	private Peer peer;
	private PeerApp app;

	private static final Logger logger = LogManager.getLogger(CommunicationHandler.class.getSimpleName());

	public CommunicationHandler(Peer peer, PeerApp app){
		this.peer = peer;
		this.app = app;
	}

	/**
	 * XML-RPC: Answers a XML-RPCall by a different Peer as part of our Protocol.
	 *
	 *
	 * @param IdArg ID of the Peer that called this function
	 * @param IPArg IP of the Peer that called this function
	 * @param portArg Port of the Peer that called this function
	 * @param capacityArg Capacity of the Peer that called this function
	 * @return Vector containing the
	 */
	@SuppressWarnings("unused")
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg) {
		this.app.getReporter().addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		this.app.addPeer(new Peer(IdArg, IPArg, portArg, capacityArg));
		return CommunicationConverter.createVector(this.peer);
	}

	/**
	 * XML-RPC: Extension of the regular pong() function. If the flag `neighbornegotiation` is set,
	 * then it signals that the other peer want to be my neighbor. Hence I have to
	 * decide and reply accordingly.
	 *
	 *
	 * @param IdArg ID of the Peer that called this function
	 * @param IPArg IP of the Peer that called this function
	 * @param portArg Port of the Peer that called this function
	 * @param capacityArg Capacity of the Peer that called this function
	 * @param isNeighborRequest Flag whether the requesting Peer wants to be our neighbor
	 * @return Vector containing the
	 */
	@SuppressWarnings("unused")
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, boolean isNeighborRequest) {
		this.app.getReporter().addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		// Create Peer object
		Peer inPeer = new Peer(IdArg, IPArg, portArg, capacityArg);
		this.app.addPeer(inPeer);

		if(isNeighborRequest){
			this.app.getReporter().addEvent(ReporterMeasurements.NEIGHBOR_REQUEST_RECEIVED);
			boolean neighborAnswer = responseNegotiate(inPeer);
			if(neighborAnswer){
				this.app.addNeighbor(inPeer, neighborAnswer);
			}
			logger.debug("Answering neighbor request from {}:{} with {}", IPArg, portArg, neighborAnswer);

			return CommunicationConverter.createVector(this.peer, neighborAnswer);
		}

		return CommunicationConverter.createVector(this.peer);

	}

	public boolean responseNegotiate(Peer inPeer) {
		this.app.getReporter().addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		if(this.app.neighborList.size() + this.app.openNeighborRequests.size() >= this.app.getPeer().getCapacity()) {
			return false;
		}
		return Math.random() < (double) ((double) inPeer.getCapacity() / (double) this.app.getPeer().getCapacity());
	}

	/**
	 * XML-RPC: Answers the call to `communication.getPeerList`
	 *
	 * @return String-Peer pairs of all known peers.
	 */
	@SuppressWarnings("unused")
	public Hashtable<String, Vector> getPeerList(){
		this.app.getReporter().addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		return CommunicationConverter.createVector(this.app.getPeerList());
	}

	/**
	 * XML-RPC: Answers the call to `communication.getNeighborList`
	 *
	 * @return List with all Neighbors in vector representation
	 */
	@SuppressWarnings("unused")
	public Vector<Vector> getNeighborList(){
		this.app.getReporter().addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		Hashtable<String, Vector> data  = CommunicationConverter.createVector(this.app.getNeighborList());
		return new Vector<Vector>(data.values());
	}
	
	/**
	 * XML-RPC: Answers the call to `communication.respondSearch`, it will return immediately. If it has the file,
	 * 			it will start a new thread to give a successful result to the caller before returning. Otherwise
	 * 			if the ttl is positive, then it passes the search to all the peers in its peer list.
	 *
	 * @param	The message of searching
	 */
	public void respondSearch(Vector<Object> origin, String fileName, Integer ttl, String ident) {
		
		if (ttl.intValue() <= 0)
			return;
		/**
		 * if the file is found, start a thread to tell the origin and return;
		 */
		if (this.app.fileList.containsKey(fileName)) {
			Thread success = new Thread(new SearchSuccess(origin, fileName, ident, this.app.getPeer()));
			success.run();
			return;
		}
		
		this.app.passSearch(origin, fileName, ttl - 1, ident);
		return;
	}
	
	/**
	 * XML-RPC: Answers the call to `communication.respondSuccess` when another peer has the file that the 
	 * 			current peer is looking for, it will return immediately. And it will add the file to the 
	 *			known file list of the local peer.
	 *
	 * @param The message of success, containing the following fields.
	 */
	public void respondSuccess(Vector<Object> origin, String fileName, String ident, Vector<Object> owner) {
		this.app.knownDataList.put(fileName, CommunicationConverter.createPeer(owner));
		return;
	}
}
