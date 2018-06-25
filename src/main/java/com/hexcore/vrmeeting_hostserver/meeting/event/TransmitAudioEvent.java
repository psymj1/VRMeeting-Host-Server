/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import java.util.Set;
import java.util.Map.Entry;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An event which when triggered will transmit the received audio message to all clients in the meeting other than the client which sent the message originally
 * @author Psymj1 (Marcus)
 *
 */
public class TransmitAudioEvent extends Event {

	private Message audioMessage;
	
	/**
	 * @param createdBy
	 */
	public TransmitAudioEvent(MeetingClient createdBy,Message audioMessage) {
		super(createdBy,5);
		this.audioMessage = audioMessage;
	}

	/**	
	 * Sends the AUDI message received by the server to all clients in the meeting which calls this event except the client who created the event
	 * This stops the presenter from hearing themselves
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		ServerLogger logger = new ServerLogger("Audio Event");
		Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> writers = meeting.getClientMessageWriters();
		for(Entry<MeetingClient,ThreadedBufferedMessageWriter> writer : writers)
		{
			if(writer.getKey() != getClientWhoCreatedEvent())
			{
				writer.getValue().enqueueMessage(audioMessage);
			}
		}
	}

}
