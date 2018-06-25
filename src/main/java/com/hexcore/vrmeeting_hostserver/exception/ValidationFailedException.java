package main.java.com.hexcore.vrmeeting_hostserver.exception;

import main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ConnectionValidator;

/**
 * Thrown internally in the {@link ConnectionValidator} if one of the following occur:
 * - A message fails to send to the client
 * - The client does not respond in time
 * - Information such as the Authentication Token or MeetingID is not valid
 * - The validation server is unavailable for querying
 * @author Psymj1 (Marcus)
 *
 */
@SuppressWarnings("serial")
public class ValidationFailedException extends Exception {
	/**
	 * 
	 * @param reason The reason why the validation failed
	 */
	public ValidationFailedException(String reason)
	{
		super(reason);
	}
}
