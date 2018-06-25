/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponent;
import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;

/**
 * A threaded class which broadcasts each message received by a connection to all subscribers continually
 * The thread will stop if the connection enters either a stopped state or error state
 * The thread will also stop if {@link #stop()} is called
 * @author Psymj1 (Marcus)
 *
 */
public class IncomingMessageBroadcaster extends ServerComponent {

	private Connection connection;
	private ArrayList<MessageBroadcastSubscriber> subscribers = new ArrayList<MessageBroadcastSubscriber>();
	private boolean keepRunning = true;
	private static final int MINIMUM_POLL_RATE = 10; //The minimum rate at which the message broadcaster will check the connection for new messages
	private MessageReader reader;
	private static ServerLogger logger;
	
	/**
	 * 
	 */
	public IncomingMessageBroadcaster(Connection c) {
		connection = c;
		reader = new MessageReader(c);
		reader.setTimeout(MINIMUM_POLL_RATE); //So that if for whatever reason a read attempt is made when there is no message then the reader will not block indefinitely
		logger = new ServerLogger(c.getName() + " Message Broadcaster");
	}

	/**
	 * @see main.java.com.hexcore.vrmeeting_hostserver.ServerComponent#startUp()
	 */
	@Override
	protected void startUp() throws StartupException {
		new ReaderThread(connection.getName() + " Message Broadcaster").start();
	}

	/**
	 * @see main.java.com.hexcore.vrmeeting_hostserver.ServerComponent#shutdown()
	 */
	@Override
	protected void shutdown() {
		keepRunning = false;
	}
	
	private class ReaderThread extends Thread{
		
		public ReaderThread(String threadName)
		{
			setName(threadName);
		}
		
		@Override
		public void run()
		{
			while(keepRunning && connection.isOpen())
			{
				try {
					if(!reader.willReadBlock())
					{
						Message nextMessage = reader.readNextMessage();
						broadcastMessageToSubscribers(nextMessage);
					}else
					{
						Thread.sleep(MINIMUM_POLL_RATE);
					}
				} catch (ConnectionErrorException e) {
					logger.logError(e.getMessage(), e);
				} catch (InterruptedException e) {
					//IGNORE since this is used to indicate the thread should stop and is handled by the outer while loop
				} catch (TimeoutException e) {
					//IGNORE as the timeout is just to protect against an indefinite block
				} catch (InvalidMessageException e) {
					logger.logWarning("Invalid message from " + connection.getName() + "||" + e.getMessage());
				}
			}
			setState(ServerComponentState.STOPPED);
		}
		
		private void broadcastMessageToSubscribers(Message m)
		{
			synchronized(subscribers)
			{
				for(MessageBroadcastSubscriber subscriber : subscribers)
				{
					subscriber.onReceiveBroadcast(connection,m);
				}
			}
		}
	}
	
	public void addSubscriber(MessageBroadcastSubscriber subscriber)
	{
		synchronized(subscribers)
		{
			subscribers.add(subscriber);
		}
	}

}
