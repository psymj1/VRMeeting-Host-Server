/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import java.util.Map.Entry;
import java.util.Set;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An event which when triggered will send the user information of all the current participants in a meeting to the client which sent the message that generated this event
 * This includes themselves
 * @author Psymj1 (Marcus)
 *
 */
public class GetAllParticipantsEvent extends Event {
	private static ServerLogger logger;
	
	/**
	 * @param createdBy The MeetingClient which sent the message that created this event
	 */
	public GetAllParticipantsEvent(MeetingClient createdBy) {
		super(createdBy,0);
		logger = new ServerLogger("Get all participants event sent by " + createdBy.getConnection().getName());
	}

	/**
	 * Sends the user information of all the current participants of the meeting to the user who sent the message that generated this event
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		logger.logInfo("Executing");
		Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> writers = meeting.getClientMessageWriters();
		ThreadedBufferedMessageWriter output = null;
		
		for(Entry<MeetingClient,ThreadedBufferedMessageWriter> writer : writers)
		{
			if(writer.getKey() == getClientWhoCreatedEvent())
			{
				output = writer.getValue();
				break;
			}
		}
		
		if(output != null)
		{
			MeetingClient[] allParticipants = meeting.getMeetingParticipants();
			for(MeetingClient client : allParticipants)
			{
				output.enqueueMessage(MessageGenerator.generateUDMMessage(client.getUserInfo()));
			}
		}else
		{
			logger.logWarning("Event failed to execute as client who requested participant information is no longer present");
		}
		logger.logInfo("Event finished");
	}

}
