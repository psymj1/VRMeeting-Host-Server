package test.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException;
import test.java.com.hexcore.vrmeeting_hostserver.TestingUtilities;

/**
 * Tests the {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser} Class
 * {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message} Can be used to generate the Transmittable Message as the tests for the class prove it adheres to the Protocol
 * @author Psymj1 (Marcus)
 *
 */
public class TestMessageParserClass {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	//Success Tests
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to check that it successfully parses a valid TOKE Message, preserving the signal type and payload contents
	 * @throws InvalidMessageException If there is an error parsing the valid TOKE message
	 */
	@Test
	public void parseMessageShouldSuccessfullyParseValidSerializedTOKEMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.TOKE.toString();
		int lengthOfTestAuthToken = 16;
		String testAuthToken = TestingUtilities.GenerateRandomString(lengthOfTestAuthToken);
		Message testMessage = new Message(testSignal,testAuthToken.getBytes());
		
		Message result = MessageParser.parseMessage(testMessage.getTransmittableMessage());
		
		assertEquals("A '" + testMessage.toString() + "' type message was not returned, actual returned message was '" + result.getSignal() + "'",result.getSignal(),testMessage.getSignal());
		assertEquals("The authentication token was not preserved during message creation, expected payload to be '" + testAuthToken + "' but was actually '" + new String(result.getPayload()) + "'",testAuthToken,new String(result.getPayload()));
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to check that it successfully parses a valid CHNG Message, preserving the signal type and slide number in the payload
	 * @throws InvalidMessageException If there is an error parsing the valid CHNG message
	 */
	@Test
	public void parseMessageShouldSuccessfullyParseValidSerializedCHNGMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.CHNG.toString();
		short slideNumber = (short)(Math.floor(Math.random()*10)+1); //Generate a number between 1 and 10
		byte[] slideNumberAsByteArray = ByteBuffer.allocate(ProtocolConfig.BYTES_PER_SLIDE_NUMBER).putShort(slideNumber).array();
		Message testCHNGMessage = new Message(testSignal,slideNumberAsByteArray);
		
		Message result = MessageParser.parseMessage(testCHNGMessage.getTransmittableMessage());

		assertEquals("A '" + testCHNGMessage.getSignal() + "' type message was not returned, actual returned message was '" + result.getSignal() + "'",result.getSignal(),testCHNGMessage.getSignal());
		short resultSlideNumber = ByteBuffer.wrap(result.getPayload()).getShort();
		assertEquals("Slide number was not preserved during parsing, slide number should have been '" + slideNumber + "' but was actually '" + resultSlideNumber + "'",slideNumber,resultSlideNumber);
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to check that it successfully parses a valid END message preserving the signal
	 * @throws InvalidMessageException If there is an error parsing the valid END message
	 */
	@Test
	public void parseMessageShouldSuccessfullyParseValidSerializedENDMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.END.toString();
		Message goodENDMessage = new Message(testSignal,null);
		Message m = MessageParser.parseMessage(goodENDMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodENDMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodENDMessage.getSignal());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to check that it successfully parses a valid AUDI Message, preserving the signal and the number of and contents of the samples in the payload
	 * @throws InvalidMessageException If there is an error parsing the valid AUDI message
	 */
	@Test
	public void parseMessageShouldSuccessfullyParseValidSerializedAUDIMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.AUDI.toString();
		float[] testSamples = new float[ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE/ProtocolConfig.BYTES_PER_AUDIO_SAMPLE];
		
		//Assign test samples to the array
		for(int i = 0;i < testSamples.length;i++)
		{
			testSamples[i] = i;
		}
		
		byte[] bATestSamples = new byte[testSamples.length*ProtocolConfig.BYTES_PER_AUDIO_SAMPLE];
		for(int i = 0;i < testSamples.length;i++)
		{
			//Convert the current sample to 4 bytes
			byte[] bFloat = ByteBuffer.allocate(Float.BYTES).putFloat(testSamples[i]).array();
			//Copy the 4 bytes into the byte array 
			System.arraycopy(bFloat, 0, bATestSamples, i*Float.BYTES, bFloat.length);
		}
		
