package dk.au.cs.p2pn.india;

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
	 * Answers a XML-RPCall by a different Peer as part of our Protocol.
	 *
	 *
	 * @param IdArg ID of the Peer that called this function
	 * @param IPArg IP of the Peer that called this function
	 * @param portArg Port of the Peer that called this function
	 * @param capacityArg Capacity of the Peer that called this function
	 * @return Vector containing the
	 */
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg) {
		this.app.addPeer(new Peer(IdArg, IPArg, portArg, capacityArg));
		return createLocalReturnValue();
	}

	/**
	 * Extension of the regular pong() function. If the flag `neighbornegotiation` is set,
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
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, boolean isNeighborRequest) {
		logger.debug("Called pong with request");

		// Create Peer object
		Peer inPeer = new Peer(IdArg, IPArg, portArg, capacityArg);
		this.app.addPeer(inPeer);

		if(isNeighborRequest){
			boolean neighborAnswer = responseNegotiate(inPeer);
			if(neighborAnswer){
				this.app.addNeighbor(inPeer, neighborAnswer);
			}
			logger.debug("Answering neighbor request from {}:{} with {}", IPArg, portArg, neighborAnswer);
			// TODO: Call check on answer
			return createLocalReturnValue(true, neighborAnswer);
		}

		return createLocalReturnValue();

	}

	public boolean responseNegotiate(Peer inPeer) {
		if(this.app.neighborList.size() + this.app.openNeighborRequests.size() >= this.app.getCapacity()) {
			return false;
		}
		return Math.random() < (double) ((double) inPeer.getCapacity() / (double) this.app.getCapacity());
	}

	private Vector createLocalReturnValue(){
		return this.createLocalReturnValue(false, false);
	}

	private Vector createLocalReturnValue(boolean isNeighborRequest, boolean neighborRequestAnswer) {
		Vector data = PeerApp.createVectorForPeer(this.peer);
		System.out.println("The length of the return value is " + data.size() + data);
		if(isNeighborRequest){
			data.add(neighborRequestAnswer);
		}
		return data;
	}

	/**
	 * Answers the call to `communication.getPeerList`
	 *
	 * @return String-Peer pairs of all known peers.
	 */
	public Hashtable<String, Vector> getPeerList(){
		return PeerApp.createExchangeData(this.app.getPeerList());
	}

	/**
	 * Answers the call to `communication.getNeighborList`
	 *
	 * @return List with all Neighbors in vector representation
	 */
	public Vector<Vector> getNeighborList(){
		Hashtable<String, Vector> data  = PeerApp.createExchangeData(this.app.getNeighborList());
		return new Vector<Vector>(data.values());
	}
}