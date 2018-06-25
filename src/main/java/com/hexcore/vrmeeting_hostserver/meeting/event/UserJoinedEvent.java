/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import java.util.Set;
import java.util.Map.Entry;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An event which when triggered will send the user data of the client who sent the message that created this message to everyone except the client themselves
 * @author Psymj1 (Marcus)
 *
 */
public class UserJoinedEvent extends Event {

	/**
	 * @param createdBy The client who created the message
	 */
	public UserJoinedEvent(MeetingClient createdBy) {
		super(createdBy,0);
	}

	/**
	 * Sends the client information to every participant of the meeting except the client who created the event
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> writers = meeting.getClientMessageWriters();
		for(Entry<MeetingClient,ThreadedBufferedMessageWriter> writer : writers)
		{
			if(writer.getKey() != getClientWhoCreatedEvent())
			{
				writer.getValue().enqueueMessage(MessageGenerator.generateUDMMessage(getClientWhoCreatedEvent().getUserInfo()));
			}
		}
	}

}
