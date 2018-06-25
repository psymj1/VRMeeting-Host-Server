package main.java.com.hexcore.vrmeeting_hostserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import main.java.com.hexcore.vrmeeting_hostserver.config.TCPAcceptorConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.ConnectionListener;
import main.java.com.hexcore.vrmeeting_hostserver.connection.acceptor.TCPConnectionAcceptor;
import main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ValidConnectionOutput;
import main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ValidationThread;
import main.java.com.hexcore.vrmeeting_hostserver.dataserver.WebServerConnector;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.UserExistsException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;


/**
 * The main thread of execution in the host server
 * @author Psymj1 (Marcus)
 *
 */
public class HostServer extends ServerComponent implements ValidConnectionOutput,ConnectionListener{
	//TODO No check in place for if last person leaves meeting then it's removed
	//TODO No check in place on creating audio event if the person creating is a presenter or not
	private static ServerLogger logger = new ServerLogger("Host Server Thread");
	private static WebServerConnector dataServer;
	private ConcurrentHashMap<String, Meeting> meetings = new ConcurrentHashMap<String, Meeting>();
	private static final int SERVER_STOPPED_CHECK_RATE = 10; //In ms, the rate at which the main thread will check to see if the server has stopped
	
	public HostServer()
	{
		
	}
	
	public static void main(String[] args) throws StartupException, IOException, IllegalComponentStateException {
		logger.logInfo("Initialising server...");
		logger.logInfo("Reading property file properties.txt");
		Properties properties = new Properties();
		FileInputStream propsFile = new FileInputStream(new File("properties.txt"));
		properties.load(propsFile);
		
		if(properties.getProperty("ListenOnPort") == null)
		{
			throw new IOException("Could not find property 'ListenOnPort'");
		}else if(properties.getProperty("WebServerURL") == null)
		{
			throw new IOException("Could not find property 'WebServerURL");
		}
		
		BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
		
		ServerLogger tempLogger = new ServerLogger("University of Nottingham (UON) - Team Hexcore");
		tempLogger.logWarning("This server is a prototype and is not suitable for production environments");
		tempLogger.logWarning("This project is subject to license, see the documentation repository for more information");
		boolean bypass = true;
		
		if(properties.getProperty("BypassNotProductionReadyWarning") == null || !properties.getProperty("BypassNotProductionReadyWarning").equals("true"))
		{
			bypass = false;
			tempLogger.logWarning("Bypass for non-production warning not found, requesting manual confirmation");
			do
			{
				tempLogger.logWarning("Please type 'agree' to confirm that you understand the application is not intended for production environments");
			}while(!consoleIn.readLine().toLowerCase().equals("agree"));
			tempLogger.logInfo("Thank you, you can bypass this manual confirmation by adding the following line to the properties file:\n\nBypassNotProductionReadyWarning = true");
		}
		
		if(bypass)
		{
			tempLogger.logWarning("Bypass for non-production warning found in property file. Bypassing manual confirmation");
		}
		
		System.out.println("\n\n");
		
		TCPAcceptorConfig.PORT = Integer.parseInt(properties.getProperty("ListenOnPort"));
		dataServer = new WebServerConnector(properties.getProperty("WebServerURL"));
		logger.logInfo("Attempting to start host server listening on port " + TCPAcceptorConfig.PORT);
		logger.logInfo("Host Server will connect to the web server located at " + properties.getProperty("WebServerURL"));
		
		if(!dataServer.isAvailable())
		{
			logger.logError("Cannot connect to the Web Server at " + properties.getProperty("WebServerURL") + " are you sure it's installed and running? Check the Instructions.md for more details");
			return;
		}
		
		HostServer server = new HostServer();
		
		server.start();
		
		
		boolean run = true;
		while(run)
		{
			logger.logInfo(server.getState().toString());
			try
			{
				String next = consoleIn.readLine();
				if(next.toLowerCase().equals("stop"))
				{
					logger.logInfo("Stopping Host Server...");
					server.stop();
					run = false;
				}
			}catch(IOException i)
			{
				logger.logError(i.getMessage(), i);
			}
		}
		
		while(server.getState() != ServerComponentState.STOPPED) //Wait for the server to stop
		{
			try
			{
				Thread.sleep(SERVER_STOPPED_CHECK_RATE);
			}catch(InterruptedException e)
			{
				//Ignore
			}
		}
		
		logger.logInfo("Host Server stopped");
	}

