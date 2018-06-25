package main.java.com.hexcore.vrmeeting_hostserver.meeting.event;

import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * An class extended by classes which describe an action that will be performed on a meeting by an event runner
 * @author Psymj1 (Marcus)
 */
public abstract class Event {
	private MeetingClient clientWhoCreatedMeeting;
	private int priority;
	
	/**
	 * 
	 * @param createdBy The client which created the event
	 * @param priority The priority of the event. 0 being the highest priority, higher priority events are executed first
	 */
	public Event(MeetingClient createdBy,int priority)
	{
		clientWhoCreatedMeeting = createdBy;
		this.priority = priority;
	}
	
	public MeetingClient getClientWhoCreatedEvent()
	{
		return clientWhoCreatedMeeting;
	}
	
	/**
	 * Perform the event described by class extending this class
	 * @param meeting The meeting the event will be executed relative to
	 */
	public abstract void executeEvent(Meeting meeting);
	
	/**
	 * 
	 * @return The priority of the event. 0 being the highest priority, higher priority events are executed first
	 */
	public int getPriority()
	{
		return priority;
	}
}
