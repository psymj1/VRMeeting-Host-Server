/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import java.util.Map.Entry;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An event which when triggered will send the Change Slide message that was received by the server to all of the clients in the meeting
 * @author Psymj1 (Marcus)
 *
 */
public class ChangeSlideEvent extends Event {

	private Message chngMessage;
	
	/**
	 *	@param m The CHNG message that will be sent to all clients
	 */
	public ChangeSlideEvent(Message m) {
		super(null,0);
		chngMessage = m;
	}

	/**
	 * Sends the CHNG message that was received by the server to all clients in the meeting that calls this event
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		ServerLogger logger = new ServerLogger("Change Slide Event");
		
		ByteBuffer b = ByteBuffer.wrap(chngMessage.getPayload());
		b.order(ByteOrder.LITTLE_ENDIAN);
		logger.logInfo("Triggered! Broadcasting to change to slide " + b.getInt() + " in meeting " + meeting.getMeetingCode());
		Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> writers = meeting.getClientMessageWriters();
		for(Entry<MeetingClient,ThreadedBufferedMessageWriter> writer : writers)
		{
			logger.logInfo("Queuing message to " + writer.getKey().getConnection().getName());
			writer.getValue().enqueueMessage(chngMessage);
		}
	}

}
