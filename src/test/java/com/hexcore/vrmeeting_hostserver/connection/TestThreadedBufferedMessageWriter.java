package test.java.com.hexcore.vrmeeting_hostserver.connection;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.junit.Test;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ConnectionState;
import main.java.com.hexcore.vrmeeting_hostserver.connection.TCPConnection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

public class TestThreadedBufferedMessageWriter {
	
	private class MockSocket extends Socket
	{
		private InputStream input;
		private OutputStream output;
		
		public MockSocket(InputStream i)
		{
			output = new ByteArrayOutputStream();
			input = i;
		}
		
		public MockSocket()
		{
			this(new ByteArrayInputStream(new byte[0]));
		}
		
		@Override
		public InputStream getInputStream()
		{
			return input;
		}
		
		@Override
		public OutputStream getOutputStream()
		{
			return output;
		}
		
		@Override
		public boolean isConnected()
		{
			return true;
		}
		
		@Override
		public boolean isClosed()
		{
			return false;
		}
		
		@Override
		public boolean isBound(){
			return true;
		}
		
		public ByteArrayOutputStream getByteArrayOutputStream()
		{
			return (ByteArrayOutputStream)output;
		}
	}
	
	
	@Test(timeout = 3000)
	public void enqueueMessageCausesMessageToSendToConnectionEndpoint() throws StartupException, ConnectionErrorException, SocketException, IOException, IllegalComponentStateException
	{
		MockSocket socket = new MockSocket();
		TCPConnection connection = new TCPConnection(socket){
			@Override
			public String getName()
			{
				return "Mock TCP Connection";
			}
		};
		
		ThreadedBufferedMessageWriter messageWriter = new ThreadedBufferedMessageWriter(connection);
		
		messageWriter.start();
		Message m = new Message("AUDI",new byte[250]);
		messageWriter.enqueueMessage(m);
		
		byte[] packet;
		do
		{
			packet = socket.getByteArrayOutputStream().toByteArray();
		}while(packet.length == 0);

		assertTrue("The message was not sent to the end point of the connection",Arrays.equals(packet,m.getNewTransmittableMessage()));
	}
	
	@Test(timeout = 3000)
	public void sendingThreadStopsWhenStopCalled() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		ThreadedBufferedMessageWriter messageWriter = new ThreadedBufferedMessageWriter(mockConnection);
		messageWriter.start();
		messageWriter.stop();
		while(messageWriter.getState() != ServerComponentState.STOPPED);
	}
	
	@Test(timeout = 3000)
	public void sendingThreadStopsWhenConnectionMovesToErrorState() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		ThreadedBufferedMessageWriter messageWriter = new ThreadedBufferedMessageWriter(mockConnection);
		messageWriter.start();
		mockConnection.setState(ConnectionState.ERROR);
		while(messageWriter.getState() != ServerComponentState.STOPPED);
	}
	
	@Test(timeout = 3000)
	public void sendingThreadStopsWhenConnectionMovesToClosedState() throws StartupException, IllegalComponentStateException
	{
		ScriptedMockConnection mockConnection = new ScriptedMockConnection();
		ThreadedBufferedMessageWriter messageWriter = new ThreadedBufferedMessageWriter(mockConnection);
		messageWriter.start();
		mockConnection.setState(ConnectionState.CLOSED);
		while(messageWriter.getState() != ServerComponentState.STOPPED);
	}
}
