package test.java.com.hexcore.vrmeeting_hostserver.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.TCPConnection;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;

/**
 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.connection.TCPConnection}
 * @author Psymj1 (Marcus)
 *
 */
public class TestTCPConnection {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
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
	
	private class MockBrokenSocket extends Socket
	{
		private boolean closeOnInputStreamCalled = false;
		private boolean closeOnOutputStreamCalled = false;
		private boolean closeOnSocketCalled = false;
		
		private InputStream input = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("Fake error");
			}

			@Override
			public void close()
			{
				closeOnInputStreamCalled = true;
			}
			
			@Override
			public int available() throws IOException
			{
				throw new IOException("Fake error");
			}
		};
		
		private OutputStream output = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				throw new IOException("Fake error");
			}
			
			@Override
			public void close()
			{
				closeOnOutputStreamCalled = true;
			}
		};
		
		@Override
		public void close()
		{
			closeOnSocketCalled = true;
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
	}
	
	@Test
	public void readShouldReturnMessageSentToSocketInputStream() throws IOException, ConnectionErrorException
	{
		byte[] testMessage = new Message("TEST",null).getNewTransmittableMessage();
		System.out.println("Message to send " + new String(testMessage));
		ByteBuffer expectedResult = ByteBuffer.allocate("TEST".length() + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length);
		expectedResult.put("TEST".getBytes());
		expectedResult.put(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER);
		
		ByteArrayInputStream input = new ByteArrayInputStream(testMessage);
		//Create an anonymous inner class to override the input stream to be my test data
		MockSocket socket = new MockSocket(input);
		
		TCPConnection connection = new TCPConnection(socket);
		byte[] incomingData = connection.receiveNextPacket();
		assertTrue("The data read from the socket was not the same as the test data placed into the buffer",Arrays.equals(expectedResult.array(),incomingData));
	}
	
	@Test
	public void sendShouldSendBytesToEndPointOfSocket() throws IOException, ConnectionErrorException
	{
		String testMessage = "Test123";
		byte[] testData = testMessage.getBytes();
		MockSocket socket = new MockSocket();
		
		TCPConnection connection = new TCPConnection(socket);
		connection.sendPacket(testData);
		byte[] sentData = socket.getByteArrayOutputStream().toByteArray();
		
		String sentMessage = new String(sentData);
		assertEquals("The message read from the endpoint was not the same as the data sent",testMessage,sentMessage);
	}	
	
	@Test
	public void willReadBlockReturnsFalseIfInputStreamHasData() throws IOException, ConnectionErrorException
	{
		String testMessage = "Test123";
		byte[] testData = testMessage.getBytes();
		ByteArrayInputStream input = new ByteArrayInputStream(testData);
		//Create an anonymous inner class to override the input stream to be my test data
		MockSocket socket = new MockSocket(input);
		
		TCPConnection connection = new TCPConnection(socket);
		assertFalse("willReadBlock returned true despite the inputstream having data",connection.willReceiveBlock());
	}
	
	@Test
	public void willReadBlockReturnsTrueIfInputStreamIsEmpty() throws IOException, ConnectionErrorException
	{
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		MockSocket socket = new MockSocket(input);
		TCPConnection connection = new TCPConnection(socket);
		assertTrue("willReadBlock returned false despite the inputstream not having data",connection.willReceiveBlock());
	}
	
	@Test
	public void socketInputAndOutputStreamClosedIfErrorOccursOnRead() throws SocketException, IOException, ConnectionErrorException
	{
		MockBrokenSocket brokenSocket = new MockBrokenSocket();
		TCPConnection connection = new TCPConnection(brokenSocket){
			@Override
			public String toString() //Fix an error that occurs because the mock socket i create doesn't have a hostname etc for the implementation of toString in TCPConnection
			{
				return "test";
			}
			
			@Override
			public String getName()
			{
				return "test";
			}
		};
		thrown.expect(ConnectionErrorException.class);
		connection.receiveNextPacket();
		
		assertTrue("The socket was not attempted to be closed",brokenSocket.closeOnSocketCalled);
		assertTrue("The input stream was not attempted to be closed",brokenSocket.closeOnInputStreamCalled);
		assertTrue("The output stream was not attempted to be closed",brokenSocket.closeOnOutputStreamCalled);
	}
	
	@Test
	public void socketInputAndOutputStreamClosedIfErrorOccursOnWrite() throws ConnectionErrorException, SocketException, IOException
	{
		MockBrokenSocket brokenSocket = new MockBrokenSocket();
		TCPConnection connection = new TCPConnection(brokenSocket){
			@Override
			public String toString() //Fix an error that occurs because the mock socket i create doesn't have a hostname etc for the implementation of toString in TCPConnection
			{
				return "test";
			}
			
			@Override
			public String getName()
			{
				return "test";
			}
		};
		thrown.expect(ConnectionErrorException.class);
		connection.sendPacket(new byte[1]);
		
		assertTrue("The socket was not attempted to be closed",brokenSocket.closeOnSocketCalled);
		assertTrue("The input stream was not attempted to be closed",brokenSocket.closeOnInputStreamCalled);
		assertTrue("The output stream was not attempted to be closed",brokenSocket.closeOnOutputStreamCalled);
	}
	
	@Test
	public void socketInputAndOutputStreamClosedIfErrorOccursWhenCheckingReadBlock() throws SocketException, IOException, ConnectionErrorException
	{
		MockBrokenSocket brokenSocket = new MockBrokenSocket();
		TCPConnection connection = new TCPConnection(brokenSocket){
			@Override
			public String toString() //Fix an error that occurs because the mock socket i create doesn't have a hostname etc for the implementation of toString in TCPConnection
			{
				return "test";
			}
			
			@Override
			public String getName()
			{
				return "test";
			}
		};
		thrown.expect(ConnectionErrorException.class);
		connection.willReceiveBlock();
		
		assertTrue("The socket was not attempted to be closed",brokenSocket.closeOnSocketCalled);
		assertTrue("The input stream was not attempted to be closed",brokenSocket.closeOnInputStreamCalled);
		assertTrue("The output stream was not attempted to be closed",brokenSocket.closeOnOutputStreamCalled);
	}
	
	@Test
	public void socketInputAndOutputStreamClosedOnClose() throws SocketException, IOException, ConnectionErrorException
	{
		MockBrokenSocket brokenSocket = new MockBrokenSocket();
		TCPConnection connection = new TCPConnection(brokenSocket){
			@Override
			public String toString() //Fix an error that occurs because the mock socket i create doesn't have a hostname etc for the implementation of toString in TCPConnection
			{
				return "test";
			}
			
			@Override
			public String getName()
			{
				return "test";
			}
		};
		connection.close();
		assertTrue("The socket was not attempted to be closed",brokenSocket.closeOnSocketCalled);
		assertTrue("The input stream was not attempted to be closed",brokenSocket.closeOnInputStreamCalled);
		assertTrue("The output stream was not attempted to be closed",brokenSocket.closeOnOutputStreamCalled);
	}
}
