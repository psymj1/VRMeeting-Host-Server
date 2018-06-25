package main.java.com.hexcore.vrmeeting_hostserver.connection;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;

/**
 * Wraps a {@link Connection} to write formatted byte arrays to its output
 * @author Psymj1 (Marcus)
 *
 */
public class MessageWriter {
	private Connection connection;
	
	/**
	 * 
	 * @param c The connection to write messages to
	 */
	public MessageWriter(Connection c)
	{
		connection = c;
	}
	
	/**
	 * Sends a message to the end point of the connection
	 * @param m The message to send
	 * @throws ConnectionErrorException Thrown if there is an error communicating with the connection
	 */
	public void sendMessage(Message m) throws ConnectionErrorException
	{
		connection.sendPacket(m.getNewTransmittableMessage());
	}
}
