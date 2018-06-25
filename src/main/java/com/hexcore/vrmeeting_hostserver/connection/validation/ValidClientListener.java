package main.java.com.hexcore.vrmeeting_hostserver.connection.validation;

import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * A class implementing this interface can be registered to a ClientValidator and will have {@link main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ValidClientListener#connectionValid(MeetingClient)} called when a connection is validated
 * @author Marcus
 *
 */
public interface ValidClientListener {
	public void connectionValid(MeetingClient validClient);
}
