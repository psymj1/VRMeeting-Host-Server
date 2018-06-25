package test.java.com.hexcore.vrmeeting_hostserver.connection.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MeetingIDMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.config.ValidatorConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ValidConnectionOutput;
import main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ValidationThread;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ValidationFailedException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;
import test.java.com.hexcore.vrmeeting_hostserver.mock.MockDataServer;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

public class TestValidationThread {
	
	//These tests rely on the relationship that validate will call the methods of the ConnectionValidator which is tested and known to either timeout, throw an error or finish
	//In all cases a ValidationFailedException is thrown. So if I test that if the validate() method of the ValidationThread throws an exception, then the component moves to stopped,
	//Then it's a transitive relationship implying that the validatorthread will move to stopped
	
	private String testAuthenticationToken = "Token123";
	private MockDataServer defaultTestServer;
	private ScriptedMockConnection defaultTestConnection;
	private User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
	private AuthTokenMessage validTOKEMessage = new AuthTokenMessage(testAuthenticationToken.getBytes(ProtocolConfig.AUTHENTICATION_TOKEN_CHARACTER_SET));
	private Meeting testMeeting = new Meeting("Meeting123");
	private MeetingIDMessage validMIDMessage = new MeetingIDMessage(testMeeting.getMeetingCode().getBytes(ProtocolConfig.MEETING_ID_CHARACTER_SET));
	private MeetingClient clientGiven = null;
	private ValidConnectionOutput testOutput = new ValidConnectionOutput() {
		@Override
		public void connectionValidationOutput(MeetingClient client) {
			clientGiven = client;
		}
	};
	
	@Before
	public void before()
	{
		testValidUser.setPresenting(testMeeting.getPresenterID() == testValidUser.getUserID());
		testValidUser.setMeetingCode(testMeeting.getMeetingCode());
		defaultTestServer = new MockDataServer(testValidUser, testMeeting);
		defaultTestConnection = new ScriptedMockConnection();
	}
	
	@Test(timeout = ValidatorConfig.RESPONSE_TIMEOUT * 2) //Since I know from testing the validator that it will at most run for as long as 2 times the response time since it has to wait on the client response twice
	public void validationThreadMovesToStoppedStateIfValidateThrowsException() throws StartupException, IllegalComponentStateException
	{
		ValidationThread validateThread = new ValidationThread(null,defaultTestConnection, defaultTestServer,null){
			@Override
			public MeetingClient validate() throws ValidationFailedException
			{
				throw new ValidationFailedException("This is a test");
			}
		};
		
		validateThread.start();
		while(validateThread.getState() != ServerComponentState.STOPPED);
	}
	
	@Test(timeout = ValidatorConfig.RESPONSE_TIMEOUT * 2)
	public void validationThreadMovesToStoppedStateIfValidateDoesNotThrowException() throws StartupException, IllegalComponentStateException
	{
		ValidationThread validateThread = new ValidationThread(null,defaultTestConnection, defaultTestServer,testOutput){
			@Override
			public MeetingClient validate() throws ValidationFailedException
			{
				return clientGiven;
				
			}
		};
		
		validateThread.start();
		while(validateThread.getState() != ServerComponentState.STOPPED);
	}
	

	
	@Test(timeout = ValidatorConfig.RESPONSE_TIMEOUT * 2)
	public void validationThreadShouldCallValidConnectionOutputIfConnectionValidationSuccessful() throws StartupException, IllegalComponentStateException
	{
		defaultTestServer.shouldMeetingIDBeValid(true);
		defaultTestServer.shouldAuthenticationTokenBeValid(true);
		defaultTestConnection.addResponse(0, validTOKEMessage.getTransmittableMessage());
		defaultTestConnection.addResponse(1, validMIDMessage.getTransmittableMessage());
		ValidationThread validationThread = new ValidationThread(null,defaultTestConnection, defaultTestServer, testOutput);
		validationThread.start();
		while(validationThread.getState() != ServerComponentState.STOPPED);
		assertTrue("The connection output was not given the valid meeting client created from the valid connection",clientGiven != null);
		assertTrue("The user in the meeting client doesn't match the information retrieved from the data server",testValidUser.equals(clientGiven.getUserInfo()));
		assertTrue("The connection was not preserved when creating the client",defaultTestConnection == clientGiven.getConnection());
	}
	
	@Test(timeout = ValidatorConfig.RESPONSE_TIMEOUT * 2)
	public void shouldSendVALToConnectionIfValid() throws StartupException, IllegalComponentStateException
	{
		defaultTestServer.shouldMeetingIDBeValid(true);
		defaultTestServer.shouldAuthenticationTokenBeValid(true);
		defaultTestConnection.addResponse(0, validTOKEMessage.getNewTransmittableMessage());
		defaultTestConnection.addResponse(1, validMIDMessage.getNewTransmittableMessage());
		ValidationThread validationThread = new ValidationThread(null,defaultTestConnection, defaultTestServer, testOutput);
		validationThread.start();
		while(validationThread.getState() != ServerComponentState.STOPPED);
		assertTrue("The connection output was not given the valid meeting client created from the valid connection",clientGiven != null);
		assertTrue("The user in the meeting client doesn't match the information retrieved from the data server",testValidUser.equals(clientGiven.getUserInfo()));
		assertTrue("The connection was not preserved when creating the client",defaultTestConnection == clientGiven.getConnection());
		
		byte[] valMessage = MessageGenerator.generateValidatedMessage().getNewTransmittableMessage();
		byte[][] packetsSent = defaultTestConnection.getAllSentPackets(); //Should have packets [AUTH,MEET,VAL]

		assertTrue("No packets were sent to the connection",packetsSent.length > 0);
		assertEquals("There are not enough packets for all of the necessary messages in the protocol to have been sent",3,packetsSent.length);
		byte[] messageReceived = packetsSent[2]; //Get the 3rd message
		assertTrue("A VAL message was not sent to the connection despite it providing valid information",Arrays.equals(valMessage,messageReceived));
	}
	
	@Test(timeout = ValidatorConfig.RESPONSE_TIMEOUT * 2)
	public void shouldSendNVALToConnectionIfInvalid() throws StartupException, IllegalComponentStateException
	{
		defaultTestServer.shouldMeetingIDBeValid(false);
		defaultTestServer.shouldAuthenticationTokenBeValid(false); //No responses programmed into defaultTestConnection so it times out
		ValidationThread validationThread = new ValidationThread(null,defaultTestConnection, defaultTestServer, testOutput);
		validationThread.start();
		while(validationThread.getState() != ServerComponentState.STOPPED);
		
		byte[] nvalMessage = MessageGenerator.generateNotValidatedMessage().getTransmittableMessage();
		byte[][] packetsSent = defaultTestConnection.getAllSentPackets(); //Should have packets [AUTH,NVAL]
		assertTrue("No packets were sent to the connection",packetsSent.length > 0);
		assertEquals("There are not enough packets for all of the necessary messages in the protocol to have been sent",2,packetsSent.length);
		byte[] messageReceived = packetsSent[1]; //Get the 2nd message
		assertTrue("An NVAL message was not sent despite the connection being invalid and so failing validation",Arrays.equals(nvalMessage,messageReceived));
	}
}
