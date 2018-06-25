/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.dataserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidMeetingIDException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.InvalidTokenException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.MalformedJSONException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ServerUnavailableException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;
import main.java.com.hexcore.vrmeeting_hostserver.user.User;

/**
 * A class which connects to the VRMeeting Web Server to validate users and retrieve their data
 * @author Psymj1 (Marcus)
 *
 */
public class WebServerConnector implements DataServerConnector {
	private String baseUrl;
	private static ServerLogger logger;
	
	/**
	 * 
	 * @param baseURL The base url of the web server to connect to. Example: http://localhost:25560
	 * @throws IOException If there is
	 */
	public WebServerConnector(String baseURL) throws IOException{
		this.baseUrl = "http://" + baseURL;
		logger = new ServerLogger("WebServerConnector to " + baseURL);
		logger.logInfo("Attempting to communicate with WebServer at " + baseURL);
		if(!isAvailable())
		{
			throw new IOException("Could not connect to web server at " + baseURL);
		}
	}
	
	/**
	 * @see vrmeeting.connection.validation.DataServerConnector#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		HttpURLConnection connection = null;
		
		try
		{
			URL serverurl = new URL(baseUrl);
			connection = (HttpURLConnection)serverurl.openConnection();
			connection.getResponseCode();
			return true;
		}catch(IOException i)
		{
			logger.logIgnore(i.getMessage());
			return false;
		}finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
	}

	/**
	 * @see vrmeeting.connection.validation.DataServerConnector#retrieveUserData(java.lang.String)
	 */
	@Override
	public User retrieveUserData(String token) throws InvalidTokenException, ServerUnavailableException {
		if(isAvailable())
		{
			HttpURLConnection connection = null;
			try
			{
				URL serverurl = new URL(baseUrl + "/user/token");
				connection = (HttpURLConnection)serverurl.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Authorization",token);
				int status = connection.getResponseCode();
				
				switch(status)
				{
				case HttpURLConnection.HTTP_OK:
					Map<String,String> userFields = JSONParser.parseJSON(readConnectionOutput(connection));
					return generateUserFromResponeFields(userFields);
				case HttpURLConnection.HTTP_UNAUTHORIZED:
					throw new InvalidTokenException();
				default:
					logger.logError("Unexpected response code " + status + " from web server");
					return null;
				}
			}catch(IOException | MalformedJSONException i)
			{
				logger.logError("Error occurred while attempting to retrieve user data:" + i.getMessage(),i);
				throw new ServerUnavailableException();
			}finally
			{
				if(connection != null)
				{
					connection.disconnect();
				}
			}
		}else
		{
			throw new ServerUnavailableException();
		}
	}
	
	private User generateUserFromResponeFields(Map<String,String> userFields)
	{
		int userID = Integer.valueOf(userFields.get("userid"));
		String firstname = userFields.get("firstname");
		String surname = userFields.get("surname");
		String company = userFields.get("company");
		String jobtitle = userFields.get("jobtitle");
		String workemail = userFields.get("workemail");
		String phonenum = userFields.get("phonenum");
		int avatarid = Integer.valueOf(userFields.get("avatarid"));
		return new User(userID,firstname,surname,company,jobtitle,workemail,phonenum,avatarid);
	}
	
	private String readConnectionOutput(HttpURLConnection connection) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String input;
		StringBuffer content = new StringBuffer();
		while((input = in.readLine()) != null)
		{
			content.append(input);
		}
		in.close();
		connection.disconnect();
		String contentString = content.toString();
		in.close();
		return contentString;
	}

	/**
	 * @see vrmeeting.connection.validation.DataServerConnector#retreiveMeetingData(java.lang.String)
	 */
	@Override
	public Meeting retreiveMeetingData(String meetingID) throws InvalidMeetingIDException, ServerUnavailableException {
		if(isAvailable())
		{
			HttpURLConnection connection = null;
			try
			{
				URL serverurl = new URL(baseUrl + "/meeting/presenter?MeetingCode=" + meetingID);
				connection = (HttpURLConnection)serverurl.openConnection();
				connection.setRequestMethod("GET");
				int status = connection.getResponseCode();
				
				switch(status)
				{
				case HttpURLConnection.HTTP_OK:
					Map<String,String> userFields = JSONParser.parseJSON(readConnectionOutput(connection));
					Meeting meeting = new Meeting(meetingID);
					meeting.setPresenterUID(Integer.valueOf(userFields.get("presenter_id")));
					return meeting;
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new InvalidMeetingIDException();
				default:
					logger.logError("Unexpected response code " + status + " from web server");
					return null;
				}
			}catch(IOException | MalformedJSONException i)
			{
				logger.logError("Error occurred while attempting to retrieve meeting data:" + i.getMessage(),i);
				throw new ServerUnavailableException();
			}finally
			{
				if(connection != null)
				{
					connection.disconnect();
				}
			}
		}else
		{
			throw new ServerUnavailableException();
		}
	}

}
