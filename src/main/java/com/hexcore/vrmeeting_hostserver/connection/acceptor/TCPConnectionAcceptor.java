/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.config.TCPAcceptorConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.TCPConnection;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;

/**
 * Listens for incoming TCP connections and notifies listeners when one is received
 * @author Psymj1 (Marcus)
 *
 */
public class TCPConnectionAcceptor extends IncomingConnectionAccepter {

	private int port;
	private ServerSocket server;
	private AcceptConnectionThread acceptThread;
	private int socketTimeout = 1000;
	private ServerLogger logger = new ServerLogger("TCP Connection Acceptor");
	
	private class AcceptConnectionThread extends Thread
	{
		@Override
		public void run() {
			logger.logInfo("Started");
			while(TCPConnectionAcceptor.this.getState() != ServerComponentState.STOPPED)
			{
				try
				{
					Socket nextSocket = server.accept();
					TCPConnection newConnection = new TCPConnection(nextSocket);
					logger.logInfo("New TCP Connection from " + newConnection.getName());
					notifyAllListeners(newConnection);
				}catch(SocketTimeoutException s)
				{
					
				}catch(IOException i)
				{
					logger.logError(i.getMessage(),i);
				}
				
				if(TCPConnectionAcceptor.this.getState().equals(ServerComponentState.STOPPING))
				{
					try
					{
						server.close();
					}catch(IOException j)
					{
						logger.logWarning("Failed to close server socket:" + j.getStackTrace());
					}
					
					setState(ServerComponentState.STOPPED);
				}
			}
			logger.logInfo("Stopped");
		}
	}
	
	/**
	 * Uses the default port number in {@link TCPAcceptorConfig#PORT}
	 */
	public TCPConnectionAcceptor()
	{
		this(TCPAcceptorConfig.PORT);
	}
	
	/**
	 * Sets the port to listen on to the port specified in the constructor
	 * @param port The port to listen on
	 */
	public TCPConnectionAcceptor(int port)
	{
		if(port < 0)
		{
			throw new IllegalArgumentException("The port cannot be negative");
		}
		if(port > TCPAcceptorConfig.MAX_PORT)
		{
			throw new IllegalArgumentException("The port cannot be greater than " + TCPAcceptorConfig.MAX_PORT);
		}
		this.port = port;
	}
	
	@Override
	protected void startUp() throws StartupException {
		logger.logInfo("Starting...");
		try {
			server = new ServerSocket(port);
			server.setSoTimeout(socketTimeout);
		} catch (IOException e) {
			logger.logError(e.getMessage(),e);
			throw new StartupException("Error starting TCPConnectionAcceptor, see logs for details");
		}
		
		acceptThread = new AcceptConnectionThread();
		acceptThread.start();
		
	}

	@Override
	protected void shutdown() {
		logger.logInfo("Stopping...");
	}
	
	public int getPort()
	{
		return port;
	}

}
