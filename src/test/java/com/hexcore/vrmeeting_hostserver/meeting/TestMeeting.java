package test.java.com.hexcore.vrmeeting_hostserver.meeting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.exception.IllegalComponentStateException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.UserExistsException;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

public class TestMeeting {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
	private ScriptedMockConnection connection = new ScriptedMockConnection();
	private String testMeetingID = "Test123";
	private MeetingClient client;
	private Meeting testMeeting;
	
	@Before
	public void before()
	{
		testMeeting = new Meeting("TestMeeting");
		testValidUser.setMeetingCode(testMeetingID);
		client = new MeetingClient(connection,testValidUser);
	}
	
	/**
	 * Tests to make sure that even when there are sub components attached to the meeting (The threads that handle the message exchanges with clients), it still moves to the stopped state
	 * @throws StartupException If there is an error starting the meeting
	 * @throws UserExistsException Should not be thrown as unique users are being generated in the test
	 * @throws IllegalComponentStateException 
	 */
	@Test(timeout = 3000)
	public void testMeetingStopsWhenMultipleClientsArePresent() throws StartupException, UserExistsException, IllegalComponentStateException
	{
		testMeeting.start();
		int randomNumberOfParticipants = (int)Math.floor(Math.random()*10)+1; //Random number between 1 and 10
		
		MeetingClient[] testParticipants = new MeetingClient[randomNumberOfParticipants];
		for(int i = 0;i < randomNumberOfParticipants;i++)
		{
			User user = new User(i,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1); //Just increasing the uid will make each user unique
			testParticipants[i] = new MeetingClient(new ScriptedMockConnection(),user);
			testMeeting.addNewParticipant(testParticipants[i]);
		}
		testMeeting.stop();
		//Generates a random number of users between 1 and 10, adds them to the meeting which will start threads to handle their messages and then stops the meeting
		while(testMeeting.getState() != ServerComponentState.STOPPED);
	}
	
	@Test
	public void addParticipantShouldAddMeetingClientToListOfParticipants() throws UserExistsException
	{
		testMeeting.addNewParticipant(client);
		assertTrue("The MeetingClient was not added to the list of participants",Arrays.asList(testMeeting.getMeetingParticipants()).contains(client));
	}
	
	@Test
	public void addParticipantShouldThrowExceptionIfClientAlreadyPresent() throws UserExistsException
	{
		testMeeting.addNewParticipant(client);
		thrown.expect(UserExistsException.class);
		testMeeting.addNewParticipant(client);
	}
	
	@Test
	public void addParticipantShouldThrowExceptionIfClientAlreadyPresentWhereOnlyUserDataMatch() throws UserExistsException
	{
		ScriptedMockConnection connection1 = new ScriptedMockConnection();
		ScriptedMockConnection connection2 = new ScriptedMockConnection();
		MeetingClient client1 = new MeetingClient(connection1, testValidUser);
		MeetingClient client2 = new MeetingClient(connection2, testValidUser);
		//2 clients with the same user info but different connections, example scenario if a user logs in to 2 places and tries to connect to the same meeting
		thrown.expect(UserExistsException.class);
		testMeeting.addNewParticipant(client1);
		testMeeting.addNewParticipant(client2);
	}
	
	@Test
	public void removeParticipantRemovesMeetingClientFromListOfParticipants() throws UserExistsException
	{
		testMeeting.addNewParticipant(client);
		assertTrue("The MeetingClient was not added to the list of participants",Arrays.asList(testMeeting.getMeetingParticipants()).contains(client));
		testMeeting.removeParticipant(client);
		assertFalse("The MeetingClient was not added to the list of participants",Arrays.asList(testMeeting.getMeetingParticipants()).contains(client));
	}
	
	@Test(timeout = 3000)
	public void meetingStopsWhenStopIsCalled() throws StartupException, IllegalComponentStateException
	{
		testMeeting.start();
		testMeeting.stop();
		while(testMeeting.getState() != ServerComponentState.STOPPED);
	}
	
	@Test
	public void getNumberOfParticipantsShouldReturnNumberOfClientsAdded() throws UserExistsException
	{
		int randomNumberOfParticipants = (int)Math.floor(Math.random()*10)+1; //Random number between 1 and 10
		
		MeetingClient[] testParticipants = new MeetingClient[randomNumberOfParticipants];
		for(int i = 0;i < randomNumberOfParticipants;i++)
		{
			User user = new User(i,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1); //Just increasing the uid will make each user unique
			testParticipants[i] = new MeetingClient(new ScriptedMockConnection(),user);
			testMeeting.addNewParticipant(testParticipants[i]);
		}
		
		assertEquals("The number of participants returned was incorrect",randomNumberOfParticipants,testMeeting.getNumberParticipants());
	}
	
	@Test
	public void getPresenterIDReturnsNegativeOneIfNotSet()
	{
		assertEquals("The presenter ID returned was not -1",-1,testMeeting.getPresenterID());
	}
	
	@Test
	public void getPresenterIDReturnsTheIDOfPresenterGivenWhenSetIsCalled()
	{
		testMeeting.setPresenterUID(10);
		assertEquals("The presenter ID returned does not match the id given in the set method",10,testMeeting.getPresenterID());
	}
}
