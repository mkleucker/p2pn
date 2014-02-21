package dk.au.cs.p2pn.india;

import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.reporting.ReporterMeasurements;
import dk.au.cs.p2pn.india.tasks.SearchSuccessTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;
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
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings("rawtypes")
	public Hashtable<String, Vector> getPeerList(){
		this.app.getReporter().addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		return CommunicationConverter.createVector(this.app.getPeerList());
	}

	/**
	 * XML-RPC: Answers the call to `communication.getNeighborList`
	 *
	 * @return List with all Neighbors in vector representation
	 */
	@SuppressWarnings("rawtypes")
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
	 */
	@SuppressWarnings("rawtypes")
	public Vector respondSearch(Vector<Object> origin, String fileName, int ttl, String ident) {
		logger.info("Inside respondSearch");

		if (ttl <= 0)
			return new Vector();
		/**
		 * if the file is found, start a thread to tell the origin and return;
		 */
		if (this.app.fileList.containsKey(fileName)) {
			logger.info("Inside respondSearch, file matched, starting a new success thread");

			Thread success = new Thread(new SearchSuccessTask(origin, fileName, ident, this.app));
			success.run();
			return new Vector();
		}
		
		/**
		 * Otherwise pass the search to other peers and return.
		 */
		logger.info("Inside respondSearch, file not matched, calling passSearch");
		this.app.passSearch(origin, fileName, ttl - 1, ident);
		return new Vector();
	}
	
	/**
	 * XML-RPC: Answers the call to `communication.respondSuccess` when another peer has the file that the 
	 * 			current peer is looking for, this function will return immediately. And it will add the file to the 
	 *			known file list of the local peer.
	 *
	 */
	@SuppressWarnings("rawtypes")
	public Vector respondSuccess(Vector<Object> origin, String fileName, String ident, Vector<Object> owner) {
		this.app.knownDataList.put(fileName, CommunicationConverter.createPeer(owner));
		logger.info(this.app.knownDataList);
		return new Vector();
	}
}
