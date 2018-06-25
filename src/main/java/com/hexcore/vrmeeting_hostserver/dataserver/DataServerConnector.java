package main.java.com.hexcore.vrmeeting_hostserver.dataserver;

import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMeetingIDException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidTokenException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ServerUnavailableException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * Defines an interface used to access a remote server for retrieving data about clients
 * @author Psymj1 (Marcus)
 *
 */
public interface DataServerConnector {
	/**
	 * 
	 * @return True if the validation server is currently reachable false if not
	 */
	public boolean isAvailable();
	
	/**
	 * Uses the given token to retrieve information about the related user from the data server
	 * @param token The Authentication Token to use to access the data
	 * @return The User information if the token is valid
	 * @throws InvalidTokenException If the token supplied is invalid
	 * @throws ServerUnavailableException If the data server isn't currently available
	 */
	public User retrieveUserData(String token) throws InvalidTokenException,ServerUnavailableException;
	
	/**
	 * Uses the given meetingID to retrieve information about the related meeting from the data server. Note the data server may not keep information such as the number of current participants in the meeting
	 * @param meetingID The meeting ID to use
	 * @return The meeting information if the meetingID is valid
	 * @throws InvalidMeetingIDException If the meetingID supplied isn't valid
	 * @throws ServerUnavailableException If the data server isn't currently available
	 */
	public Meeting retreiveMeetingData(String meetingID) throws InvalidMeetingIDException,ServerUnavailableException;
}
