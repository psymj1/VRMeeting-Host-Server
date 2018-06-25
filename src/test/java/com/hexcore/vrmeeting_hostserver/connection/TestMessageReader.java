package test.java.com.hexcore.vrmeeting_hostserver.connection;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ConnectionState;
import main.java.com.hexcore.vrmeeting_hostserver.connection.MessageReader;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

public class TestMessageReader {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void readMessageThrowsExceptionIfAtStartConnectionClosed() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		connection.setState(ConnectionState.CLOSED);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("Connection " + connection.getName() + " cannot be read from as it is no longer open");
		reader.readNextMessage();
	}
	
	@Test
	public void readMessageThrowsExceptionIfAtStartConnectionErrorState() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		connection.setState(ConnectionState.ERROR);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("Connection " + connection.getName() + " cannot be read from as it is no longer open");
		reader.readNextMessage();
	}
	
	@Test
	public void readMessageThrowsExceptionIfWhileWaitingConnectionCloses() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		Thread setToClosed = new Thread(){
			@Override
			public void run()
			{
				try {
					Thread.sleep(100); //Wait a moment for the main thread to start waiting for a packet
				} catch (InterruptedException e1) {
				
				}
				
				try {
					connection.close();
				} catch (ConnectionErrorException e) {

				}
			}
		};
		
		setToClosed.start();
		thrown.expect(ConnectionErrorException.class);
		reader.readNextMessage();
	}
	
	@Test
	public void readMessageThrowsExceptionIfWhileWaitingConnectionEntersErrorState() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		Thread setToClosed = new Thread(){
			@Override
			public void run()
			{
				try {
					Thread.sleep(100); //Wait a moment for the main thread to start waiting for a packet
				} catch (InterruptedException e1) {
				
				}
					connection.setState(ConnectionState.ERROR);
			}
		};
		
		setToClosed.start();
		thrown.expect(ConnectionErrorException.class);
		reader.readNextMessage();
	}
	
	@Test
	public void willReadBlockThrowsExceptionIfConnectionClosed() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		connection.setState(ConnectionState.CLOSED);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("Connection " + connection.getName() + " cannot be read from anymore as it is no longer open");
		reader.willReadBlock();
	}
	
	@Test
	public void willReadBlockThrowsExceptionIfConnectionInErrorState() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		connection.setState(ConnectionState.ERROR);
		thrown.expect(ConnectionErrorException.class);
		thrown.expectMessage("Connection " + connection.getName() + " cannot be read from anymore as it is no longer open");
		reader.willReadBlock();
	}
	
	@Test
	public void willReadBlockReturnsTrueWhenConnectionReadWillBlock() throws ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		assertTrue("The reader did not return true for willReadBlock despite connection blocking",reader.willReadBlock());
	}
	
	@Test
	public void willReadBlockReturnsFalseWhenConnectionReadWillNotBlock() throws ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		connection.addResponse(0, new byte[1]);
		assertTrue("The reader returned true for willReadBlock despite connection having data",!reader.willReadBlock());
	}
	
	@Test
	public void messageSentFromConnectionIsParsedCorrectlyByReader() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		Message testMessage = new Message(ClientSignals.TOKE.toString(),"TestToken".getBytes());
		connection.addResponse(0,testMessage.getTransmittableMessage());
		Message received = reader.readNextMessage();
		assertTrue("The message received is not the same as that which was sent",Arrays.equals(received.getTransmittableMessage(), testMessage.getTransmittableMessage()));
	}
	
	@Test
	public void shouldThrowExceptionIfBadPacketReceived() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		Message testMessage = new Message(ClientSignals.TOKE.toString(),null);
		connection.addResponse(0,testMessage.getTransmittableMessage());
		thrown.expect(InvalidMessageException.class);
		Message received = reader.readNextMessage();
	}
	
	@Test
	public void shouldThrowExceptionIfWaitingLongerThanTimeoutIfTimeoutSet() throws TimeoutException, InvalidMessageException, ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageReader reader = new MessageReader(connection);
		int timeoutLength = 1000;
		reader.setTimeout(timeoutLength);
		long startTime = System.currentTimeMillis();
		thrown.expect(TimeoutException.class);
		thrown.expectMessage("Reader timed out after no packets were received after " + timeoutLength + "ms of waiting");
		reader.readNextMessage();
		assertTrue("The timeout finished before " + timeoutLength + "ms had passed",System.currentTimeMillis() >= startTime + timeoutLength);
	}
}
