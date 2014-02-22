package dk.au.cs.p2pn.india.communication;

import dk.au.cs.p2pn.india.reporting.Reporter;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;


public class ClientRequestFactory {

	private static Reporter reporter;

	public static void setReporter(Reporter reporter1){
		reporter = reporter1;
	}

	public static XmlRpcClient getClient(String url) throws IOException, XmlRpcException{
		return new ClientRequest(url);
	}
}
