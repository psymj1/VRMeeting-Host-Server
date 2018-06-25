package main.java.com.hexcore.vrmeeting_hostserver.config;

import java.nio.charset.Charset;

/**
 * A class used to store the properties relating to the VRMeeting Messaging Protocol
 * Final so that it cannot be extended
 * @author Psymj1 (Marcus)
 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
 */
public final class ProtocolConfig {
	/**
	 * Private constructor so that it cannot be instantiated
	 */
	private ProtocolConfig(){}
	
	/**
	 * Represents the character(s) which will denote the end of the signal in a message
	 */
	public static final String END_OF_SIGNAL_DELIMITER = "\n";
	
	/**
	 * Represents the character(s) which will denote the end of the payload in a message
	 */
	public static final String END_OF_PAYLOAD_DELIMITER = "d11ebd74585511e89c2dfa7ae01bbebc";
	
	public static final byte[] ENCODED_END_PAYLOAD_DELIMITER = END_OF_PAYLOAD_DELIMITER.getBytes();
	  
	/**
	 * The name of the character set used to encode the signal of a message as defined by the VRMeeting Messaging Protocol
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static final String SIGNAL_CHARACTER_SET_NAME = "UTF-8";
	
	/**
	 * The Java CharacterSet that is referenced by the {@link #SIGNAL_CHARACTER_SET_NAME}
	 */
	public static final Charset SIGNAL_CHARACTER_SET = Charset.forName(SIGNAL_CHARACTER_SET_NAME);
	
	/**
	 * The name of the character set used to encode the Meeting ID of a MID message as defined by the VRMeeting Messaging Protocol
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static final String MEETING_ID_CHARACTER_SET_NAME = "UTF-8";
	
	/**
	 * The Java CharacterSet that is referenced by the {@link #MEETING_ID_CHARACTER_SET_NAME}
	 */
	public static final Charset MEETING_ID_CHARACTER_SET = Charset.forName(MEETING_ID_CHARACTER_SET_NAME);
	
	/**
	 * The name of the character set used to encode the authentication token of a user as defined by the VRMeeting Messaging Protocol
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static final String AUTHENTICATION_TOKEN_CHARACTER_SET_NAME = "UTF-8";
	
	/**
	 * The Java CharacterSet that is referenced by the {@link #AUTHENTICATION_TOKEN_CHARACTER_SET_NAME}
	 */
	public static final Charset AUTHENTICATION_TOKEN_CHARACTER_SET = Charset.forName(AUTHENTICATION_TOKEN_CHARACTER_SET_NAME);
	
	/**
	 * The number of bytes used to represent 1 character in the {@link #SIGNAL_CHARACTER_SET}
	 */
	public static final int BYTES_PER_SIGNAL_CHARACTER = 4;
	
	/**
	 * The {@link #END_OF_SIGNAL_DELIMITER} Encoded using {@link #SIGNAL_CHARACTER_SET}
	 */
	public static final byte[] ENCODED_END_OF_SIGNAL_DELIMITER = SIGNAL_CHARACTER_SET.encode(END_OF_SIGNAL_DELIMITER).array();
	
	/**
	 * The maximum number of characters that can be used to define a 'signal' as defined by the VRMeeting Messaging Protocol
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static final int MAX_SIGNAL_LENGTH = 4; //In characters
	
	/**
	 * The maximum possible number of bytes that a signal could consist of assuming it is {@link #MAX_SIGNAL_LENGTH} characters long
	 * Defined as {@link #MAX_SIGNAL_LENGTH} * {@link #BYTES_PER_SIGNAL_CHARACTER}
	 */
	public static final int MAX_BYTES_PER_SIGNAL = MAX_SIGNAL_LENGTH * BYTES_PER_SIGNAL_CHARACTER; //In Bytes
	
	/**
	 * The maximum serialized signal length is defined as the {@link #MAX_BYTES_PER_SIGNAL} + The length of the {@link #ENCODED_END_OF_SIGNAL_DELIMITER}
	 */
	public static final int MAX_SERIALIZED_SIGNAL_LENGTH = MAX_BYTES_PER_SIGNAL + ENCODED_END_OF_SIGNAL_DELIMITER.length;//In Bytes
	
	/**
	 * The maximum size of the payload that can be sent and received as denoted by the VRMeeting Messaging Protocol
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static final int MAX_SERIALIZED_PAYLOAD_SIZE = 10000; //In Bytes
	
	/**
	 * The maximum possible serialized message length is defined as the sum of the {@link #MAX_SERIALIZED_SIGNAL_LENGTH} + the {@link #MAX_SERIALIZED_PAYLOAD_SIZE}
	 */
	public static final int MAX_POSSIBLE_SERIALIZED_MESSAGE_LENGTH = MAX_SERIALIZED_SIGNAL_LENGTH + MAX_SERIALIZED_PAYLOAD_SIZE; //In Bytes
	
	/**
	 * Represents the number of bytes per audio sample in an audio transmission message. Used to check the integrity of messages claiming to be transmitting audio
	 */
	public static final int BYTES_PER_AUDIO_SAMPLE = 4; //In Bytes
	
	/**
	 * The number of bytes used to define the slide number in a CHNG message
	 */
	public static final int BYTES_PER_SLIDE_NUMBER = Integer.BYTES;
	
}
