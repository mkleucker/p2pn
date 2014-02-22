package dk.au.cs.p2pn.india.communication;


import dk.au.cs.p2pn.india.reporting.Reporter;
import dk.au.cs.p2pn.india.reporting.ReporterMeasurements;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;


public class ClientRequest extends XmlRpcClient {

	public ClientRequest(String address) throws IOException, XmlRpcException {
		super(address);
	}

	/**
	 * Overloaded execute() function to enable
	 * @param method
	 * @param params
	 * @return
	 * @throws XmlRpcException
	 * @throws IOException
	 */
	public Object execute(String method, Vector params)
			throws XmlRpcException, IOException
	{
		Reporter.addEvent(ReporterMeasurements.BYTES_SENT, sizeOf(params));
		Object retVal = super.execute(method, params);
		Reporter.addEvent(ReporterMeasurements.BYTES_RECEIVED,sizeOf(retVal));
		return retVal;
	}

	/**
	 * Returns the byte size of an object.
	 * @param obj Object to measure
	 * @return Size of object in Bytes
	 * @throws IOException
	 */
	private static int sizeOf(Object obj) throws IOException
	{
		ByteArrayOutputStream byteObject = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteObject);
		objectOutputStream.writeObject(obj);
		objectOutputStream.flush();
		objectOutputStream.close();
		byteObject.close();

		return byteObject.toByteArray().length;
	}
}
