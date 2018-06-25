package main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor;

import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;

/**
 * A class implementing this interface can be subscribed to a ConnectionReceiver to be notified when a connection is received 
 * @author Psymj1 (Marcus)
 */
public interface ConnectionListener {
	/**
	 * Called when a connection is received
	 * @param connection The connection that has been established	
	 */
	public void connectionReceived(Connection connection);
}
