package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import java.nio.ByteBuffer;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMessageException;

/**
 * A Utility Class used to parse incoming messages, checking that they adhere to the VRMeeting Messaging Protocol
 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
 * @author Psymj1 (Marcus)
 *
 */
public class MessageParser {

	private MessageParser() {}
	
	/**
	 * Parses the raw 'message' passed into the function according to the VRMeeting Messaging Protocol
	 * @param message The message to validate
	 * @return Returns a {@link Message} object which represents the parsed raw message
	 * @throws InvalidMessageException Thrown if the message is invalid
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static Message parseMessage(byte[] message) throws InvalidMessageException
	{
		if(message == null)
		{
			throw new IllegalArgumentException("Error parsing message: message cannot be null");
		}
		
		if(!(message.length > 0))
		{
			throw new IllegalArgumentException("Error parsing message: The byte array cannot be empty");
		}
		
		if(message.length > ProtocolConfig.MAX_POSSIBLE_SERIALIZED_MESSAGE_LENGTH)
		{
			throw new InvalidMessageException("Error parsing message: The maximum possible message size defined by the VRMeeting Messaging Protocol is " + ProtocolConfig.MAX_POSSIBLE_SERIALIZED_MESSAGE_LENGTH + " but the message supplied is " + message.length + " bytes long");
		}
		
		ClientSignals signal = extractSignal(message);
		switch(signal)
		{
		case AUDI:
			return parseAUDIMessage(message);
		case CHNG:
			return parseCHNGMessage(message);
		case END:
			return parseENDMessage();
		case TOKE:
			return parseTOKEMessage(message);
		case MID:
			return parseMIDMessage(message);
		case GAP:
			return parseGAPMessage(message);
		case GONE:
			return parseGONEMessage(message);
		case HERE:
			return parseHEREMessage(message);
		case HRTB:
			return parseHRTBMessage(message);
		case EAUD:
			return parseEAUDMessage(message);
		default:
			throw new InvalidMessageException("Warning: A " + signal.toString() + " message should not be received by the server");
		}
	}
	
	private static Message parseEAUDMessage(byte[] message) throws InvalidMessageException
	{
		//TODO Add validation to check the payload contains the length of the original audio
		byte[] payload = extractPayload(message);
		return new Message(ClientSignals.EAUD.toString(),payload);
	}
	
	private static Message parseGAPMessage(byte[] message) throws InvalidMessageException
	{
		return new Message(ClientSignals.GAP.toString(),null);
	}
	
	private static Message parseGONEMessage(byte[] message) throws InvalidMessageException
	{
		return new Message(ClientSignals.GONE.toString(),null);
	}
	
	private static Message parseHEREMessage(byte[] message) throws InvalidMessageException
	{
		return new Message(ClientSignals.HERE.toString(),null);
	}
	
	private static Message parseHRTBMessage(byte[] message) throws InvalidMessageException
	{
		return new Message(ClientSignals.HRTB.toString(),null);
	}
	
	private static Message parseMIDMessage(byte[] message) throws InvalidMessageException
	{ 
		byte[] payload = extractPayload(message);
		validateMIDPayload(payload);
		return new MeetingIDMessage(payload);
	}
	
	/**
	 * Checks the byte array passed in to see if it is a valid MID message payload according to the VRMeeting Messaging Protocol
	 * @param payload The payload to check
	 * @throws InvalidMessageException Thrown if the payload is invalid and contains the reason
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	private static void validateMIDPayload(byte[] payload) throws InvalidMessageException
	{
		if(!(payload.length > 0))
		{
			throw new InvalidMessageException("Error Parsing MID Message: The payload of an MID message cannot be empty");
		}
	}
	
	private static Message parseAUDIMessage(byte[] message) throws InvalidMessageException
	{
		byte[] payload = extractPayload(message);
		validateAUDIPayload(payload);
		return new Message(ClientSignals.AUDI.toString(),payload);
	}
	
	/**
	 * Checks the byte array passed in to see if it is a valid AUDI message payload according to the VRMeeting Messaging Protocol
	 * @param payload The payload to check
	 * @throws InvalidMessageException Thrown if the payload is invalid and contains the reason
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	private static void validateAUDIPayload(byte[] payload) throws InvalidMessageException
	{
		if(!(payload.length > 0))
		{
			throw new InvalidMessageException("Error Parsing AUDI Message: The payload of an AUDI message cannot be empty");
		}
//		if(!(payload.length % ProtocolConfig.BYTES_PER_AUDIO_SAMPLE == 0))
//		{
//			throw new InvalidMessageException("Error Parsing AUDI Message: The payload length isn't a multiple of the number of bytes per audio sample. Payload length: " + payload.length + ", bytes per audio samples: " + ProtocolConfig.BYTES_PER_AUDIO_SAMPLE);
//		}
	}
	
	private static Message parseCHNGMessage(byte[] message) throws InvalidMessageException
	{
		byte[] payload = extractPayload(message);
		validateCHNGPayload(payload);
		return new Message(ClientSignals.CHNG.toString(),payload);
	}
	
	/**
	 * Checks the byte array passed in to see if it is a valid CHNG message payload according to the VRMeeting Messaging Protocol
	 * @param payload The payload to check
	 * @throws InvalidMessageException Thrown if the payload is invalid and contains the reason
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	private static void validateCHNGPayload(byte[] payload) throws InvalidMessageException
	{
		if(!(payload.length > 0))
		{
			throw new InvalidMessageException("Error Parsing CHNG Message: The payload cannot be empty");
		}
		
		if(payload.length != ProtocolConfig.BYTES_PER_SLIDE_NUMBER)
		{
			throw new InvalidMessageException("Error Parsing CHNG Message: The payload must be " + ProtocolConfig.BYTES_PER_SLIDE_NUMBER + " bytes long but was " + payload.length + " bytes long");
		}
		
		int slideNumber = ByteBuffer.wrap(payload).getInt();
//		if(slideNumber == 0)
//		{
//			throw new InvalidMessageException("Error Parsing CHNG Message: The slide number cannot be 0");
//		}
		if(slideNumber < 0)
		{
			throw new InvalidMessageException("Error Parsing CHNG Message: The slide number cannot be negative");
		}
	}
	
	private static Message parseENDMessage()
	{
		return new Message(ClientSignals.END.toString(),null);
	}
	
	private static Message parseTOKEMessage(byte[] message) throws InvalidMessageException
	{
		byte[] payload = extractPayload(message);
		validateTOKEPayload(payload);
		return new AuthTokenMessage(payload);
	}
	
	/**
	 * Checks the byte array passed in to see if it is a valid TOKE message payload according to the VRMeeting Messaging Protocol
	 * @param payload The payload to check
	 * @throws InvalidMessageException Thrown if the payload is invalid and contains the reason
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	private static void validateTOKEPayload(byte[] payload) throws InvalidMessageException
	{
		if(!(payload.length > 0))
		{
			throw new InvalidMessageException("Error parsing TOKE message: The payload cannot be empty");
		}
	}
	
	/**
	 * Attempts to separate the payload from the signal in the raw message
	 * @param message The raw message to extract the payload from
	 * @return The byte array that represents the payload of the message
	 * @throws InvalidMessageException If the message does not contain a {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#END_OF_SIGNAL_DELIMITER}
	 */
	private static byte[] extractPayload(byte[] message) throws InvalidMessageException
	{
		int delimiterIndex = findSignalDelimiterIndex(message);
		int payloadStart = delimiterIndex+ProtocolConfig.END_OF_SIGNAL_DELIMITER.length();
		//Since 1 character = 1 byte when using ASCII
		byte[] payload = new byte[message.length - payloadStart];
		System.arraycopy(message, payloadStart, payload, 0, payload.length);
		return payload;
	}
	
