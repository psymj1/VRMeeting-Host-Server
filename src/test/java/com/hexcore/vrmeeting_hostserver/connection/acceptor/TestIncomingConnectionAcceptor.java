package test.java.com.hexcore.vrmeeting_hostserver.connection.acceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.ConnectionListener;
import main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.IncomingConnectionAccepter;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;

/**
 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.IncomingConnectionAccepter}
 * @author Psymj1 (Marcus)
 */
public class TestIncomingConnectionAcceptor {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	//Create mock classes to use during testing
	private MockConnectionAcceptor workingMockConnectionAcceptor;
	
	private class MockConnectionAcceptor extends IncomingConnectionAccepter
	{
		@Override
		protected void startUp() throws StartupException {
			
		}

		@Override
		protected void shutdown() {
			
		}
	}
	
	private Connection mockConnection = new Connection() {

		@Override
		protected byte[] receive() throws ConnectionErrorException {
			return null;
		}

		@Override
		protected void send(byte[] packet) throws ConnectionErrorException {
			
		}

		@Override
		protected boolean willReadBlock() throws ConnectionErrorException {
			return false;
		}

		@Override
		protected void onClose() {
			
		}

		@Override
		public String getName() {
			return null;
		}
	};	
	
	private class MockConnectionListener implements ConnectionListener
	{
		private boolean notified = false;
		private Connection lastReceivedConnection = null;
		
		@Override
		public void connectionReceived(Connection connection) {
			notified = true;
			lastReceivedConnection = connection;
		}
	}
	
	@Before
	public void beforeTest()
	{
		workingMockConnectionAcceptor = new MockConnectionAcceptor();
	}
	
	/**
	 * Tests to check that a listener is not notified if it's not subscribed to the acceptor
	 */
	@Test
	public void listenerNotNotifiedIfNotSubscribed()
	{
		MockConnectionListener connectionListener = new MockConnectionListener();
		assertFalse("Somehow the listener has been notified despite not being subscribed to the acceptor",connectionListener.notified);
	}
	
	/**
	 * Tests to check that a listener is not notified if it was previously subscribed and then unsubscribes
	 */
	@Test
	public void listenerNotNotifiedIfUnsubscribesFromAcceptor()
	{
		MockConnectionListener connectionListener = new MockConnectionListener();
		workingMockConnectionAcceptor.addConnectionListener(connectionListener);
		workingMockConnectionAcceptor.removeConnectionListener(connectionListener);
		workingMockConnectionAcceptor.notifyAllListeners(mockConnection);
		assertFalse("The listener was notified despite unsubscribing from the acceptor",connectionListener.notified);
	}
	
	/**
	 * Tests to check that a listener's {@link main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.ConnectionListener#connectionReceived(Connection)} method is called when the acceptor notifies it
	 */
	@Test
	public void connectionReceivedCalledWhenNotifyCalledForSpecificListener()
	{
		MockConnectionListener connectionListener = new MockConnectionListener();
		workingMockConnectionAcceptor.notifyListener(connectionListener,mockConnection);
		assertTrue("connectionReceived was not called on the connection listener despite being passed into notify",connectionListener.notified);
	}
	
	/**
	 * Tests to check that the connection passed into notify is the same connection object that the listener receives from receivedConnection()
	 */
	@Test
	public void connectionPassedToListenerIdenticalToThatPassedIntoNotify()
	{
		MockConnectionListener connectionListener = new MockConnectionListener();
		workingMockConnectionAcceptor.notifyListener(connectionListener,mockConnection);
		assertEquals("The connection passed into notify was not the connection received by the listener",mockConnection,connectionListener.lastReceivedConnection);
	}
	
	/**
	 * Tests that if null is passed in for the listener to notify then an {@link IllegalArgumentException} is thrown
	 */
	@Test
	public void notifyShouldThrowExceptionWhenNullUsedForListener()
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The listener cannot be null");
		workingMockConnectionAcceptor.notifyListener(null, mockConnection);
	}
	
	/**
	 * Tests that if null is used for the connection to notify a listener has come in, then an {@link IllegalArgumentException} is thrown
	 */
	@Test
	public void notifyShouldThrowExceptionWhenNullUsedForConnection()
	{
		MockConnectionListener connectionListener = new MockConnectionListener();
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The connection cannot be null");
		workingMockConnectionAcceptor.notifyListener(connectionListener, null);
	}
	
	/**
	 * Tests that all listeners subscribed to the acceptor are notified when notifyAll is called
	 */
	@Test
	public void notifyAllShouldCallConnectionReceivedOnAllSubscribedListeners()
	{
		int randomNumberOfListeners = (int)Math.floor((Math.random()*10)+1); //Generate int between 1 and 10
		//Generate a list of connection listenrs
		ArrayList<MockConnectionListener> listeners = new ArrayList<MockConnectionListener>();
		for(int i = 0;i < randomNumberOfListeners;i++)
		{
			MockConnectionListener listener = new MockConnectionListener();
			listeners.add(listener);
			workingMockConnectionAcceptor.addConnectionListener(listener);
		}

		workingMockConnectionAcceptor.notifyAllListeners(mockConnection);
		
		//Check that every listener was notified
		for(int i = 0;i < listeners.size();i++)
		{
			assertTrue("Out of " + listeners.size() + " listeners, only up to listener " + i + " was notified",listeners.get(i).notified);
		}
	}
}
