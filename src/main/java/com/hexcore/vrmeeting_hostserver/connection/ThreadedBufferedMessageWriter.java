/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection;

import java.util.ArrayList;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponent;
import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;

/**
 * A threaded class which maintains an internal buffer of messages to be sent. Messages added to the buffer are queued in a FIFO fashion and then sent to the endpoint of the connection
 * The thread will stop if the connection enters either a stopped state or error state
 * The thread will also stop if {@link #stop()} is called
 * Once a message is added to the queue it cannot be removed
 * @author Psymj1 (Marcus)
 *
 */
public class ThreadedBufferedMessageWriter extends ServerComponent {
	
	private Connection connection;
	private boolean keepRunning = true;
	private ArrayList<Message> messageQueue = new ArrayList<Message>();
	private static final int MIN_QUEUE_CHECK_RATE = 10; //In ms, the minimum rate at which the queue will be checked for new messages
	private MessageWriter writer;
	private static ServerLogger logger;
	
	/**
	 * 
	 */
	public ThreadedBufferedMessageWriter(Connection c) {
		connection = c;
		writer = new MessageWriter(connection);
		logger = new ServerLogger(connection.getName() + " Buffered Message Writer");
	}

	/**
	 * @see main.java.com.hexcore.vrmeeting_hostserver.ServerComponent#startUp()
	 */
	@Override
	protected void startUp() throws StartupException {
		new SendingThread(connection.getName() + " Buffered Message Writer").start();
	}

	/**
	 * @see main.java.com.hexcore.vrmeeting_hostserver.ServerComponent#shutdown()
	 */
	@Override
	protected void shutdown() {
		keepRunning = false;
	}
	
	/**
	 * Enqueue a message to the end of the queue of messages to be sent to the endpoint of the connection
	 * @param message The message to send
	 */
	public void enqueueMessage(Message message)
	{
		synchronized(messageQueue)
		{
			messageQueue.add(message);
		}
	}
	
	private class SendingThread extends Thread{
		
		public SendingThread(String name)
		{
			setName(name);
		}
		
		@Override
		public void run()
		{
			while(keepRunning && connection.isOpen())
			{
				if(messageQueue.isEmpty())
				{
					try {
						Thread.sleep(MIN_QUEUE_CHECK_RATE);
					} catch (InterruptedException e) {
						
					}
				}else
				{
					Message m = messageQueue.get(0);
					messageQueue.remove(0);
					try {
						writer.sendMessage(m);
					} catch (ConnectionErrorException e) {
						logger.logError(e.getMessage(),e);
					}
				}
				
			}
			
			setState(ServerComponentState.STOPPED);
		}
	}
}
