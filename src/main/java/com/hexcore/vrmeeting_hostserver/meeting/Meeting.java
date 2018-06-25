package main.java.com.hexcore.vrmeeting_hostserver.meeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponent;
import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.connection.IncomingMessageBroadcaster;
import main.java.com.hexcore.vrmeeting_hostserver.connection.MessageBroadcastSubscriber;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ThreadedBufferedMessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.MessageNotEventException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.UserExistsException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.Event;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.MessageEventParser;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.UserLeftEvent;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * Represents a collection of {@link User}s who can communicate between one another
 * A meeting has an ID, a number of people present and 1 presenter
 * Once started a meeting will execute events added to a queue by the connections present in the meeting clients
 * @author Psymj1 (Marcus)
 *
 */
public class Meeting extends ServerComponent implements MessageBroadcastSubscriber{
	private String meetingCode;
	private int presenterUID = -1;
	private Set<MeetingClient> participants = new HashSet<MeetingClient>();
	private Map<MeetingClient,IncomingMessageBroadcaster> clientBroadcasters = new HashMap<MeetingClient,IncomingMessageBroadcaster>();
	private Map<MeetingClient,ThreadedBufferedMessageWriter> clientWriters = new HashMap<MeetingClient,ThreadedBufferedMessageWriter>();
	private String meetingLock = "MeetingLock"; //Used to avoid concurrent modification of clientBroadcasters, clientWriters and participants
	private boolean runMeetingThread = true;
	private String eventLock = "EventLock";
	private ArrayList<ArrayList<Event>> eventQueueList = new ArrayList<>();
	private static final int MIN_EVENT_POLL_RATE = 10; //In ms, the minimum rate at which the event queue will be checked by the thread
	private static final int HEARTBEAT_CHECK_RATE = 100; //In ms, the rate at which the heartbeats of every client are checked to see if they have expired in order to remove them from a meeting 
	private static final int MEETING_CLOSE_CHECK_RATE = 5000; //In ms the rate at which the meeting checks if it has any participants, closing if there aren't any remaining
	private long lastCloseCheck = System.currentTimeMillis();
	private boolean oneUserJoined = false; //Only close if empty if 1 user has joined before
	private long lastHeartbeatCheck = System.currentTimeMillis(); //The last time the heartbeats of all clients were checked
	private static ServerLogger logger;
	
	/**
	 * @param meetingCode The meeting code of the meeting that this object represents. Used when attempting to add a user to a specific meeting
	 */
	public Meeting(String meetingCode)
	{
		this.meetingCode = meetingCode;
		logger = new ServerLogger("Meeting " + meetingCode);
	}
	
	/**
	 * Add a new participant to the meeting. Note this will not send out a notification to all other clients. This must be done by the client sending a JOIN message to the server
	 * Also adds the client to the sub components of the meeting
	 * @param client The client of the participant to add
	 * @throws UserExistsException If the user referred to by the MeetingClient has already exists in the meeting, this is a comparison between the underlying user objects
 	 */
	public void addNewParticipant(MeetingClient client) throws UserExistsException
	{
		oneUserJoined = true;
		if(participants.contains(client))
		{
			throw new UserExistsException();
		}else
		{
			IncomingMessageBroadcaster broadcaster = new IncomingMessageBroadcaster(client.getConnection());
			broadcaster.addSubscriber(this);
			ThreadedBufferedMessageWriter writer = new ThreadedBufferedMessageWriter(client.getConnection());
			addSubServerComponent(broadcaster);
			addSubServerComponent(writer);
			
			synchronized(meetingLock)
			{
				participants.add(client);
				clientBroadcasters.put(client,broadcaster);
				clientWriters.put(client,writer);
			}
			
			try {
				broadcaster.start();
				writer.start();
			} catch (StartupException | IllegalComponentStateException e) {
				logger.logError("Failed to start the broadcaster or writer threads for connection " + client.getConnection().getName(), e);
			}
		}
	}
	
	/**
	 * Removes a participant from the meeting if it is participating. Note this will not send out a notification to all other clients. This must be done by the client sending a LEFT message to the server
	 * @param client The client to remove
	 */
	public void removeParticipant(MeetingClient client)
	{
		IncomingMessageBroadcaster broadcaster;
		ThreadedBufferedMessageWriter writer;
		
		//Remove the client from the list of participants
		//Remove the writer
		//Remove the broadcaster
		synchronized(meetingLock)
		{
			broadcaster = clientBroadcasters.get(client);
			clientBroadcasters.remove(client);
			writer = clientWriters.get(client);
			clientWriters.remove(client);
			participants.remove(client);
		}
		try
		{
			broadcaster.stop();
			writer.stop();
		}catch(IllegalComponentStateException e)
		{
			logger.logIgnore(e.getMessage());
		}		
		
		removeSubServerComponent(broadcaster);
		removeSubServerComponent(writer);
		logger.logInfo(client.getUserInfo().getFirstName() + " on connection " + client.getConnection().getName() + " has been removed from the meeting");
	}
	
