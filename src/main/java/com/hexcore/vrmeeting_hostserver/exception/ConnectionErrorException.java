package main.java.com.hexcore.vrmeeting_hostserver.exception;

/**
 * Thrown if there is an error with a {@link main.java.com.hexcore.vrmeeting_hostserver.connection.Connection} when attempting to perform an action
 * @author Psymj1 (Marcus)
 *
 */
@SuppressWarnings("serial")
public class ConnectionErrorException extends Exception {
	
	public ConnectionErrorException(String s)
	{
		super(s);
	}
}
