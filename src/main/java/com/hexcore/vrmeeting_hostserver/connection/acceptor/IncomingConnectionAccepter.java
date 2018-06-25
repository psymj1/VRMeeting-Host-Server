package main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor;

import java.util.ArrayList;
import java.util.List;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponent;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;

/**
 * A class used to accept new incoming connections. It will notify any registered ConnectionListeners when a new connection is accepted
 * @author Psymj1 (Marcus)
 *
 */
public abstract class IncomingConnectionAccepter extends ServerComponent{
	private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
	
	/**
	 * Add a new listener to the list of {@link main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.ConnectionListener}s that are notified when a connection is accepted
	 * @param listener The listener to add
	 */
	public void addConnectionListener(ConnectionListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Remove a listener from the list of {@link main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.ConnectionListener}s that are notified when a connection is accepted
	 * @param listener The listener to remove
	 */
	public void removeConnectionListener(ConnectionListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * @return Returns the {@link ConnectionListener}s subscribed to the ConnectionAcceptor as an iterator over the stored list
	 */
	public ConnectionListener[] getConnectionListeners()
	{
		return listeners.toArray(new ConnectionListener[0]);
	}
	
	/**
	 * Notifies the passed in ConnectionListener that a connection has been accepted
	 * @param listener The listener to notify
	 * @param connection The connection that has been received
	 */
	public void notifyListener(ConnectionListener listener,Connection connection)
	{
		if(listener == null)
		{
			throw new IllegalArgumentException("The listener cannot be null");
		}
		if(connection == null)
		{
			throw new IllegalArgumentException("The connection cannot be null");
		}
		listener.connectionReceived(connection);
	}
	
	/**
	 * Notifies all ConnectionListeners subscribed to the ConnectionAcceptor that a connection has been received
	 * @param connection The connection that has been received
	 */
	public void notifyAllListeners(Connection connection)
	{
		for(ConnectionListener listener : listeners)
		{
			listener.connectionReceived(connection);
		}
	}
}
