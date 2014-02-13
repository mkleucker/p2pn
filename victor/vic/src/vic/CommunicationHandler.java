package vic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;
import java.util.Vector;


public class CommunicationHandler {
	private Peer peer;
	private PeerApp app;

	private static final Logger logger = LogManager.getLogger(CommunicationHandler.class.getName());

	public CommunicationHandler(Peer peer, PeerApp app){
		this.peer = peer;
		this.app = app;
	}

	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, int depthInt) {
		this.app.addPeer(new Peer(IdArg, IPArg, portArg, capacityArg));

		return createLocalReturnValue(peer);
	}

	/**
	 * Extension of the regular pong() function. If the flag `neighbornegotiation` is set,
	 * then it signals that the other peer want to be my neighbor. Hence I have to
	 * decide and reply accordingly.
	 *
	 * @param IdArg
	 * @param IPArg
	 * @param portArg
	 * @param capacityArg
	 * @param depthInt
	 * @param neighboranswer
	 * @return
	 */
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, int depthInt, boolean neighboranswer) {

		// Create Peer object
		Peer inPeer = new Peer(IdArg, IPArg, portArg, capacityArg);
		this.app.addPeer(inPeer);

		if(neighboranswer){
			boolean neighborAnswer = responseNegotiate(inPeer);
			// TODO: Call check on answer
			return createLocalReturnValue(peer, true, neighborAnswer);
		}

		return createLocalReturnValue(peer);

	}

	public boolean responseNegotiate(Peer inPeer) {
		if(this.app.neighborList.size() >= this.app.getCapacity()) {
			return false;
		}
		if(Math.random() < (double)((double)inPeer.getCapacity() / (double)this.app.getCapacity())) {
			return true;
		}
		return false;
	}

	private Vector createLocalReturnValue(Peer peer){
		return this.createLocalReturnValue(peer, false, false);
	}

	private Vector createLocalReturnValue(Peer peer, boolean isNeighborRequest, boolean neighborRequestAnswer) {
		Vector data = PeerApp.createVectorForPeer(this.peer, 0);
		if(isNeighborRequest){
			data.add(neighborRequestAnswer);
		}
		return data;

	}

	public Hashtable<String, Vector> getPeerList(){
		return PeerApp.createExchangeData(this.app.getPeerList());
	}
}
