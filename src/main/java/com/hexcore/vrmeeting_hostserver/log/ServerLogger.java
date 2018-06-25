package main.java.com.hexcore.vrmeeting_hostserver.log;

import java.util.Date;

public class ServerLogger {
	private String loggerName;
	public ServerLogger(String name)
	{
		loggerName = name;
	}
	
	public void logInfo(String message)
	{
		System.out.println("INFO " + getPrefix() + " " + message);
	}
	
	public void logWarning(String message)
	{
		System.out.println("WARNING " + getPrefix() + " " + message);
	}
	
	public void logError(String errorMessage,Exception e)
	{
		System.err.println("ERROR " + getPrefix() + " " + errorMessage);
		System.err.println(e.getStackTrace());
	}
	
	public void logError(String errorMessage)
	{
		System.err.println("ERROR " + getPrefix() + " " + errorMessage);
	}
	
	public void logIgnore(String message)
	{
		System.out.println("IGNORE " + getPrefix() + " " + message);
	}
	
	private String getPrefix()
	{
		Date date = new Date();
		String dateString = "<" + date.toString() + "> (" + loggerName + ")";
		return dateString;
	}
	
	public String getName()
	{
		return loggerName;
	}
}
