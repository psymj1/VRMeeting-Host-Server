package test.java.com.hexcore.vrmeeting_hostserver.connection.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MeetingIDMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.config.ValidatorConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ConnectionState;
import main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ConnectionValidator;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ValidationFailedException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;
import test.java.com.hexcore.vrmeeting_hostserver.mock.MockDataServer;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;


public class TestConnectionValidator {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private String testAuthenticationToken = "Token123";
	private Meeting testMeeting = new Meeting("Meeting123");
	private AuthTokenMessage validTOKEMessage = new AuthTokenMessage(testAuthenticationToken.getBytes(ProtocolConfig.AUTHENTICATION_TOKEN_CHARACTER_SET));
	private MeetingIDMessage validMIDMessage = new MeetingIDMessage(testMeeting.getMeetingCode().getBytes(ProtocolConfig.MEETING_ID_CHARACTER_SET));
	private User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
	private byte[] invalidPacket = new byte[1];
	private MockDataServer defaultTestServer;
	private ScriptedMockConnection defaultTestConnection;
	
	@Before
	public void before()
	{
		testValidUser.setPresenting(testMeeting.getPresenterID() == testValidUser.getUserID());
		testValidUser.setMeetingCode(testMeeting.getMeetingCode());
		defaultTestServer = new MockDataServer(testValidUser, testMeeting);
		defaultTestConnection = new ScriptedMockConnection();
	}
	
	@Test
	public void validateConnectionShouldThrowExceptionifConnectionNotOpen() throws ValidationFailedException
	{
		defaultTestConnection.setState(ConnectionState.CLOSED);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Before Communication Started): The connection to " + defaultTestConnection.getName() + " is not open");
		ConnectionValidator.validateConnection(defaultTestConnection, defaultTestServer);
		
