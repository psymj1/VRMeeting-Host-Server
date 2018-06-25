package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import java.nio.ByteBuffer;

import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;

/**
 * This class models a 'message' sent during the VRMeeting Messaging Protocol.
 * It contains a 'signal' which indicates the purpose of the message and a 'payload' which is the content of the message itself.
 * The message 'signal' denotes the format of the message 'payload'
 * @author Psymj1 (Marcus)
 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
 */
public class Message {
	private String signal;
	private byte[] payload;
	
	/**
	 * Creates a new Message
	 * Adheres to the standard defined by the VRMeeting Messaging Protocol
	 * @param signal Must be less than {@value main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#MAX_SIGNAL_LENGTH} characters Long. Denotes the purpose of the message and the format of the 'payload'
	 * @param payload Must be less than {@value main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig#MAX_SERIALIZED_PAYLOAD_SIZE} bytes long. The data to be sent in the message.
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public Message(String signal,byte[] payload)
	{
		if(signal == null)
		{
			throw new IllegalArgumentException("Signal cannot be null");
		}
		if(signal.equals(""))
		{
			throw new IllegalArgumentException("Signal cannot be empty");
		}
		
		if(payload != null && payload.length > ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE)
		{
			throw new IllegalArgumentException("Payload is too long. Expected Payload Length < " + ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE + ". Actual length was (" + payload.length + ")");
		}
		
		if(signal.length() > ProtocolConfig.MAX_SIGNAL_LENGTH)
		{
			throw new IllegalArgumentException("Signal is too long. Expected Signal Length < " + ProtocolConfig.MAX_SIGNAL_LENGTH + ". Actual length was (" + signal.length() + ")");
		}

		this.signal = signal;
		this.payload = (payload == null ? null : (payload.length > 0 ? payload : null));
	}
	
	private int getPayloadLength()
	{
		return payload == null ? 0 : payload.length;
	}
	
	/**
	 * Converts the signal and payload stored in the message to a valid form as defined by the VRMeeting Messaging Protocol
	 * @return Returns a byte array which represents the data the signal and payload stored in the object
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public byte[] getTransmittableMessage()
	{
		int lengthOfPayload = getPayloadLength();
		byte[] signalAsByteArray = signal.getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		
		byte[] message = new byte[signalAsByteArray.length + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length + lengthOfPayload];
		int offset = 0;
		
		System.arraycopy(signalAsByteArray, 0, message, offset, signalAsByteArray.length);
		offset += signalAsByteArray.length;
		System.arraycopy(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER, 0, message, offset, ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length);
		offset += ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length;
		
		if(payload != null)
		{
			System.arraycopy(payload, 0, message, offset, payload.length);
			offset += payload.length;
		}
		
		return message;
	}
	
	/**
	 * Converts the signal and payload stored in the message to the new message format which includes the {@link ProtocolConfig#ENCODED_END_PAYLOAD_DELIMITER} on the end of the payload so the server and client can tell when a message ends
	 * @return Returns a byte array which represents the data the signal and payload stored in the object
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public byte[] getNewTransmittableMessage()
	{
		int lengthOfPayload = getPayloadLength();
		byte[] signalAsByteArray = signal.getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET);
		
		ByteBuffer message = ByteBuffer.allocate(signalAsByteArray.length + ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER.length + lengthOfPayload + ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER.length);
		message.put(signalAsByteArray);
		message.put(ProtocolConfig.ENCODED_END_OF_SIGNAL_DELIMITER);
		
		if(getPayloadLength() > 0)
		{
			message.put(getPayload());
		}
		
		message.put(ProtocolConfig.ENCODED_END_PAYLOAD_DELIMITER);
		
		return message.array();
	}
	
	public String getSignal()
	{
		return signal;
	}
	
	public byte[] getPayload()
	{
		return payload;
	}
}
