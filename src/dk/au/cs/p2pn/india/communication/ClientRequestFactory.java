package dk.au.cs.p2pn.india.communication;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

/**
 * Static library that creates ClientRequests to be used for communication.
 */
public class ClientRequestFactory {

	/**
	 * Creates a new instance of ClientRequest.
	 * @param url Address of the other client
	 * @return XmlRpcClient object to use for communication
	 * @throws IOException
	 * @throws XmlRpcException
	 */
	public static XmlRpcClient getClient(String url) throws IOException, XmlRpcException{
		return new ClientRequest(url);
	}
}
