package main.java.com.hexcore.vrmeeting_hostserver.meeting;

import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * A meeting client is a class used to store a connection and the information about the user who is using it
 * @author Psymj1 (Marcus)
 *
 */
public class MeetingClient{
	
	private User userinfo;
	private Connection connection;
	private long lastHeartbeat;
	private static final long HEARTBEAT_TIMEOUT_LENGTH = 7000; //In ms, the maximum time before the heartbeat expires
	
	public MeetingClient(Connection connection, User user)
	{
		this.connection = connection;
		userinfo = user;
		refreshHeartbeat();
	}
	
	public User getUserInfo()
	{
		return userinfo;
	}
	
	public Connection getConnection()
	{
		return connection;
	}
	
	@Override
	public boolean equals(Object toCompare)
	{
		if(toCompare.getClass() == this.getClass())
		{
			MeetingClient comparisonClient = (MeetingClient)toCompare;
			return comparisonClient.getUserInfo().equals(userinfo);
		}
		return false;
	}
	
	/**
	 * Overridden to allow meeting clients to be identified as the same in a hash set if they have the same user
	 */
	@Override
	public int hashCode()
	{
		return userinfo.hashCode();
	}
	
	/**
	 * Resets the heartbeat on the MeetingClient
	 */
	public void refreshHeartbeat()
	{
		lastHeartbeat = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @return True if {@link #HEARTBEAT_TIMEOUT_LENGTH} has passed since the last heart beat was received from the client
	 */
	public boolean hasHeartbeatExpired()
	{
		return System.currentTimeMillis() >= lastHeartbeat + HEARTBEAT_TIMEOUT_LENGTH;
	}
}