	@Override
	public void connectionValidationOutput(MeetingClient client) {
		logger.logInfo("New connection successfully validated from " + client.getConnection().getName() + " for user " + client.getUserInfo().getFirstName());
		addClientToMeeting(client); //Add the new client to their respective meeting
	}
	
	/**
	 * Adds the given client to the meeting it was attempting to join. creates a new meeting if the meeting doesn't exit
	 * @param client The client to add to their meeting
	 */
	private void addClientToMeeting(MeetingClient client)
	{
		try
		{
			String meetingID = client.getUserInfo().getMeetingID();
			Meeting meeting;

			if(meetings.containsKey(meetingID))
			{
				//If the meeting exists then get the meeting
				meeting = meetings.get(meetingID);
			}else
			{
				//else create a new meeting, start it and return a reference to it
				logger.logInfo("Meeting doesn't exist, creating meeting " + meetingID);
				meeting = new Meeting(meetingID);
				meetings.put(meetingID, meeting);
				addSubServerComponent(meeting);
				meeting.start();
			}
			
			try {
				logger.logInfo("Adding client " + client.getConnection().getName() + " to meeting " + meetingID);
				meeting.addNewParticipant(client);
			} catch (UserExistsException e) {
				//If the user already exists in the given meeting close the connection to them without warning
				logger.logWarning("User " + client.getUserInfo().getUserID() + " attempted to join meeting " + meetingID + " from " + client.getConnection().getName() + " when they are already connected");
				try {
					client.getConnection().close();
				} catch (ConnectionErrorException e1) {
					logger.logIgnore(e1.getMessage());
				}
			}
		} catch (StartupException | IllegalComponentStateException e) {
			//There are no components in this try catch that can throw a StartupException, perhaps this exception should be removed or moved to a subclass of server component
			logger.logError("EXCEPTION OCCURED THAT SHOULD NOT BE POSSIBLE",e);
		}
	}

	@Override
	public void connectionReceived(Connection connection) {
		//When a new connetion is received startup a validation thread which will check whether the connection is to a valid client 
		ValidationThread thread = new ValidationThread(this,connection, dataServer, this); //Set this object as the output for any valid connections
		addSubServerComponent(thread); //Add the thread as a sub component so it can be shutdonw
		try {
			thread.start();
		} catch (StartupException | IllegalComponentStateException e) {
			logger.logWarning("Failed to start a ValidationThread for connection: " + connection.getName());
		}
	}

	@Override
	protected void startUp() throws StartupException {
		TCPConnectionAcceptor acceptor = new TCPConnectionAcceptor(); //Create the listener thread to receive incoming TCP connections
		addSubServerComponent(acceptor); //Add the listener thread as a sub component so it will stop when this component stops
		acceptor.addConnectionListener(this); //Add the host server as an output for any new incoming connection
		try {
			acceptor.start();
		} catch (IllegalComponentStateException e) {
			throw new StartupException(e.getMessage());
		} //Start the listener
	}

	@Override
	protected void shutdown() {
		setState(ServerComponentState.STOPPED);
	}
	
	/**
	 * 
	 */
	public void removeMeeting(String meetingID)
	{
		Meeting meeting = meetings.remove(meetingID);
		if(meeting != null)
		{
			removeSubServerComponent(meeting);
			logger.logInfo("Removed meeting " + meetingID + " from list");
		}
	}
	
	@Override
	public void removeSubServerComponent(ServerComponent component)
	{
		if(component.getClass() == Meeting.class)
		{
			Meeting meeting = (Meeting)component;
			removeMeeting(meeting.getMeetingCode());
		}
		
		super.removeSubServerComponent(component);
	}

}
	