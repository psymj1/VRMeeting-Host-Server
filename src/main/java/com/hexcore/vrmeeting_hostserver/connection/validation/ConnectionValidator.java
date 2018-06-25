/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection.validation;

import java.util.concurrent.TimeoutException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MeetingIDMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.config.ValidatorConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.MessageReader;
import main.java.com.hexcore.vrmeeting_hostserver.connection.MessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.dataserver.DataServerConnector;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMeetingIDException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidTokenException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ServerUnavailableException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ValidationFailedException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * A Utility class which provides the logic to interrogate a Connection and query a data server to check the validity of the data sent by the connection, and retrieve the relevant information to create a new client
 * @author Marcus
 *
 */
public class ConnectionValidator{
//	public static final Logger LOGGER = Logger.getLogger(ConnectionValidator.class);
	
	private ConnectionValidator()
	{
		
	}
	
	/**
	 * Uses the validation server passed in to interrogate the connection to check if it's a valid VRMeeting Client
	 * @param clientConnection The connection to interrogate
	 * @param dataServer The server to use to query for information regarding the validity of the connection
	 * @throws ValidationFailedException Thrown if there is an error during validation with the reason why the validation failed
	 * @return The Connection and its related user information as a {@link MeetingClient} object
	 */
	public static MeetingClient validateConnection(Connection clientConnection, DataServerConnector dataServer) throws ValidationFailedException
	{
		checkConnectionState(clientConnection, "Before Communication Started");
		if(!dataServer.isAvailable())
		{
			throw new ValidationFailedException("Validation Failed at (Before Communication Started): The data server " + dataServer + " is unavailable");
		}
		
		askClientForToken(clientConnection);
		String authenticationToken = getTokenFromClient(clientConnection);
		User user = validateUserToken(dataServer, authenticationToken);
		askClientForMeetingID(clientConnection);
		String meetingID = getMeetingIDFromClient(clientConnection);
		Meeting meeting = validateMeetingID(dataServer, meetingID);
		
		user.setPresenting(meeting.getPresenterID() == user.getUserID());
		user.setMeetingCode(meetingID);
		
		return new MeetingClient(clientConnection,user);
	}
	
	public static void askClientForToken(Connection client) throws ValidationFailedException
	{
		String currentStage = "Asking for Authentication Token";
		
		checkConnectionState(client, currentStage);
		MessageWriter writer = new MessageWriter(client);
		try {
			writer.sendMessage(MessageGenerator.generateAuthenticationRequestMessage());
		} catch (ConnectionErrorException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client +  " has encountered an error. See the logs for more details");
		}
	}
	
	public static String getTokenFromClient(Connection client) throws ValidationFailedException
	{
		String currentStage = "Receiving Authentication Token";
		checkConnectionState(client, currentStage);
		MessageReader reader = new MessageReader(client);
		reader.setTimeout(ValidatorConfig.RESPONSE_TIMEOUT);
		try {
			Message input = reader.readNextMessage();
			if(input.getClass() == AuthTokenMessage.class)
			{
				AuthTokenMessage tokenMsg = (AuthTokenMessage)input;
				return tokenMsg.getAuthenticationToken();
			}else
			{
				throw new InvalidMessageException("Validation Failed at (" + currentStage +": The connection to " + client.getName() + " sent an invalid message");
			}
		} catch (TimeoutException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client.getName() + " failed to respond in time");
		} catch (InvalidMessageException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client.getName() + " sent an invalid message");
		} catch (ConnectionErrorException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client +  " has encountered an error. See the logs for more details");
		}
	}
	
	public static User validateUserToken(DataServerConnector dataServer,String userToken) throws ValidationFailedException
	{
		String currentStage = "Validating Authentication Token";
		
		if(!dataServer.isAvailable())
		{
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The data server " + dataServer + " is unavailable");
		}
		
		try {
			return dataServer.retrieveUserData(userToken);
		} catch (InvalidTokenException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): Invalid Authentication Token");
		} catch (ServerUnavailableException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The data server " + dataServer + " is unavailable");
		}
	}
	
	public static void askClientForMeetingID(Connection client) throws ValidationFailedException
	{
		String currentStage = "Asking for Meeting ID";
		checkConnectionState(client, currentStage);
		MessageWriter sender = new MessageWriter(client);
		try {
			sender.sendMessage(MessageGenerator.generateRequestMeetingIDMessage());
		} catch (ConnectionErrorException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client +  " has encountered an error. See the logs for more details");
		}
	}
	
	public static String getMeetingIDFromClient(Connection client) throws ValidationFailedException
	{
		String currentStage = "Receiving Meeting ID";
		checkConnectionState(client, currentStage);
		MessageReader reader = new MessageReader(client);
		reader.setTimeout(ValidatorConfig.RESPONSE_TIMEOUT);
		
		try {
			Message input = reader.readNextMessage();
			if(input.getClass() == MeetingIDMessage.class)
			{
				MeetingIDMessage midMsg = (MeetingIDMessage)input;
				return midMsg.getMeetingID();
			}else
			{
				throw new InvalidMessageException("Validation Failed at (" + currentStage +": The connection to " + client.getName() + " sent an invalid message");
			}
		} catch (TimeoutException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client.getName() + " failed to respond in time");
		} catch (InvalidMessageException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client.getName() + " sent an invalid message");
		} catch (ConnectionErrorException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client +  " has encountered an error. See the logs for more details");
		}
	}
	
	public static Meeting validateMeetingID(DataServerConnector dataServer,String meetingID) throws ValidationFailedException
	{
		String currentStage = "Validating Meeting ID";
		if(!dataServer.isAvailable())
		{
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The data server " + dataServer + " is unavailable");
		}
		try {
			return dataServer.retreiveMeetingData(meetingID);
		} catch (InvalidMeetingIDException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): Invalid Meeting ID");
		} catch (ServerUnavailableException e) {
			throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The data server " + dataServer + " is unavailable");
		}
	}
	
	private static void checkConnectionState(Connection client,String currentStage) throws ValidationFailedException
	{
		if(!client.isOpen())
		{
			switch(client.getState())
			{
			case ERROR:
				throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client +  " has encountered an error. See the logs for more details");
			case CLOSED:
				throw new ValidationFailedException("Validation Failed at (" + currentStage + "): The connection to " + client.getName() + " is not open");
			case OPEN:
				throw new ValidationFailedException("Validation Failed at (" + currentStage + "): Impossible State reached, Connection Open inside of a statement only reachable if connection.isOpen() is false");
			}
		}
	}
}