	private void closeMeeting()
	{
		logger.logInfo("No more participants in meeting " + getMeetingCode() + ", closing meeting.");
		getParentComponent().removeSubServerComponent(this);
		try {
			stop();
		} catch (IllegalComponentStateException e) {
			logger.logIgnore(e.getMessage());
		}
	}
	
	public int getNumberParticipants()
	{
		int num = 0;
		synchronized(meetingLock)
		{
			num = participants.size();
		}
		return num;
	}
	
	public MeetingClient[] getMeetingParticipants()
	{
		synchronized(meetingLock)
		{
			return participants.toArray(new MeetingClient[0]);
		}
	}
	
	public String getMeetingCode()
	{
		return meetingCode;
	}
	
	public int getPresenterID()
	{
		return presenterUID;
	}
	
	public void setPresenterUID(int uID)
	{
		presenterUID = uID;
	}

	@Override
	protected void startUp() throws StartupException {
		logger.logInfo("Starting meeting...");
		new MeetingThread(logger.getName()).start();
	}

	@Override
	protected void shutdown() {
		logger.logInfo("Stopping meeting...");
		runMeetingThread = false;
	}

	@Override
	public void onReceiveBroadcast(Connection origin,Message m) {
		MeetingClient originClient = null;
		
		synchronized(meetingLock) //Potential point of congestion as every message received will have to wait for this lock
		{
			for(MeetingClient client : participants)
			{
				if(client.getConnection() == origin)
				{
					originClient = client;
				}
			}
		}
		
		if(originClient != null)
		{
			try {
				Event newEvent = MessageEventParser.parseMessageToEvent(originClient,m);
				addEventToQueues(newEvent);
			} catch (MessageNotEventException e) {
				logger.logWarning("Message received that is not valid event from " + origin.getName());
			}
		}else
		{
			logger.logWarning("Client " + origin.getName() + " could not be found in the meeting to translate their " + m.getSignal() + " message to an event");
		}
	}
	
	private class MeetingThread extends Thread
	{
		
		public MeetingThread(String name)
		{
			setName(name);
		}
		
		@Override
		public void run()
		{
			logger.logInfo("Meeting started");
			while(runMeetingThread)
			{
				if(shouldCheckHeartbeats())
				{
					checkHeartbeats();
				}
				
				Event nextEvent = null;
				
				synchronized(eventLock)
				{
					//Look through each priority queue from highest to lowest, if a queue is not empty then get the next event and execute it
					for(ArrayList<Event> priorityQueue : eventQueueList)
					{
						if(!priorityQueue.isEmpty())
						{
							nextEvent = priorityQueue.get(0);
							priorityQueue.remove(0);
							break;
						}
					}
				}
				
				if(nextEvent != null)
				{
					nextEvent.executeEvent(Meeting.this);
				}else
				{
					try {
						Thread.sleep(MIN_EVENT_POLL_RATE);
					} catch (InterruptedException e) {
						
					}
				}
				
				if(oneUserJoined && System.currentTimeMillis() >= lastCloseCheck + MEETING_CLOSE_CHECK_RATE)
				{
					synchronized(meetingLock)
					{
						if(getNumberParticipants() == 0) //If the meeting is empty then close it
						{
							closeMeeting(); //TODO Add testing for this feature
						}
					}
				}
				
			}
			
			setState(ServerComponentState.STOPPED);
			logger.logInfo("Meeting stopped");
		}
	}
	
	/**
	 * Go through every client in the meeting and check if their heart beat has expired, if so then generate an event equivalent to them leaving which will remove them from the meeting
	 */
	private void checkHeartbeats()
	{
		lastHeartbeatCheck = System.currentTimeMillis();
		synchronized(meetingLock)
		{
			for(MeetingClient client : getMeetingParticipants())
			{
				if(client.hasHeartbeatExpired())
				{
					logger.logWarning("Connection to " + client.getConnection().getName() + " has timed out");
					UserLeftEvent event = new UserLeftEvent(client);
					addEventToQueues(event);
				}
			}
		}
	}
	
	private void addEventToQueues(Event e)
	{
		ArrayList<Event> eventQueue = null;
		while(eventQueue == null) //Either get the priority queue for the new event priority or generate them until you do
		{
			try
			{
				eventQueue = eventQueueList.get(e.getPriority());
			}catch(IndexOutOfBoundsException a)
			{
				logger.logInfo("Event Queue not found for priority " + e.getPriority() + ", generating event queue for priority " + eventQueueList.size());
				eventQueueList.add(new ArrayList<Event>());
			}
		}
		eventQueue.add(e); //Add the event to its respective priority queue
	}
	
	private boolean shouldCheckHeartbeats()
	{
		return System.currentTimeMillis() >= lastHeartbeatCheck + HEARTBEAT_CHECK_RATE;
	}
	
	/**
	 * Get a set which represents each meeting client in the meeting with its respective threaded writer for queueing messages to be sent back to the clients
	 * @return A set containing the MeetingClients and their respective {@link ThreadedBufferedMessageWriter}s
	 */
	public Set<Entry<MeetingClient,ThreadedBufferedMessageWriter>> getClientMessageWriters()
	{
		synchronized(meetingLock)
		{
			return clientWriters.entrySet();
		}
	}
}
