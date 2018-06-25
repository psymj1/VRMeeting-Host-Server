package main.java.com.hexcore.vrmeeting_hostserver.connection;

import java.util.concurrent.TimeoutException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException;

/**
 * Wraps a {@link Connection} to read formatted byte arrays from its input 
 * @author Psymj1 (Marcus)
 *
 */
public class MessageReader {
	
	private int timeout = 0;
	private Connection connection;
	
	/**
	 * 
	 * @param c The connection to read from
	 */
	public MessageReader(Connection c)
	{	
		connection = c;
	}
	
	/**
	 * If set then {@link #readNextMessage()} will throw a {@link TimeoutException} after {@link #timeout} ms
	 * @param timeout The time before a timeout occurs in ms
	 */
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}
	
	/**
	 * Attempts to take the next packet that comes into the connection and convert it to a message and return the converted message
	 * @return The next message that comes into the connection
	 * @throws TimeoutException If {@link #setTimeout(int)} has been called and set to be greater than 0
	 * @throws InvalidMessageException If the next packet to come into the connection is not a valid message
	 * @throws ConnectionErrorException If there is an error reading the next packet from the connection
	 */
	public Message readNextMessage() throws TimeoutException, InvalidMessageException,ConnectionErrorException
	{
		if(!connection.isOpen())
		{
			throw new ConnectionErrorException("Connection " + connection.getName() + " cannot be read from as it is no longer open");
		}
		
		long waitStart = System.currentTimeMillis();
		
		while(willReadBlock())
		{
			if(timeout > 0)
			{
				if(System.currentTimeMillis() >= waitStart + timeout)
				{
					throw new TimeoutException("Reader timed out after no packets were received after " + timeout + "ms of waiting");
				}
			}
		}
		
		byte[] packet = connection.receiveNextPacket();
		return MessageParser.parseMessage(packet);
	}
	
	/**
	 * Checks whether there is a packet waiting for the reader in the wrapped connection
	 * @throws ConnectionErrorException If the connection is no longer open
	 * @return True if calling read on the connection will not block, False if it will block
	 */
	public boolean willReadBlock() throws ConnectionErrorException
	{
		if(!connection.isOpen())
		{
			throw new ConnectionErrorException("Connection " + connection.getName() + " cannot be read from anymore as it is no longer open");
		}
		
		return connection.willReceiveBlock();
	}
}
