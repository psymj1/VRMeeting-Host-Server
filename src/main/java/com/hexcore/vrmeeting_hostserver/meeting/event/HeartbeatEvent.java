/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An event which when triggered will refresh the heartbeat timer on the client who sent the message that triggered the event
 * @author Psymj1 (Marcus)
 *
 */
public class HeartbeatEvent extends Event {

	/**
	 * @param createdBy
	 */
	public HeartbeatEvent(MeetingClient createdBy) {
		super(createdBy,0);
	}

	/**
	 * Refreshes the heartbeat on the meeting client who created the event
	 * @see main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event#executeEvent(main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting)
	 */
	@Override
	public void executeEvent(Meeting meeting) {
		new ServerLogger("Heartbeat Event for " + getClientWhoCreatedEvent().getConnection().getName()).logInfo("Heartbeat");;
		
		getClientWhoCreatedEvent().refreshHeartbeat();
	}

}
