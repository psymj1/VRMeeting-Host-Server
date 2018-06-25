/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.connection.validation;

import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * A class implementing this interface will be the output for a ValidationThread if a connection is validated. It will have it's {@link #connectionValidationOutput(MeetingClient)} method called if the validator thread it is registered to succeeds
 * @author Psymj1 (Marcus)
 *
 */
public interface ValidConnectionOutput {
	public void connectionValidationOutput(MeetingClient client);
}