	/**
	 * Attempts to decode the signal in the message and convert it to the appropriate {@link vrmeeting.messages.signals.Signals}
	 * @param message The raw message to extract the signal from
	 * @return Returns the {@link vrmeeting.messages.signals.Signals} that was encoded into the message
	 * @throws InvalidMessageException If there is an error with the format of the message such that the signal cannot be extracted
	 */
	private static ClientSignals extractSignal(byte[] message) throws InvalidMessageException
	{
		int delimiterIndex = findSignalDelimiterIndex(message);
		String messageAsString = new String(message);
		//Get the string that represents the signal
		String stringSignal = messageAsString.substring(0,delimiterIndex);
		try
		{
			ClientSignals signal = ClientSignals.valueOf(stringSignal);
			return signal;
		}catch(IllegalArgumentException i)
		{
			//If the string doesn't represent any of the valid signals
			throw new InvalidMessageException("Error parsing message: Unknown signal '" + stringSignal + "'");
		}
	}
	
	/**
	 * Attempts to find the index of the first character of the {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#END_OF_SIGNAL_DELIMITER} in a raw message
	 * @param message The message to search
	 * @return The index of the first character of the {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#END_OF_SIGNAL_DELIMITER} in the raw message
	 * @throws InvalidMessageException if the message does not contain a {@link main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#END_OF_SIGNAL_DELIMITER}
	 */
	private static int findSignalDelimiterIndex(byte[] message) throws InvalidMessageException
	{
		//First convert the whole message to a string as the signal and signal delimiter are encoded in ASCII
		String messageAsString = new String(message);
		int delimiterIndex = messageAsString.indexOf(ProtocolConfig.END_OF_SIGNAL_DELIMITER);
		if(delimiterIndex == -1)
		{
			throw new InvalidMessageException("Signal End Delimiter not found");
		}
		return delimiterIndex;
	}
}
