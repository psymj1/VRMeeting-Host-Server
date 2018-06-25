package test.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ServerSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * Tests the {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator} class
 * @author Psymj1 (Marcus)
 *
 */
public class TestMessageGeneratorClass {
	@Test
	/**
	 * Tests that a Message object is created with an empty payload and a signal equal to AUTH
	 */
	public void shouldGenerateAuthenticateMessageWithSetSignalAndEmptyPayload()
	{
		Message m = MessageGenerator.generateAuthenticationRequestMessage();
		String expectedSignal = ServerSignals.AUTH.toString();
		assertEquals("The signal should be '" + expectedSignal + "' but was actually '" + m.getSignal() + "'",m.getSignal(),expectedSignal);
		assertEquals("The payload of the message should be null",m.getPayload(),null);
	}
	
	@Test
	/**
	 * Tests that a Message object is created with an empty payload and a signal equal to VAL
	 */
	public void shouldGenerateValidatedMessageWithSetSignalAndEmptyPayload()
	{
		Message m = MessageGenerator.generateValidatedMessage();
		String expectedSignal = ServerSignals.VAL.toString();
		assertEquals("The signal should be '" + expectedSignal + "' but was actually '" + m.getSignal() + "'",m.getSignal(),expectedSignal);
		assertEquals("The payload of the message should be null",m.getPayload(),null);
	}
	
	@Test
	/**
	 * Tests that a Message object is created with an empty payload and a signal equal to NVAL
	 */
	public void shouldGenerateNotValidatedMessageWithSetSignalAndEmptyPayload()
	{
		Message m = MessageGenerator.generateNotValidatedMessage();
		String expectedSignal = ServerSignals.NVAL.toString();
		assertEquals("The signal should be '" + expectedSignal + "' but was actually '" + m.getSignal() + "'",m.getSignal(),expectedSignal);
		assertEquals("The payload of the message should be null",m.getPayload(),null);
	}
	
	@Test
	/**
	 * Tests that a Message object is created with an empty payload and a signal equal to MEET
	 */
	public void shouldGenerateRequestMeetingIDdMessageWithSetSignalAndEmptyPayload()
	{
		Message m = MessageGenerator.generateRequestMeetingIDMessage();
		String expectedSignal = ServerSignals.MEET.toString();
		assertEquals("The signal should be '" + expectedSignal + "' but was actually '" + m.getSignal() + "'",m.getSignal(),expectedSignal);
		assertEquals("The payload of the message should be null",m.getPayload(),null);
	}
	
	@Test
	public void shoudldGenerateUDMMessageWithAppropriatelyFormattedPayloadFromUser()
	{
		User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
		Message message = MessageGenerator.generateUDMMessage(testValidUser);
		assertEquals("The signal of the message was not UDM",ServerSignals.UDM.toString(),message.getSignal());
		byte[] userDataAsByteArray = testValidUser.convertToJSON().getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		assertTrue("The payload did not match the byte array repreesntation of the JSON equivalent of the test user",Arrays.equals(userDataAsByteArray, message.getPayload()));
	}
	
	@Test
	public void shouldGenerateLEFTMessageWithAppropriatePayloadFromUser()
	{
		User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
		Message message = MessageGenerator.generateLeftMessage(testValidUser.getUserID());
		assertEquals("The signal of the message was not LEFT",ServerSignals.LEFT.toString(),message.getSignal());
		ByteBuffer b = ByteBuffer.wrap(message.getPayload());
		int id = b.getInt();
		assertEquals("The ID stored in the payload is not identical to the user who was used to create the message",testValidUser.getUserID(),id);
	}
}