		//Message Constructed
		Message testAUDIMessage = new Message(testSignal,bATestSamples);
		
		
		Message result = MessageParser.parseMessage(testAUDIMessage.getTransmittableMessage());
		assertEquals("A '" + testAUDIMessage.getSignal() + "' type message was not returned, actual returned message was '" + result.getSignal() + "'",result.getSignal(),testAUDIMessage.getSignal());
		assertTrue("The payload was modified during parsing",Arrays.equals(result.getPayload(),testAUDIMessage.getPayload()));
	}
	
	//Error Testing
	
	//General Message Errors
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to check that a message without the {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#END_OF_SIGNAL_DELIMITER} will throw an {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException}
	 * @throws InvalidMessageException Due to the AUDI message missing the character that denotes the end of the signal
	 */
	@Test
	public void parseMessageShouldThrowExceptionWithMessageWithoutSignalDelimiter() throws InvalidMessageException
	{
		//Construct a message without the signal delimiter but with a valid signal
		String signal = ClientSignals.AUDI.toString();
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Signal End Delimiter not found");
		Message m = MessageParser.parseMessage(signal.getBytes());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure an {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when an unknown 'signal' is encoded into the serialized message
	 * As a result it also tests for exception handling for attempts to send bad messages to the server
	 * @throws InvalidMessageException As the signal used in the message is not a valid signal that a message can have making the message invalid
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenInvalidSignalUsed() throws InvalidMessageException
	{
		//Construct a message with a random signal according to the VRMeeting Messaging Protocol 0.1
		String testSignal = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SIGNAL_LENGTH);
		Message testMessage = new Message(testSignal,null);
		
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error parsing message: Unknown signal '" + testSignal + "'");
		MessageParser.parseMessage(testMessage.getTransmittableMessage());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure an {@link IllegalArgumentException} is thrown when an empty byte array is passed in
	 * @throws InvalidMessageException As the message passed into the parser is empty
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenEmptyMessageUsed() throws InvalidMessageException
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Error parsing message: The byte array cannot be empty");
		MessageParser.parseMessage(new byte[0]);
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure an {@link IllegalArgumentException} is thrown when null is passed in for <code>message</code>
	 * @throws InvalidMessageException As the message passed into the parser is empty
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenParameterNull() throws InvalidMessageException
	{
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Error parsing message: message cannot be null");
		MessageParser.parseMessage(null);
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when the byte array passed in is larger than the maximum possible message size
	 * @throws InvalidMessageException As the array passed in is larger than the maximum allowed size
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenMessageIsLargerThanMaximumPossibleSize() throws InvalidMessageException
	{
		byte[] messageTooLarge = new byte[ProtocolConfig.MAX_POSSIBLE_SERIALIZED_MESSAGE_LENGTH+1];
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error parsing message: The maximum possible message size defined by the VRMeeting Messaging Protocol is " + ProtocolConfig.MAX_POSSIBLE_SERIALIZED_MESSAGE_LENGTH + " but the message supplied is " + messageTooLarge.length + " bytes long");
		MessageParser.parseMessage(messageTooLarge);
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid signal is used but the characters are encoded in lower case
	 * @throws InvalidMessageException As the signal in the message does not technically match a valid signal since it is in lower case
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenValidSignalIsLowerCase() throws InvalidMessageException
	{
		String testSignal = ClientSignals.TOKE.toString().toLowerCase();
		Message testMessage = new Message(testSignal, null);
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error parsing message: Unknown signal '" + testSignal + "'");
		MessageParser.parseMessage(testMessage.getTransmittableMessage());
	}
	
	//TOKE Message Errors
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid TOKE signal is used with an empty authentication token
	 * @throws InvalidMessageException As the TOKE message is invalid without a payload
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenTOKESignalUsedWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.TOKE.toString();
		Message testMessage = new Message(testSignal,null);
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error parsing TOKE message: The payload cannot be empty");
		Message result = MessageParser.parseMessage(testMessage.getTransmittableMessage());
	}
	
	//CHNG Message Errors
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid CHNG signal is used with an empty payload
	 * @throws InvalidMessageException As the CHNG message is invalid without a payload
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenCHNGSignalUsedWithEmptyPayload() throws InvalidMessageException
	{
		String signal = ClientSignals.CHNG.toString();
		Message badCHNGMessage = new Message(signal,null);
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing CHNG Message: The payload cannot be empty");
		Message result = MessageParser.parseMessage(badCHNGMessage.getTransmittableMessage());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid CHNG signal is used with a payload which isn't {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#BYTES_PER_SLIDE_NUMBER} long
	 * Given equivalence to checking less than and greater than the number of required bytes to be valid
	 * @throws InvalidMessageException As the payload for the slide number will be too large to be valid
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenCHNGSignalUsedWithPayloadNotRightLengthTooLarge() throws InvalidMessageException
	{
		String signal = ClientSignals.CHNG.toString();
		short slideNumber = (short)(Math.floor(Math.random()*10)+1); //Generate a number between 1 and 10
		byte[] slideNumberAsByteArray = ByteBuffer.allocate(ProtocolConfig.BYTES_PER_SLIDE_NUMBER).putShort(slideNumber).array();
		
		//Create a payload that is 1 more then the expected length and copy over the valid payload
		byte[] tooLargePayload = new byte[slideNumberAsByteArray.length+1];
		System.arraycopy(slideNumberAsByteArray, 0, tooLargePayload, 0, slideNumberAsByteArray.length);
		Message badCHNGMessage = new Message(signal,tooLargePayload);
		
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing CHNG Message: The payload must be " + ProtocolConfig.BYTES_PER_SLIDE_NUMBER + " bytes long but was " + tooLargePayload.length + " bytes long");
		Message result = MessageParser.parseMessage(badCHNGMessage.getTransmittableMessage());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid CHNG signal is used with a payload which isn't {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#BYTES_PER_SLIDE_NUMBER} long
	 * Given equivalence to checking less than and greater than the number of required bytes to be valid
	 * @throws InvalidMessageException As the payload used will be too short to represent the short int
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenCHNGSignalUsedWithPayloadNotRightLengthTooSmall() throws InvalidMessageException
	{
		String signal = ClientSignals.CHNG.toString();
		short slideNumber = (short)(Math.floor(Math.random()*10)+1); //Generate a number between 1 and 10
		byte[] slideNumberAsByteArray = ByteBuffer.allocate(ProtocolConfig.BYTES_PER_SLIDE_NUMBER).putShort(slideNumber).array();
		
		//Create payload that is 1 too small
		byte[] tooSmallPayload = new byte[ProtocolConfig.BYTES_PER_SLIDE_NUMBER-1]; 
		Message badCHNGMessage = new Message(signal,tooSmallPayload);
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing CHNG Message: The payload must be " + ProtocolConfig.BYTES_PER_SLIDE_NUMBER + " bytes long but was " + tooSmallPayload.length + " bytes long");
		Message result = MessageParser.parseMessage(badCHNGMessage.getTransmittableMessage());
	}
	
	/**
	 * {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid CHNG signal is used with a slide number that is zero
	 * @throws InvalidMessageException As the CHNG message will be invalid as there will be no slide number 0 in a power point, only referencing from 1
	 */
	@Ignore
	@Test
	public void parseMessageShouldThrowExceptionWhenCHNGSignalUsedWithSlideNumberZero() throws InvalidMessageException
	{
		String testSignal = ClientSignals.CHNG.toString();
		short slideNumber = 0; 
		byte[] slideNumberAsByteArray = ByteBuffer.allocate(ProtocolConfig.BYTES_PER_SLIDE_NUMBER).putShort(slideNumber).array();
		Message testCHNGMessage = new Message(testSignal,slideNumberAsByteArray);
		
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing CHNG Message: The slide number cannot be 0");
		Message result = MessageParser.parseMessage(testCHNGMessage.getTransmittableMessage());
	}
	
	/**
	 * {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when a valid CHNG signal is used with a slide number that is negative
	 * @throws InvalidMessageException As the CHNG message will be invalid because there are no negative slide numbers
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenCHNGSignalUsedWithSlideNumberNegative() throws InvalidMessageException
	{
		String testSignal = ClientSignals.CHNG.toString();
		short slideNumber = -1; 
		byte[] slideNumberAsByteArray = ByteBuffer.allocate(ProtocolConfig.BYTES_PER_SLIDE_NUMBER).putShort(slideNumber).array();
		Message testCHNGMessage = new Message(testSignal,slideNumberAsByteArray);
		
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing CHNG Message: The slide number cannot be negative");
		Message result = MessageParser.parseMessage(testCHNGMessage.getTransmittableMessage());
	}
	
	//AUDI Message Errors
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when an AUDI signal is used but the payload does not have enough bytes to represent a complete set of samples
	 * If the length is not a multiple of {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#BYTES_PER_AUDIO_SAMPLE} Then either the payload was corrupted or the message was formatted incorrectly
	 * @throws InvalidMessageException As the message is invalid because each sample is x bytes long and so packet.length % x must be 0
	 */
	@Test
	@Ignore //This feature has been modified so the test is no longer valid
	public void parseMessageShouldThrowExceptionWhenAUDISignalUsedWithPayloadLengthIsNotMultipleOfAudioSampleSize() throws InvalidMessageException
	{
		String testSignal = ClientSignals.AUDI.toString();
		int badPayloadLength = ((ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE/ProtocolConfig.BYTES_PER_AUDIO_SAMPLE)-1); //Generate a payload which isn't a multiple of the BytesPerAudioSample and will be less than the max message length
		byte[] badPayload = new byte[badPayloadLength];
		Message badAUDIMessage = new Message(testSignal,badPayload);
		
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing AUDI Message: The payload length isn't a multiple of the number of bytes per audio sample. Payload length: " + badPayloadLength + ", bytes per audio samples: " + ProtocolConfig.BYTES_PER_AUDIO_SAMPLE);
		Message m = MessageParser.parseMessage(badAUDIMessage.getTransmittableMessage());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when an AUDI signal is used with an empty payload
	 * @throws InvalidMessageException As an AUDI message is invalid with an empty payload
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenAUDISignalUsedWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.AUDI.toString();
		Message badAUDIMessage = new Message(testSignal,null);
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing AUDI Message: The payload of an AUDI message cannot be empty");
		Message m = MessageParser.parseMessage(badAUDIMessage.getTransmittableMessage());
	}
	
	/**
	 * Tests {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageParser#parseMessage(byte[])} to make sure a {@link main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException} is thrown when an MEET signal is used with an empty payload
	 * @throws InvalidMessageException As a MID Message is invalid with an empty payload
	 */
	@Test
	public void parseMessageShouldThrowExceptionWhenMIDUsedWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.MID.toString();
		Message badMIDMessage = new Message(testSignal,null);
		thrown.expect(InvalidMessageException.class);
		thrown.expectMessage("Error Parsing MID Message: The payload of an MID message cannot be empty");
		Message m = MessageParser.parseMessage(badMIDMessage.getTransmittableMessage());
	}
	
	/**
	 * Test that a MID message parses with no exceptions when the format is correct
	 * @throws InvalidMessageException If the parser fails to parse the valid message
	 */
	@Test
	public void parseMessageShouldSuccessfullyParseValidMIDMessage() throws InvalidMessageException
	{
		String meetingID = "Meeting123";
		String signal = ClientSignals.MID.toString();
		Message goodMIDMessage = new Message(signal,meetingID.getBytes(ProtocolConfig.MEETING_ID_CHARACTER_SET));
		Message result = MessageParser.parseMessage(goodMIDMessage.getTransmittableMessage());
		assertEquals("A '" + goodMIDMessage.getSignal() + "' type message was not returned, actual returned message was '" + result.getSignal() + "'",goodMIDMessage.getSignal(),result.getSignal());
		assertTrue("The payload was modified during parsing",Arrays.equals(result.getPayload(),goodMIDMessage.getPayload()));
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseGAPMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.GAP.toString();
		Message goodGAPMessage = new Message(testSignal,null);
		Message m = MessageParser.parseMessage(goodGAPMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodGAPMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodGAPMessage.getSignal());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseHEREMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.HERE.toString();
		Message goodHEREMessage = new Message(testSignal,null);
		Message m = MessageParser.parseMessage(goodHEREMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodHEREMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodHEREMessage.getSignal());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseGONEMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.GONE.toString();
		Message goodGONEMessage = new Message(testSignal,null);
		Message m = MessageParser.parseMessage(goodGONEMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodGONEMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodGONEMessage.getSignal());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseHRTBMessage() throws InvalidMessageException
	{
		String testSignal = ClientSignals.HRTB.toString();
		Message goodHRTBMessage = new Message(testSignal,null);
		Message m = MessageParser.parseMessage(goodHRTBMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodHRTBMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodHRTBMessage.getSignal());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseGAPMessageWithPayloadReturningMessageWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.GAP.toString();
		String testString = "TestString";
		Message goodGAPMessage = new Message(testSignal,testString.getBytes());
		Message m = MessageParser.parseMessage(goodGAPMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodGAPMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodGAPMessage.getSignal());
		assertTrue("The payload was not empty",null == m.getPayload());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseHEREMessageWithPayloadReturningMessageWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.HERE.toString();
		String testString = "TestString";
		Message goodHEREMessage = new Message(testSignal,testString.getBytes());
		Message m = MessageParser.parseMessage(goodHEREMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodHEREMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodHEREMessage.getSignal());
		assertTrue("The payload was not empty",null == m.getPayload());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseGONEMessageWithPayloadReturningMessageWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.GONE.toString();
		String testString = "TestString";
		Message goodGONEMessage = new Message(testSignal,testString.getBytes());
		Message m = MessageParser.parseMessage(goodGONEMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodGONEMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodGONEMessage.getSignal());
		assertTrue("The payload was not empty",null == m.getPayload());
	}
	
	@Test
	public void parseMessageShouldSuccessfullyParseHRTBMessageWithPayloadReturningMessageWithEmptyPayload() throws InvalidMessageException
	{
		String testSignal = ClientSignals.HRTB.toString();
		String testString = "TestString";
		Message goodHRTBMessage = new Message(testSignal,testString.getBytes());
		Message m = MessageParser.parseMessage(goodHRTBMessage.getTransmittableMessage());
		assertEquals("The signal was not preserved after parsing the message, Expected '" + goodHRTBMessage.getSignal() + "' but was actually '" + m.getSignal() + "'",m.getSignal(),goodHRTBMessage.getSignal());
		assertTrue("The payload was not empty",null == m.getPayload());
	}
}
