package test.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MeetingIDMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.exception.MessageNotEventException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.ChangeSlideEvent;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.GetAllParticipantsEvent;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.HeartbeatEvent;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.MessageEventParser;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.TransmitAudioEvent;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.UserJoinedEvent;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.UserLeftEvent;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

/**
 * All of the tests here pass null as the parameter for the meetingclient as it isn't necessary to have a meeting client to test that the messages parse correctly however this will cause errors during execution of the event
 * @author Psymj1 (Marcus)
 *
 */
public class TestMessageEventParser {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	@Test
	public void parserReturnsChangeSlideEventFromCHNGMessage() throws MessageNotEventException
	{
		//Create a valid CHNG message
		short slideNumber = 1;
		byte[] shortArray = new byte[2];
		ByteBuffer buffer = ByteBuffer.allocate(shortArray.length);
		buffer.putShort(slideNumber);
		Message message = new Message(ClientSignals.CHNG.toString(),buffer.array());
		Event event = MessageEventParser.parseMessageToEvent(null, message);
		assertTrue("The returned event was not a ChangeSlideEvent",event.getClass() == ChangeSlideEvent.class);
	}
	
	@Test
	public void parserReturnsTransmitAudioEventFromAUDIMessage() throws MessageNotEventException
	{
		byte[] audio = new byte[250];
		Message message = new Message(ClientSignals.AUDI.toString(),audio);
		Event event = MessageEventParser.parseMessageToEvent(null, message);
		assertTrue("The returned event was not a TransmitAudioEvent",event.getClass() == TransmitAudioEvent.class);
	}
	
	@Test
	public void parserReturnsUserJoinedEventFromHEREMessage() throws MessageNotEventException
	{
		Message message = new Message(ClientSignals.HERE.toString(),null);
		Event event = MessageEventParser.parseMessageToEvent(null, message);
		assertTrue("The returned event was not a UserJoinedEvent",event.getClass() == UserJoinedEvent.class);
	}
	
	@Test
	public void parserReturnsUserLeftEventFromGONEMessage() throws MessageNotEventException
	{
		Message message = new Message(ClientSignals.GONE.toString(),null);
		Connection connection = new ScriptedMockConnection();
		
		User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
		MeetingClient client = new MeetingClient(connection,testValidUser);
		Event event = MessageEventParser.parseMessageToEvent(client, message);
		
		assertTrue("The returned event was not a UserLeftEvent",event.getClass() == UserLeftEvent.class);
	}
	
	@Test
	public void parserReturnsGetAllParticipantsEventFromGAPMessage() throws MessageNotEventException
	{
		Message message = new Message(ClientSignals.GAP.toString(),null);
		Connection connection = new ScriptedMockConnection();
		
		User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
		MeetingClient client = new MeetingClient(connection,testValidUser);
		
		Event event = MessageEventParser.parseMessageToEvent(client, message);
		assertTrue("The returned event was not a GetAllParticipantsEvent",event.getClass() == GetAllParticipantsEvent.class);
	}
	
	@Test
	public void parserReturnsHeartbeatEventFromHRTBMessage() throws MessageNotEventException
	{
		Message message = new Message(ClientSignals.HRTB.toString(),null);
		Event event = MessageEventParser.parseMessageToEvent(null, message);
		assertTrue("The returned event was not a HeartbeatEvent",event.getClass() == HeartbeatEvent.class);	
	}
	
	//TODO Implement the END message
	
	@Test
	public void parserThrowsExceptionWhenAnyNonEventMessageIsUsed() throws MessageNotEventException
	{
		Message testMessage = MessageGenerator.generateAuthenticationRequestMessage();
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		testMessage = MessageGenerator.generateLeftMessage(1);
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		testMessage = MessageGenerator.generateNotValidatedMessage();
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		testMessage = MessageGenerator.generateRequestMeetingIDMessage();
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
		testMessage = MessageGenerator.generateUDMMessage(testValidUser);
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		testMessage = MessageGenerator.generateValidatedMessage();
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		String testAuthenticationToken = "Token123";
		Meeting testMeeting = new Meeting("Meeting123");
		
		testMessage = new AuthTokenMessage(testAuthenticationToken.getBytes(ProtocolConfig.AUTHENTICATION_TOKEN_CHARACTER_SET));
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
		testMessage = new MeetingIDMessage(testMeeting.getMeetingCode().getBytes(ProtocolConfig.MEETING_ID_CHARACTER_SET));
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, testMessage);
		
	}

	@Test
	public void parserThrowsExceptionWhenNonExistentSignalUsed() throws MessageNotEventException
	{
		Message m = new Message("NOTR",null);
		thrown.expect(MessageNotEventException.class);
		MessageEventParser.parseMessageToEvent(null, m);
	}
}
