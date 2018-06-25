package main.java.com.hexcore.vrmeeting_hostserver.exception;

/**
 * Thrown if there is an error during calling start on a server component
 * @author Psymj1 (Marcus)
 *
 */
@SuppressWarnings("serial")
public class StartupException extends Exception {
	public StartupException(String s)
	{
		super(s);
	}
}
