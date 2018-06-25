package test.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import test.java.com.hexcore.vrmeeting_hostserver.TestingUtilities;

/**
 * Tests the {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message} Class
 * @author Psymj1 (Marcus)
 *
 */
public class TestMessageClass {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private String validLengthSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
	private byte[] validLengthPayload = new byte[1];
	
	@Test
	/**
	 * Tests to see if IllegalArgumentException is thrown when a signal that is too big is used in the constructor
	 */
	public void constructorShouldNotAcceptSignalTooBig()
	{
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH+1);
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Signal is too long. Expected Signal Length < " + ProtocolConfig.MAX_SIGNAL_LENGTH + ". Actual length was (" + testSignal.length() + ")");
		Message m = new Message(testSignal,validLengthPayload);
	}
	
	@Test
	/**
	 * Tests to see if IllegalArgumentException is thrown when a payload that is too large is used in the constructor
	 */
	public void constructorShouldNotAcceptPayloadTooBig()
	{
		byte[] invalidPayload = new byte[ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE+1];
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Payload is too long. Expected Payload Length < " + ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE + ". Actual length was (" + invalidPayload.length + ")");
		Message m = new Message(validLengthSignal,invalidPayload);
	}
	
	@Test
	/**
	 * Tests to check that the constructor accepts a null payload
	 */
	public void constructorShouldAcceptNullPayload()
	{
		Message m = new Message(validLengthSignal,null);
	}
	
	@Test
	/**
	 * Tests to check the constructor throws an IllegalArgumentException with a null signal
	 */
	public void constructorShouldNotAcceptNullSignal()
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Signal cannot be null");
		Message m = new Message(null,validLengthPayload);
	}
	
	@Test
	/**
	 * Tests to check the constructor throws an IllegalArgumentException with an empty String signal
	 */
	public void constructorShouldNotAcceptEmptySignal()
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Signal cannot be empty");
		Message m = new Message("",validLengthPayload);
	}
	
	@Test
	/**
	 * Tests to check the constructor accepts an empty payload
	 */
	public void constructorShouldAcceptEmptyPayload()
	{
		Message m = new Message(validLengthSignal,new byte[0]);
	}
	
	@Test
	/**
	 * Tests to check the constructor accepts valid parameters
	 */
	public void constructorShouldAcceptValidParamters()
	{
		Message m = new Message(validLengthSignal,validLengthPayload);
	}
	
	@Test
	/**
	 * Tests to check that a message with a valid signal and full payload is converted to an equivalent byte array representation of the original values that adheres to the VRMeeting Messaging Protocol
	 */
	public void getNewTransmittableMessageShouldEncodeSignalAndPayloadIntoByteArray()
	{
		//Generate test signal and payload
		String testString = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE);
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
		
		//Create message and retrieve the transmittable form
		Message m = new Message(testSignal,testString.getBytes());
		byte[] transmittableMessage = m.getNewTransmittableMessage();
		
		//Construct the transmittable message from the test signal and payload based on the VRMeeting Messaging Protocol
		//First construct the signal in byte array form
		byte[] signalAsByteArray = testSignal.getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		
		//Next construct the payload in byte array form
		byte[] bArrayPayload = testString.getBytes();
		
		//Now combine it
		ByteBuffer buffer = ByteBuffer.allocate(signalAsByteArray.length + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length + bArrayPayload.length + ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length);
		buffer.put(signalAsByteArray);
		buffer.put(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER);
		buffer.put(bArrayPayload);
		buffer.put(ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER);
		
		//Now compare this message to that returned from the transmittable message
		assertTrue("The message produced by getTransmittableMessage does not match that generated according to the VRMeeting Messaging Protocol",Arrays.equals(transmittableMessage, buffer.array()));
	}
	
	@Test
	/**
	 * Tests to check that a message with a valid signal and empty payload is converted to a byte array consisting of only the signal
	 */
	public void getTransmittableMessageShouldContainSignalOnlyWithEmptyPayload()
	{
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
		Message m = new Message(testSignal,new byte[0]);
		byte[] transmitMessage = m.getTransmittableMessage();
		//Now Construct the message manually
		byte[] signalAsByteArray = testSignal.getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		ByteBuffer b = ByteBuffer.allocate(signalAsByteArray.length + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length);
		b.put(signalAsByteArray);
		b.put(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER);
		
		assertTrue("The byte array produced does not match the one manually created according to the VRMeeting Messaging Protocol",Arrays.equals(b.array(),transmitMessage));
	}
	
	@Test
	public void getNewTransmittableMessageShouldContainSignalOnlyWithEmptyPayloadAndPayloadSignalEnd()
	{
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
		Message m = new Message(testSignal,new byte[0]);
		byte[] transmitMessage = m.getNewTransmittableMessage();
		//Now Construct the message manually
		byte[] signalAsByteArray = testSignal.getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		ByteBuffer b = ByteBuffer.allocate(signalAsByteArray.length + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length + ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length);
		b.put(signalAsByteArray);
		b.put(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER);
		b.put(ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER);
		
		assertTrue("The byte array produced does not match the one manually created according to the VRMeeting Messaging Protocol",Arrays.equals(b.array(),transmitMessage));
	}
	
	@Test
	/**
	 * Tests to check that a message with a valid signal and null payload is converted to a byte array consisting of only the signal
	 */
	public void getNewTransmittableMessageShouldContainSignalOnlyWithNullPayload()
	{
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
		Message m = new Message(testSignal,null);
		byte[] transmitMessage = m.getNewTransmittableMessage();
		//Now Construct the message manually
		byte[] signalAsByteArray = testSignal.getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		ByteBuffer b = ByteBuffer.allocate(signalAsByteArray.length + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length + ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length);
		b.put(signalAsByteArray);
		b.put(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER);
		b.put(ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER);
		assertTrue("The byte array produced does not match the one manually created according to the VRMeeting Messaging Protocol",Arrays.equals(b.array(),transmitMessage));
	}
	
	
	/**
	 * Tests that a transmittable message contains the {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#END_OF_SIGNAL_DELIMITER}
	 */
	@Test
	public void getTransmittableMessageShouldAmmendTheSignalDelimiterAfterSignal()
	{
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
		Message m = new Message(testSignal,null);
		String messageAsString = new String(m.getTransmittableMessage(),ProtocolConfig.SIGNAL_CHARACTER_SET);
		assertTrue("The generated transmittable message did not contain the end of signal delimiter",messageAsString.contains(ProtocolConfig.END_OF_SIGNAL_DELIMITER));
	}
}
