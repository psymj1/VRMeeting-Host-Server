/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import java.util.Map.Entry;
import java.util.Set;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * This Event will, when triggered, send the original EAUD message to all clients other than the presenter
 * @author Psymj1 (Marcus)
 *
 */
public class EndOfAudioSegmentEvent extends Event {

	private Message original;
	
	public EndOfAudioSegmentEvent(MeetingClient createdBy,Message originalMessage) {
		super(createdBy, 5);
		original = originalMessage;
	}

	/**
	 * When triggered will send the original EAUD message to every client other than the presenter
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		ServerLogger logger = new ServerLogger("End of Audio Event");
//		logger.logInfo("Event executing");
		Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> writers = meeting.getClientMessageWriters();
		for(Entry<MeetingClient,ThreadedBufferedMessageWriter> writer : writers)
		{
			if(writer.getKey() != getClientWhoCreatedEvent())
			{
				writer.getValue().enqueueMessage(original);
			}
		}
//		logger.logInfo("Event finished");
	}

}
