/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.exception.MessageNotEventException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * A Utility class which contains methods used to translate a Message into the event that it represents
 * @author Psymj1 (Marcus)
 *
 */
public class MessageEventParser {
	private MessageEventParser()
	{
		
	}
	
	/**
	 * Converts the given message, if possible, into an {@link Event}
	 * @param m The message to convert to an event
	 * @param origin The meetingclient who sent the message
	 * @return The event represented by the message
	 * @throws MessageNotEventException thrown if the message passed in does not represent an event that occurs during the meeting
	 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol</a>
	 */
	public static final Event parseMessageToEvent(MeetingClient origin,Message m) throws MessageNotEventException
	{
		try
		{
			ClientSignals signal = ClientSignals.valueOf(m.getSignal());
			switch(signal)
			{
			case AUDI:
				return new TransmitAudioEvent(origin, m);
			case CHNG:
				return new ChangeSlideEvent(m);
			case GAP:
				return new GetAllParticipantsEvent(origin);
			case GONE:
				return new UserLeftEvent(origin);
			case HERE:
				return new UserJoinedEvent(origin);
			case HRTB:
				return new HeartbeatEvent(origin);
			case EAUD:
				return new EndOfAudioSegmentEvent(origin,m);
			default:
				throw new MessageNotEventException();
			}
		}catch(IllegalArgumentException e)
		{
			throw new MessageNotEventException();
		}
		
	}
}
