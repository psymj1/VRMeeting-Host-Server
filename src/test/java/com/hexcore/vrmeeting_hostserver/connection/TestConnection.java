package test.java.com.hexcore.vrmeeting_hostserver.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ConnectionState;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;

/**
 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.connection.Connection}
 * @author Psymj1 (Marcus)
 *
 */
public class TestConnection {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private class WorkingConnection extends Connection
	{
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
		
	}
	
	private class BrokenConnection extends Connection
	{		
		@Override
		protected byte[] receive() throws ConnectionErrorException {
			throw new ConnectionErrorException("This is a fake exception");
		}

		@Override
		protected void send(byte[] packet) throws ConnectionErrorException {
			throw new ConnectionErrorException("This is a fake exception");
		}

		@Override
		protected boolean willReadBlock() throws ConnectionErrorException {
			throw new ConnectionErrorException("This is a fake exception");
		}

		@Override
		protected void onClose() {
			
		}

		@Override
		public String getName() {
			return null;
		}
		
	}
	
	/**
	 * Tests that sendPacket should throw an {@link IllegalArgumentException} If the packet passed in is empty (0 length)
	 * @throws ConnectionErrorException As the packet to be sent to the connection is empty
	 */
	@Test
	public void shouldThrowExceptionIfSendPacketPacketEmpty() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The packet cannot be empty");
		connection.sendPacket(new byte[0]);
	}
	
	/**
	 * Tests that sendPacket should throw an {@link IllegalArgumentException} If the packet passed in is null
	 * @throws ConnectionErrorException As the packet to be sent is null
	 */
	@Test
	public void shouldThrowExceptionIfSendPacketPacketNull() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The packet cannot be null");
		connection.sendPacket(null);
	}
	
	/**
	 * Tests that the connection's state is set to ERROR if when you call ReceiveNextPacket, a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException} was thrown
	 * @throws ConnectionErrorException As the connection is disconnected so calling receive will throw an exception
	 */
	@Test
	public void stateShouldBeErrorIfReceiveNextPacketThrowsException() throws ConnectionErrorException
	{
		BrokenConnection connection = new BrokenConnection();
		thrown.expect(ConnectionErrorException.class);
		connection.receiveNextPacket();
		assertEquals("The state was not updated to ERROR when an exception was thrown from calling ReceiveNextPacket",ConnectionState.ERROR,connection.getState());
	}
	
	/**
	 * Tests that the connection's state is set to ERROR if when you call sendPacket, a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException} was thrown
	 * @throws ConnectionErrorException As the connection used will be disconnected so attempting to use it will throw an exception
	 */
	@Test
	public void stateShouldBeErrorIfSendPacketThrowsException() throws ConnectionErrorException
	{
		BrokenConnection connection = new BrokenConnection();
		thrown.expect(ConnectionErrorException.class);
		connection.sendPacket(new byte[1]);
		assertEquals("The state was not updated to ERROR when an exception was thrown from calling ReceiveNextPacket",ConnectionState.ERROR,connection.getState());
	}
	
	/**
	 * Tests that a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException} is thrown if the connection state is ERROR when receiveNextPacket is called
	 * @throws ConnectionErrorException As the connection will be in an error state so it is unuseable so will throw an exception
	 */
	@Test
	public void receiveNextPacketShouldThrowExceptionIfStateError() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.ERROR);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("An error has occurred in the connection so the next packet cannot be received");
		connection.receiveNextPacket();
	}
	
	/**
	 * Tests that a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException} is thrown if the connection state is ERROR when sendPacket is called
	 * @throws ConnectionErrorException As the connection will be in an error state so it is unuseable so will throw an exception
	 */
	@Test
	public void sendPacketShouldThrowExceptionIfStateError() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.ERROR);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("An error has occurred in the connection so the packet cannot be sent");
		connection.sendPacket(new byte[1]);
	}
	
	/**
	 * Tests that when the Connection object is created it is initialised to an OPEN state
	 */
	@Test
	public void connectionInitialisesToOpenState()
	{
		WorkingConnection connection = new WorkingConnection();
		assertEquals("The connection was not initialised to an OPEN state",ConnectionState.OPEN,connection.getState());
	}
	
	/**
	 * Tests that a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException} is thrown if the connection state is CLOSED when receiveNextpacket is called
	 * @throws ConnectionErrorException As the connection will be in a closed state so it is unuseable so will throw an exception
	 */
	@Test
	public void receiveNextPacketShouldThrowExceptionIfClosedState() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.CLOSED);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("A packet cannot be received from a connection that is closed");
		connection.receiveNextPacket();
	}
	
	/**
	 * Tests that a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException} is thrown if the connection state is CLOSED when receiveNextpacket is called
	 * @throws ConnectionErrorException As the connection will be in a closed state so it is unuseable so will throw an exception
	 */
	@Test
	public void sendPacketShouldThrowExceptionIfClosedState() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.CLOSED);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("A packet cannot be sent from a connection that is closed");
		connection.sendPacket(new byte[1]);
	}
	
	/**
	 * Tests the connection to make sure that attempting to check if calling receive will block will throw an exception if the connection is in an error state
	 * @throws ConnectionErrorException As the connection is in an error state so it is not able to receive packets
	 */
	@Test
	public void willReceiveBlockShouldThrowExceptionIfErrorState() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.ERROR);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("An error has occurred in the connection so no further packets can be received");
		connection.willReceiveBlock();
	}
	
	/**
	 * Tests the connection to make sure that attempting to check if calling receive will block will throw an exception if the connection is in an error state
	 * @throws ConnectionErrorException As the connection is in a closed state so will not be able to receive packets
	 */
	@Test
	public void willReceiveBlockShouldThrowExceptionifClosedState() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.CLOSED);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("No further packets can be received from a connection that is closed");
		connection.willReceiveBlock();
	}
	
	/**
	 * Checks that when willReceiveBlock throws an exception that the responsible connection is set to an error state
	 * @throws ConnectionErrorException As the mock connection is written to fail
	 */
	@Test
	public void stateShouldBeErrorIfWillReceiveBlockThrowsError() throws ConnectionErrorException
	{
		BrokenConnection connection = new BrokenConnection();
		thrown.expect(ConnectionErrorException.class);
		connection.willReceiveBlock();
		assertEquals("The state was not updated to ERROR when an exception was thrown from calling willReceiveBlock",ConnectionState.ERROR,connection.getState());
	}
	
	@Test
	public void isOpenShouldReturnTrueIfStateIsOpen()
	{
		WorkingConnection connection = new WorkingConnection();
		assertTrue("The connection was not considered open despite being in the open state",connection.isOpen());
	}
	
	@Test
	public void isOpenShouldReturnFalseIfStateIsClosed()
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.ERROR);
		assertTrue("The connection was considered open despite not being in the error state",connection.isOpen() == false);
	}
	
	@Test
	public void isOpenShouldReturnFalseIfStateIsError()
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.CLOSED);
		assertTrue("The connection was considered open despite being in the closed state",connection.isOpen() == false);
	}
	
	/**
	 * @throws ConnectionErrorException Should not be thrown
	 */
	@Test
	public void closeShouldMoveConnectionToClosedStateIfOpen() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.close();
		assertTrue("The connection did not move to a closed state despite close being called",connection.getState().equals(ConnectionState.CLOSED));
	}
	
	/**
	 * @throws ConnectionErrorException As the connection is already closed so will throw an exception
	 */
	@Test
	public void closeShouldThrowExceptionifConnectionAlreadyClosed() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.close();
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("The connection cannot be closed because it is not open");
		connection.close();
	}
	
	/**
	 * @throws ConnectionErrorException As the connection is an error state and so cannot be closed
	 */
	@Test
	public void closeShouldThrowExceptionifConnectionInErrorState() throws ConnectionErrorException
	{
		WorkingConnection connection = new WorkingConnection();
		connection.setState(ConnectionState.ERROR);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("The connection cannot be closed because it is not open");
		connection.close();
	}
}
