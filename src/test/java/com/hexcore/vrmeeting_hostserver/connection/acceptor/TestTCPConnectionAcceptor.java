package test.java.com.hexcore.vrmeeting_hostserver.connection.acceptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.config.TCPAcceptorConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.ConnectionListener;
import main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.TCPConnectionAcceptor;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;

public class TestTCPConnectionAcceptor {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private int connectionTimeout = 10000; //In ms
	
	private class TestConnectionListener implements ConnectionListener
	{
		private boolean notified = false;
		private Connection receivedConnection = null;
		@Override
		public void connectionReceived(Connection connection) {
			notified = true;
			receivedConnection = connection;
		}
		
	}
	
	@Test(timeout = 10000) //10 Second timeout
	public void shouldMoveToStoppedStateWhenStopCalledInAReasonableAmountOfTime() throws StartupException, IllegalComponentStateException
	{
		TCPConnectionAcceptor acceptor = new TCPConnectionAcceptor(TCPAcceptorConfig.PORT);
		acceptor.start();
		
		acceptor.stop();
		while(acceptor.getState() != ServerComponentState.STOPPED);
	}
	
	@Test
	public void whenTCPConnectionMadeTheConnectionIsSetToOpenAndContainsTheConnectionReceieved() throws StartupException, UnknownHostException, IOException, IllegalComponentStateException
	{
		TCPConnectionAcceptor acceptor = new TCPConnectionAcceptor(TCPAcceptorConfig.PORT);
		acceptor.start();

		TestConnectionListener listener = new TestConnectionListener();
		acceptor.addConnectionListener(listener);
		Socket newConnection = new Socket("localhost",TCPAcceptorConfig.PORT);
		
		try
		{
			newConnection.close();
		}catch(IOException e){/*Ignoring the exception thrown as it doesn't matter*/}
		
		acceptor.stop();
		while(acceptor.getState() != ServerComponentState.STOPPED);
		
		assertTrue("The listener was not notified when a new TCP connection was established",listener.notified);
		assertTrue("The connection passed into the receivedConnection method was null",listener.receivedConnection != null);
	}
	
	@Test
	public void portToUseSetByConstructor()
	{
		int testPort = 3;
		TCPConnectionAcceptor acceptor = new TCPConnectionAcceptor(testPort);
		assertEquals("The port used by the acceptor is not equal to that passed into the constructor",testPort,acceptor.getPort());
	}
	
	@Test
	public void exceptionThrownByConstructorIfPortNumberIsNegative()
	{
		int testPort = -1;
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The port cannot be negative");
		TCPConnectionAcceptor acceptor = new TCPConnectionAcceptor(testPort);
	}

	@Test
	public void exceptionThrownByConstructorIfPortNumberOutsideOfValidTCPRange()
	{
		int testPort = 65536;
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The port cannot be greater than 65535");
		TCPConnectionAcceptor acceptor = new TCPConnectionAcceptor(testPort);
	}
}