		defaultTestConnection.setState(ConnectionState.ERROR);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Asking for Authentication Token): The connection to " + defaultTestConnection + " has encountered an error. See the logs for more details");
		ConnectionValidator.validateConnection(defaultTestConnection, defaultTestServer);
	}
	
	@Test
	public void validateConnectionShouldThrowExceptionIfDataServerNotAvailable() throws ValidationFailedException
	{
		defaultTestServer.shouldValidationServerBeAvailable(false);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Before Communication Started): The data server " + defaultTestServer + " is unavailable");
		ConnectionValidator.validateConnection(defaultTestConnection, defaultTestServer);
	}
	
	@Test
	public void askForTokenShouldThrowExceptionIfConnectionNotOpen() throws ValidationFailedException
	{
		defaultTestConnection.setState(ConnectionState.ERROR);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Asking for Authentication Token): The connection to " + defaultTestConnection + " has encountered an error. See the logs for more details");
		ConnectionValidator.askClientForToken(defaultTestConnection);
		
		defaultTestConnection.setState(ConnectionState.CLOSED);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Asking for Authentication Token): The connection to " + defaultTestConnection + " is not open");
		ConnectionValidator.askClientForToken(defaultTestConnection);
	}
	
	@Test
	public void askForTokenShouldSendAnAUTHMessageToConnection() throws ValidationFailedException
	{
		ConnectionValidator.askClientForToken(defaultTestConnection);
		byte[][] sentPackets = defaultTestConnection.getAllSentPackets();
		assertTrue("No messages were sent to the connection",sentPackets.length > 0);
		byte[] sentPacket = sentPackets[0]; //There will be only 1
		byte[] mockAUTHMessage = MessageGenerator.generateAuthenticationRequestMessage().getNewTransmittableMessage();
		assertTrue("The message sent to the connection does not match an AUTH message generated on the server",Arrays.equals(sentPacket, mockAUTHMessage));
	}
	
	@Test
	public void getTokenShouldThrowExceptionIfConnectionNotOpen() throws ValidationFailedException
	{
		defaultTestConnection.setState(ConnectionState.CLOSED);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Authentication Token): The connection to " + defaultTestConnection + " is not open");
		ConnectionValidator.getTokenFromClient(defaultTestConnection);
		
		defaultTestConnection.setState(ConnectionState.ERROR);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Authentication Token): The connection to " + defaultTestConnection + " has encountered an error. See the logs for more details");
		ConnectionValidator.getTokenFromClient(defaultTestConnection);
	}
	
	@Test(timeout = ValidatorConfig.RESPONSE_TIMEOUT*2)
	public void getTokenShouldThrowExceptionIfConnectionResponseTimesout() throws ValidationFailedException
	{
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Authentication Token): The connection to " + defaultTestConnection + " failed to respond in time");
		ConnectionValidator.getTokenFromClient(defaultTestConnection); //Since it's a scripted connection and no responses have been scripted it will return true for will read block
	}
	
	@Test
	public void getTokenShouldThrowExceptionIfMessageFromClientIsInvalid() throws ValidationFailedException
	{
		defaultTestConnection.addResponse(0, invalidPacket); //Random byte array which will be invalid to the server
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Authentication Token): The connection to " + defaultTestConnection + " sent an invalid message");
		ConnectionValidator.getTokenFromClient(defaultTestConnection);
	}
	
	@Test
	public void getTokenShouldReturnTokenSentFromClient() throws ValidationFailedException
	{
		defaultTestConnection.addResponse(0, validTOKEMessage.getTransmittableMessage());
		String receivedToken = ConnectionValidator.getTokenFromClient(defaultTestConnection);
		assertEquals("The token received from the connection does not match that which was sent from the client",validTOKEMessage.getAuthenticationToken(),receivedToken);
	}
	
	@Test
	public void validateUserTokenShouldThrowExceptionIfServerUnavailable() throws ValidationFailedException
	{
		defaultTestServer.shouldValidationServerBeAvailable(false);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Validating Authentication Token): The data server " + defaultTestServer + " is unavailable");
		ConnectionValidator.validateUserToken(defaultTestServer, testAuthenticationToken);
	}
	
	@Test
	public void validateUserTokenShouldThrowExceptionIfTheTokenIsInvalid() throws ValidationFailedException
	{
		defaultTestServer.shouldAuthenticationTokenBeValid(false);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Validating Authentication Token): Invalid Authentication Token");
		ConnectionValidator.validateUserToken(defaultTestServer, testAuthenticationToken);
	}
	
	@Test
	public void askForMeetingIDShouldThrowExceptionIfConnectionNotOpen() throws ValidationFailedException
	{
		defaultTestConnection.setState(ConnectionState.ERROR);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Asking for Meeting ID): The connection to " + defaultTestConnection + " has encountered an error. See the logs for more details");
		ConnectionValidator.askClientForMeetingID(defaultTestConnection);
		
		defaultTestConnection.setState(ConnectionState.CLOSED);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Asking for Meeting ID): The connection to " + defaultTestConnection + " is not open");
		ConnectionValidator.askClientForMeetingID(defaultTestConnection);
	}
	
	@Test
	public void askForMeetingIDShouldSendAMEETMessagetoConnection() throws ValidationFailedException
	{
		ConnectionValidator.askClientForMeetingID(defaultTestConnection);
		byte[][] sentPackets = defaultTestConnection.getAllSentPackets();
		assertTrue("No messages were sent to the connection",sentPackets.length > 0);
		byte[] sentPacket = sentPackets[0]; //There will be only 1
		byte[] mockMEETMessage = MessageGenerator.generateRequestMeetingIDMessage().getNewTransmittableMessage();
		assertTrue("The message sent to the connection does not match a MEET message generated on the server",Arrays.equals(sentPacket, mockMEETMessage));
	}
	
	@Test
	public void getMeetingIDFromClientShouldThrowExceptionIfConnectionNotOpen() throws ValidationFailedException
	{
		defaultTestConnection.setState(ConnectionState.CLOSED);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Meeting ID): The connection to " + defaultTestConnection + " is not open");
		ConnectionValidator.getMeetingIDFromClient(defaultTestConnection);
		
		defaultTestConnection.setState(ConnectionState.ERROR);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Meeting ID): The connection to " + defaultTestConnection + " has encountered an error. See the logs for more details");
		ConnectionValidator.getMeetingIDFromClient(defaultTestConnection);
	}
	
	@Test
	public void getMeetingIDFromClientShouldThrowExceptionIfConnectionDoesNotRespondBeforeTimeout() throws ValidationFailedException
	{
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Meeting ID): The connection to " + defaultTestConnection + " failed to respond in time");
		ConnectionValidator.getMeetingIDFromClient(defaultTestConnection); //Since it's a scripted connection and no responses have been scripted it will return true for will read block
	}
	
	@Test
	public void getMeetingIDFromClientShouldThrowExceptionIfMessageFromClientIsInvalid() throws ValidationFailedException
	{
		defaultTestConnection.addResponse(0, invalidPacket); //Random byte array which will be invalid to the server
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Receiving Meeting ID): The connection to " + defaultTestConnection + " sent an invalid message");
		ConnectionValidator.getMeetingIDFromClient(defaultTestConnection);
	}
	
	@Test
	public void getMeetingIDFromClientShouldReturnMeetingIDSentFromClient() throws ValidationFailedException
	{
		defaultTestConnection.addResponse(0, validMIDMessage.getTransmittableMessage());
		String receivedMID = ConnectionValidator.getMeetingIDFromClient(defaultTestConnection);
		assertEquals("The meeting id received from the connection does not match that which was sent from the client",validMIDMessage.getMeetingID(),receivedMID);
	}
	
	@Test
	public void validateMeetingIDShouldThrowExceptionIfServerUnavailable() throws ValidationFailedException
	{
		defaultTestServer.shouldValidationServerBeAvailable(false);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Validating Meeting ID): The data server " + defaultTestServer + " is unavailable");
		ConnectionValidator.validateMeetingID(defaultTestServer, testAuthenticationToken);
	}
	
	@Test
	public void validateMeetingIDShouldThrowExceptionIfMeetingIDInvalid() throws ValidationFailedException
	{
		defaultTestServer.shouldMeetingIDBeValid(false);
		thrown.expect(ValidationFailedException.class);
		thrown.expectMessage("Validation Failed at (Validating Meeting ID): Invalid Meeting ID");
		ConnectionValidator.validateMeetingID(defaultTestServer, testAuthenticationToken);
	}
	
	@Test
	public void validateConnectionShouldReturnAMeetingClientWithUserDetailsIfTokenAndMeetingIDCorrect() throws ValidationFailedException
	{
		defaultTestServer.shouldMeetingIDBeValid(true);
		defaultTestServer.shouldAuthenticationTokenBeValid(true);
		defaultTestConnection.addResponse(0, validTOKEMessage.getTransmittableMessage());
		defaultTestConnection.addResponse(1, validMIDMessage.getTransmittableMessage());
		MeetingClient client = ConnectionValidator.validateConnection(defaultTestConnection, defaultTestServer);
		User resultantUser = client.getUserInfo();
		assertTrue("The meeting client generated as a result of validating the connection is not identical to the matchin user who's information was used as connection responses",resultantUser.equals(testValidUser));
	}
}