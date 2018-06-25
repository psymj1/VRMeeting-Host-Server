/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;

/**
 * An implmentation of the Connection interface which stores a TCP Connection
 * @author Psymj1 (Marcus)
 */
public class TCPConnection extends Connection {
//	public static final Logger LOGGER = Logger.getLogger(TCPConnection.class);
	private Socket connection;
	private BufferedInputStream inputStream;
	private BufferedOutputStream outputStream;
	private ServerLogger logger;
	
	public TCPConnection(Socket connection) throws IOException, SocketException
	{
		this.connection = connection;
		inputStream = new BufferedInputStream(connection.getInputStream());
		outputStream = new BufferedOutputStream(connection.getOutputStream());
		logger = new ServerLogger("TCPConnection");
	}

	@Override
	protected byte[] receive() throws ConnectionErrorException {
		try
		{
			synchronized(inputStream)
			{
				ArrayList<Byte> signal = new ArrayList<Byte>();
	            ArrayList<Byte> payload = new ArrayList<Byte>();
	            

	            //First loop will gather the signal 
	            while(true)
	            {
	            	byte[] nextByte = new byte[1];
	                inputStream.read(nextByte,0,1);
	                signal.add(nextByte[0]);
	                char byteAsCharacter = new String(nextByte).toCharArray()[0];
	                if(byteAsCharacter == ProtocolConfig.END_OF_SIGNAL_DELIMITER.toCharArray()[0])
	                {
	                    break;
	                }
	            }

	            //Second loop will gather the payload
	            while(true)
	             {
	            	byte[] nextByte = new byte[1];
	            	int bytesRead = inputStream.read(nextByte,0,1);
	            	if(bytesRead <= 0)
	            	{
	            		throw new RuntimeException("FAILED");
	            	}
	            	payload.add(nextByte[0]);
	                     if (payload.size() >= ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length)
	                     {
	                         int offsetFromStart = payload.size() - ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length;
	                         ByteBuffer bs = ByteBuffer.allocate(ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length);
	                         for(int i = 0;i < ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length;i++)
	                         {
	                             bs.put(payload.get(offsetFromStart + i));
	                         }
	 
	                         String toCompare = new String(bs.array());
	                         if (toCompare.equals(ProtocolConfig.END_OF_PAYLOAD_DELIMITER))
	                         {
	                             break;
	                         }
	                     }
	             }
	            
	             int positionToStartAt = payload.size() - ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length;
	             for(int i = 0;i< ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length;i++)
	             {
	            	 payload.remove(positionToStartAt);
	             }
	            
	            ByteBuffer buffer = ByteBuffer.allocate(signal.size() + payload.size());
	            byte[] bSignal = new byte[signal.size()];
	            for(int i = 0;i < signal.size();i++)
	            {
	            	bSignal[i] = signal.get(i);
	            }
	            
	            byte[] bPayload = new byte[payload.size()];
	            for(int i = 0;i < payload.size();i++)
	            {
	            	bPayload[i] = payload.get(i);
	            }
	            
	            buffer.put(bSignal);
	            buffer.put(bPayload);
	            return buffer.array();
			}
		}catch(IOException i)
		{
			logger.logError(i.getMessage(),i);
			closeUnderlyingComponents();
			throw new ConnectionErrorException("Error in TCP connection to " + getName() + ", see logs for details.");
		}
	}

	@Override
	protected void send(byte[] packet) throws ConnectionErrorException {
		try
		{
			outputStream.write(packet, 0, packet.length);
			outputStream.flush();
		}catch(IOException i)
		{
			logger.logError(i.getMessage(),i);
			closeUnderlyingComponents();
			throw new ConnectionErrorException("Error in TCP connection to " + getName() + ", see logs for details.");
		}
	}
	
	@Override
	protected boolean willReadBlock() throws ConnectionErrorException {
		try
		{
			boolean willBlock = true;
			synchronized(inputStream)
			{
				willBlock = !(inputStream.available() > 0);
			}
			return willBlock;
		}catch(IOException i)
		{
			logger.logError(i.getMessage(),i);
			closeUnderlyingComponents();
			throw new ConnectionErrorException("Error in TCP connection to " + getName() + ", see logs for details.");
		}
	}
	
	private void closeUnderlyingComponents()
	{
		logger.logInfo("Closing TCPConnection to " + getName());
//		LOGGER.info("Closing TCP Connection to " + toString());
		try {
			connection.close();
		} catch (IOException e) {
			logger.logError("Error closing TCPConnection to " + getName() + e.getMessage(),e);
//			LOGGER.severe("Error closing TCPConnection:" + e.getMessage(),e);
		}
		try {
			inputStream.close();
		} catch (IOException e) {
//			LOGGER.severe("Error closing TCPConnection inputstream:" + e.getMessage(),e);
		}
		try {
			outputStream.close();
		} catch (IOException e) {
//			LOGGER.severe("Error closing TCPConnection outputstream:" + e.getMessage(),e);
		}
//		LOGGER.info("Closed TCP Connection to " + toString());
	}

	@Override
	public void onClose() {
		closeUnderlyingComponents();
	}

	@Override
	public String getName() {
		return connection.getInetAddress().getHostName() + ":" + connection.getPort();
	}
	
	@Override
	public String toString() {
		return connection.getInetAddress().getHostName() + ":" + connection.getPort();
	}

}
