package test.java.com.hexcore.vrmeeting_hostserver.mock;

import main.java.com.hexcore.vrmeeting_hostserver.dataserver.DataServerConnector;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMeetingIDException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidTokenException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ServerUnavailableException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * A mock {@link DataServerConnector} used in testing
 * Has the ability to change whether {@link #retrieveUserData(String)} throws an exception due to an invalid token and {@link #retreiveMeetingData(String)} throws an exception due to an invalid meeting id
 * Also whether either will throw an exception due to the data server being 'unavailable'
 * By default the server is available and all tokens and meetingid are valid
 * A defualt test user is returned unless an alternative is provided in the constructor
 * @author Psymj1 (Marcus)
 *
 */
public class MockDataServer implements DataServerConnector {
	
	private boolean available = true;
	private boolean authenticationTokenValid = true;
	private boolean meetingIDValid = true;
	private User testUser;
	private Meeting testMeeting;
	
	public MockDataServer(User testUser,Meeting testMeeting)
	{
		this.testUser = testUser;
		this.testMeeting = testMeeting;
	}
	
	@Override
	public boolean isAvailable() {
		return available;
	}
	
	/**
	 * Set whether the authentication token should be returned as valid or invalid when {@link #retrieveUserData(String)} is called
	 * @param valid Whether {@link #retrieveUserData(String)} throws an exception due to the token being invalid or not
	 */
	public void shouldAuthenticationTokenBeValid(boolean valid)
	{
		authenticationTokenValid = valid;
	}
	
	/**
	 * Set whether the meeting Id should be returned as valid or invalid when {@link #retreiveMeetingData(String)} is called
	 * @param valid Whether {@link #retrieveUserData(String)} throws an exception due to the meeting id being invalid or not
	 */
	public void shouldMeetingIDBeValid(boolean valid)
	{
		meetingIDValid = valid;
	}
	
	/**
	 * Set where {@link #isAvailable()} should return true or false
	 * @param available Whether {@link #isAvailable()} should return true or false
	 */
	public void shouldValidationServerBeAvailable(boolean available)
	{
		this.available = available;
	}

	@Override
	public User retrieveUserData(String token) throws InvalidTokenException, ServerUnavailableException {
		if(available)
		{
			if(authenticationTokenValid)
			{
				return testUser;
			}else
			{
				throw new InvalidTokenException();
			}
		}else
		{
			throw new ServerUnavailableException();
		}
		
	}

	@Override
	public Meeting retreiveMeetingData(String meetingID) throws InvalidMeetingIDException, ServerUnavailableException {
		if(available)
		{
			if(meetingIDValid)
			{
				return testMeeting;
			}else
			{
				throw new InvalidMeetingIDException();
			}
		}else
		{
			throw new ServerUnavailableException();
		}
	}
}
