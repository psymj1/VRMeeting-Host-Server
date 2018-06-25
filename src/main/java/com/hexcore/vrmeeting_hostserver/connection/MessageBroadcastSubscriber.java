/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;

/**
 * A class implementing this interface can subscribe to {@link IncomingMessageBroadcaster}s to receive any new messages that are sent to that connection
 * @author Psymj1 (Marcus)
 *
 */
public interface MessageBroadcastSubscriber {
	/**
	 * Called by the {@link IncomingMessageBroadcaster} the subscriber is subscribed to whenever a new message is received
	 * @param m The message received by the broadcaster
	 * @param origin The connection the message came from
	 */
	public void onReceiveBroadcast(Connection origin,Message m);
}
