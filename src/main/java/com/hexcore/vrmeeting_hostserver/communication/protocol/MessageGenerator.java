package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import java.nio.ByteBuffer;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ServerSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * A Utility Class used to generate {@link Message} objects that are sent from the server to a client
 * @author Psymj1 (Marcus)
 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
 */
public final class MessageGenerator {
	/**
	 * Private Constructor so that the class cannot be initialised
	 */
	private MessageGenerator(){}
	
	/**
	 * @return Returns a {@link Message} whose signal is AUTH and payload is empty
	 */
	public static Message generateAuthenticationRequestMessage()
	{
		return new Message(ServerSignals.AUTH.toString(),null);
	}
	
	/**
	 * @return Returns a {@link Message} whose signal is VAL and payload is empty
	 */
	public static Message generateValidatedMessage()
	{
		return new Message(ServerSignals.VAL.toString(),null);
	}
	
	/**
	 * @return Returns a {@link Message} whose signal is NVAL and payload is empty
	 */
	public static Message generateNotValidatedMessage()
	{
		return new Message(ServerSignals.NVAL.toString(),null);
	}
	
	/**
	 * @return Returns a {@link Message} whose signal is MEET and payload is empty
	 */
	public static Message generateRequestMeetingIDMessage()
	{
		return new Message(ServerSignals.MEET.toString(),null);
	}
	
	/**
	 * Generate a UDM message with a payload which contains the information of the user who is passed in
	 * @param user The user to encode into the payload of the message
	 * @return A UDM message containing the given user's information in the payload
	 */
	public static Message generateUDMMessage(User user)
	{
		return new Message(ServerSignals.UDM.toString(),user.convertToJSON().getBytes(ProtocolConfig.SIGNAL_CHARACTER_SET));
	}
	
	/**
	 * Generate a LEFT message with the ID of the user who left in the payload
	 * @param userID The ID of the user who left 
	 * @return A message with the LEFT signal and the userID encoded in UTF-8 in the payload
	 */
	public static Message generateLeftMessage(int userID)
	{
		ByteBuffer id = ByteBuffer.allocate(4);
		id.putInt(userID);
		return new Message(ServerSignals.LEFT.toString(),id.array());
	}
	
	public static Message generateEAUDMessage()
	{
		return new Message(ClientSignals.EAUD.toString(),null);
	}
}
