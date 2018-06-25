/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import java.util.Map.Entry;
import java.util.Set;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An event which when triggered will send a LEFT message to all clients except the client who sent the message that created this event
 * It will then remove the client in question from the meeting and attempt to close the connection to the client
 * @author Psymj1 (Marcus)
 *
 */
public class UserLeftEvent extends Event {
	
	private static ServerLogger logger;
	
	/**
	 * @param createdBy The client who created this event 
	 */
	public UserLeftEvent(MeetingClient createdBy) {
		super(createdBy,0);
		logger = new ServerLogger("User left event created by " + createdBy.getConnection().getName());
	}

	/**
	 * Sends a LEFT message to all clients except the client who sent the message
	 * Then removes the client from the meeting and closes the connection
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> writers = meeting.getClientMessageWriters();
		Message leftMessage = MessageGenerator.generateLeftMessage(getClientWhoCreatedEvent().getUserInfo().getUserID());
		for(Entry<MeetingClient,ThreadedBufferedMessageWriter> writer : writers)
		{
			if(writer.getKey() != getClientWhoCreatedEvent())
			{
				writer.getValue().enqueueMessage(leftMessage);
			}
		}
		
		meeting.removeParticipant(getClientWhoCreatedEvent());
		
		try {
			getClientWhoCreatedEvent().getConnection().close();
		} catch (ConnectionErrorException e) {
			logger.logError(e.getMessage(),e);
		}
	}

}
