package dk.au.cs.p2pn.india;

import dk.au.cs.p2pn.india.helper.CommunicationConverter;
import dk.au.cs.p2pn.india.reporting.Reporter;
import dk.au.cs.p2pn.india.reporting.ReporterMeasurements;
import dk.au.cs.p2pn.india.search.AdvancedWalkerSearch;
import dk.au.cs.p2pn.india.search.BasicSearch;
import dk.au.cs.p2pn.india.search.FloodSearch;
import dk.au.cs.p2pn.india.search.SearchTypes;
import dk.au.cs.p2pn.india.search.WalkerSearch;
import dk.au.cs.p2pn.india.tasks.SearchPassTask;
import dk.au.cs.p2pn.india.tasks.SearchSuccessTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class CommunicationHandler {
	private Peer peer;
	private PeerApp app;

	private static final Logger logger = LogManager.getLogger(CommunicationHandler.class.getSimpleName());

	public CommunicationHandler(Peer peer, PeerApp app) {
		this.peer = peer;
		this.app = app;
	}

	/**
	 * XML-RPC: Answers a XML-RPCall by a different Peer as part of our Protocol.
	 *
	 * @param IdArg       ID of the Peer that called this function
	 * @param IPArg       IP of the Peer that called this function
	 * @param portArg     Port of the Peer that called this function
	 * @param capacityArg Capacity of the Peer that called this function
	 * @return Vector containing the
	 */
	@SuppressWarnings("rawtypes")
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg) {
		Reporter.addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		this.app.addPeer(new Peer(IdArg, IPArg, portArg, capacityArg));
		return CommunicationConverter.createVector(this.peer);
	}

	/**
	 * XML-RPC: Extension of the regular pong() function. If the flag `neighbornegotiation` is set,
	 * then it signals that the other peer want to be my neighbor. Hence I have to
	 * decide and reply accordingly.
	 *
	 * @param IdArg             ID of the Peer that called this function
	 * @param IPArg             IP of the Peer that called this function
	 * @param portArg           Port of the Peer that called this function
	 * @param capacityArg       Capacity of the Peer that called this function
	 * @param isNeighborRequest Flag whether the requesting Peer wants to be our neighbor
	 * @return Vector containing the
	 */
	@SuppressWarnings("rawtypes")
	public Vector pong(int IdArg, String IPArg, int portArg, int capacityArg, boolean isNeighborRequest) {
		Reporter.addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		// Create Peer object
		Peer inPeer = new Peer(IdArg, IPArg, portArg, capacityArg);
		this.app.addPeer(inPeer);

		if (isNeighborRequest) {
			Reporter.addEvent(ReporterMeasurements.NEIGHBOR_REQUEST_RECEIVED);
			boolean neighborAnswer = responseNegotiate(inPeer);
			if (neighborAnswer) {
				this.app.addNeighbor(inPeer, neighborAnswer);
			}
			logger.debug("Answering neighbor request from {}:{} with {}", IPArg, portArg, neighborAnswer);

			return CommunicationConverter.createVector(this.peer, neighborAnswer);
		}

		return CommunicationConverter.createVector(this.peer);

	}

	public boolean responseNegotiate(Peer inPeer) {
		Reporter.addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		if (this.app.neighborList.size() + this.app.openNeighborRequests.size() >= this.app.getPeer().getCapacity()) {
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
	public Hashtable<String, Vector> getPeerList() {
		Reporter.addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		return CommunicationConverter.createVector(this.app.getPeerList());
	}

	/**
	 * XML-RPC: Answers the call to 'communication.getFile'
	 *
	 * @param fileName Name of the file and directory
	 * @return Bytes array with the content of the File
	 */
	public byte[] getFile(String fileName) {
		String path = "downloads/";
		path += fileName;
		File file = new File(path);
		byte[] fileBytes = null;
		try {
			fileBytes = CommunicationConverter.fileToBytes(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileBytes;
	}

	/**
	 * XML-RPC: Answers the call to `communication.getNeighborList`
	 *
	 * @return List with all Neighbors in vector representation
	 */
	@SuppressWarnings("rawtypes")
	public Vector<Vector> getNeighborList() {
		Reporter.addEvent(ReporterMeasurements.MESSAGE_RECEIVED);

		Hashtable<String, Vector> data = CommunicationConverter.createVector(this.app.getNeighborList());
		return new Vector<Vector>(data.values());
	}

	/**
	 * XML-RPC: Answers the call to `communication.respondSearch`, it will return immediately. If it has the file,
	 * it will start a new thread to give a successful result to the caller before returning. Otherwise
	 * if the ttl is positive, then it passes the search to all the peers in its peer list.
	 */
	@SuppressWarnings("rawtypes")
	public Vector respondSearch(Vector<Object> origin, String fileName, int ttl, String ident, int type) {
		logger.info("Inside respondSearch");

		BasicSearch search;
		Peer peer = CommunicationConverter.createPeer(origin);

		if (type == SearchTypes.FLOOD_SEARCH.getValue()) {
			search = new FloodSearch(ident, fileName, ttl, peer);
		} else if (type == SearchTypes.K_WALKER_SEARCH.getValue()) {
			search = new WalkerSearch(ident, fileName, ttl, peer);
		} else if (type == SearchTypes.AK_WALKER_SEARCH.getValue()) {
			search = new AdvancedWalkerSearch(ident, fileName, ttl, peer);
		} else {
			return new Vector();
		}

		// Case 1: Search
		if (!shouldAnswerSearch(search)) {
			return new Vector();
		}

		// Send success
		if (this.app.fileList.containsKey(fileName)) {
			// but only if i didn't yet.
			if(!this.app.getSearchList().containsKey(search.getId())) {
				logger.info("Inside respondSearch, file matched, starting a new success thread");
				//TODO need to update the weight along the whole path
				search.setSuccess(this.app.getPeer());
				Thread success = new Thread(new SearchSuccessTask(search, this.app));
				success.run();
			}
			return new Vector();
		}

		// Pass Search
		logger.info("Inside respond Search, file not matched, calling passSearch");

		Thread pass = new Thread(new SearchPassTask(this.app, search));
		pass.start();

		return new Vector();
	}

	private boolean shouldAnswerSearch(BasicSearch search) {
		if (search == null){
			return  false;
		}
		if (search.getTtl() <= 0) {
			return false;
		}

		// Process all search types if thy haven't been passed yet.
		if (!this.app.searchList.containsKey(search.getId())){
			return true;
		}

		// Process Walker search only if i have more neighbors left
		// then I have already contacted for this.
		if (search.getType() == SearchTypes.K_WALKER_SEARCH || search.getType() == SearchTypes.AK_WALKER_SEARCH) {
			if (this.app.getSearchList().get(search.getId()).size() < this.app.getNeighborList().size()){
				return true;
			}
		}

		return false;
	}

	/**
	 * XML-RPC: Answers the call to `communication.respondSuccess` when another peer has the file that the
	 * current peer is looking for, this function will return immediately. And it will add the file to the
	 * known file list of the local peer.
	 */
	@SuppressWarnings("rawtypes")
	public Vector respondSuccess(Vector<Object> origin, String fileName, int ttl, String ident, int type, Vector<Object> owner) {
		this.app.knownDataList.put(fileName, CommunicationConverter.createPeer(owner));
		logger.info("The known data list is {}", this.app.knownDataList);
		return new Vector();
	}
}
