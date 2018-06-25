package test.java.com.hexcore.vrmeeting_hostserver.connection;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ConnectionState;
import main.java.com.hexcore.vrmeeting_hostserver.connection.IncomingMessageBroadcaster;
import main.java.com.hexcore.vrmeeting_hostserver.connection.MessageBroadcastSubscriber;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

public class TestIncomingMessageBroadcaster {
	
	@Test(timeout = 5000)
	public void messageReceivedByConnectionIsReceivedByAllSubscribers() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		mockConnection.addResponse(0,new Message("END",null).getTransmittableMessage()); //Add a new message to the scripted incoming messages of the connection
		
		class BroadcastSubscriber implements MessageBroadcastSubscriber
		{
			private Message messageReceived = null;
			
			@Override
			public void onReceiveBroadcast(Connection c,Message m) {
				messageReceived = m;
			}
			
			public Message getMessageReceived()
			{
				return messageReceived;
			}
		}
		
		IncomingMessageBroadcaster broadcaster = new IncomingMessageBroadcaster(mockConnection);
		
		int randomNumberOfSubscribers = (int)Math.floor((Math.random()*10)) + 1;
		BroadcastSubscriber[] subscribers = new BroadcastSubscriber[randomNumberOfSubscribers];
		
		for(int i = 0;i < randomNumberOfSubscribers;i++)
		{
			subscribers[i] = new BroadcastSubscriber();
			broadcaster.addSubscriber(subscribers[i]);
		}
		
		broadcaster.start();
	
		for(int i = 0;i < randomNumberOfSubscribers;i++)
		{
			while(subscribers[i].getMessageReceived() == null)
			{//Give the broadcaster thread a chance to run and call the methods on each subscriber
				try{
					Thread.sleep(10);
				}catch(InterruptedException z)
				{
					
				}
			}
			assertTrue("Subscriber " + i + " was not given the message received by the connection",subscribers[i].getMessageReceived() != null);
			assertTrue("Subscriber " + i + " was not given the message received by the connection",subscribers[i].getMessageReceived().getSignal().equals("END"));
		}
	}
	
	@Test(timeout = 3000)
	public void broadcasterThreadStopsWhenStopIsCalled() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		IncomingMessageBroadcaster broadcaster = new IncomingMessageBroadcaster(mockConnection);
		broadcaster.start();
		broadcaster.stop();
		while(broadcaster.getState() != ServerComponentState.STOPPED);
	}
	
	@Test(timeout = 3000)
	public void broadcasterThreadStopsWhenConnectionEntersClosedState() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		IncomingMessageBroadcaster broadcaster = new IncomingMessageBroadcaster(mockConnection);
		broadcaster.start();
		mockConnection.setState(ConnectionState.CLOSED);
		while(broadcaster.getState() != ServerComponentState.STOPPED);
	}
	
	@Test(timeout = 3000)
	public void broadcasterThreadStopsWhenConnectionEntersErrorState() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		IncomingMessageBroadcaster broadcaster = new IncomingMessageBroadcaster(mockConnection);
		broadcaster.start();
		mockConnection.setState(ConnectionState.ERROR);
		while(broadcaster.getState() != ServerComponentState.STOPPED);
	}
}
