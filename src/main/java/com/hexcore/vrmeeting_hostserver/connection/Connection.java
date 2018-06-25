package main.java.com.hexcore.vrmeeting_hostserver.connection;

import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;

/**
 * A class extending this is able to send and receive packets in the form of byte arrays to and from an end point
 * @author Psymj1 (Marcus)
 */
public abstract class Connection {
	private ConnectionState state;
	
	public Connection()
	{
		state = ConnectionState.OPEN;
	}
	
	/**
	 * Get the next packet of data sent to the connection from the end point. Call may block if {@link #willReceiveBlock()} is false
	 * @return Returns the next packet of data in a byte array or Null if there is no data to be read
	 * @throws ConnectionErrorException If there is an error has occurred with the connection such that it cannot perform any further actions
	 */
	public final byte[] receiveNextPacket() throws ConnectionErrorException
	{
		if(getState().equals(ConnectionState.ERROR))
		{
			throw new ConnectionErrorException("An error has occurred in the connection so the next packet cannot be received");
		}
		
		if(getState().equals(ConnectionState.CLOSED))
		{
			throw new ConnectionErrorException("A packet cannot be received from a connection that is closed");
		}
		
		try
		{
			return receive();
		}catch(ConnectionErrorException e)
		{
			setState(ConnectionState.ERROR);
			throw e;
		}
	}
	
	/**
	 * Implemented by a subclass to receive and return the next packet sent to the connection
	 * @return Returns the next packet of data in a byte array format or null if there is no data to be read
	 * @throws ConnectionErrorException If an error occurs while attempting to get the next packet
	 */
	protected abstract byte[] receive() throws ConnectionErrorException;
	
	/**
	 * Send a packet to the end point at the other end of the connection
	 * @param packet The packet to send to the connection endpoint
	 * @throws ConnectionErrorException If there is an error has occurred with the connection such that it cannot perform any further actions
	 */
	public final void sendPacket(byte[] packet) throws ConnectionErrorException
	{
		if(getState().equals(ConnectionState.ERROR))
		{
			throw new ConnectionErrorException("An error has occurred in the connection so the packet cannot be sent");
		}
		
		if(getState().equals(ConnectionState.CLOSED))
		{
			throw new ConnectionErrorException("A packet cannot be sent from a connection that is closed");
		}
		
		if(packet == null)
		{
			throw new IllegalArgumentException("The packet cannot be null");
		}
		if(packet.length == 0)
		{
			throw new IllegalArgumentException("The packet cannot be empty");
		}
		
		try
		{
			send(packet);
		}catch(ConnectionErrorException e)
		{
			setState(ConnectionState.ERROR);
			throw e;
		}
	}
	
	/**
	 * Implemented by a subclass to send a packet to the endpoint of the connection
	 * @param packet The packet to send in byte array form
	 * @throws ConnectionErrorException If an error occurs while attempting to send the packet
	 */
	protected abstract void send(byte[] packet) throws ConnectionErrorException;
	
	/**
	 * Checks whether {@link #receiveNextPacket()} will block, it does not guarantee that the call will not block
	 * @return Returns true if calling {@link #receiveNextPacket()} will block
	 * @throws ConnectionErrorException If there is an error with the connection
	 */
	public final boolean willReceiveBlock() throws ConnectionErrorException
	{
		if(getState().equals(ConnectionState.ERROR))
		{
			throw new ConnectionErrorException("An error has occurred in the connection so no further packets can be received");
		}
		if(getState().equals(ConnectionState.CLOSED))
		{
			throw new ConnectionErrorException("No further packets can be received from a connection that is closed");
		}
		try
		{
			return willReadBlock();
		}catch(ConnectionErrorException c)
		{
			setState(ConnectionState.ERROR);
			throw c;
		}
	}
	
	/**
	 * Implemented by a subclass to provide a response to willReceiveBlock
	 * @return True if calling {@link #receiveNextPacket()} will block
	 * @throws ConnectionErrorException If there is an error with the connection
	 */
	protected abstract boolean willReadBlock() throws ConnectionErrorException;
	
	/**
	 * 
	 * @return The current {@link ConnectionState} of the connection
	 */
	public final ConnectionState getState()
	{
		ConnectionState temp;
		synchronized(state)
		{
			temp = state;
		}
		
		return temp;
	}
	
	public final void setState(ConnectionState state)
	{
		synchronized (this.state) {
			this.state = state;
		}
	}
	
	/**
	 * Closes the connection stopping any further packets from being sent or received. Has no effect if the connection is already closed or in an error state
	 * @throws ConnectionErrorException if the connection is already closed or in an error state
	 */
	public final void close() throws ConnectionErrorException
	{
		if(isOpen())
		{
			setState(ConnectionState.CLOSED);
			onClose();
		}else
		{
			throw new ConnectionErrorException("The connection cannot be closed because it is not open");
		}
	}
	
	/**
	 * 
	 * @return True if the current state of the connection is {@link ConnectionState#OPEN} otherwise any other {@link ConnectionState} will return false
	 */
	public boolean isOpen()
	{
		return getState().equals(ConnectionState.OPEN);
	}
	
	/**
	 * Called when close is called to provide the opportunity for extending classes to implement their own close logic
	 */
	protected abstract void onClose();
	
	public abstract String getName();
}
