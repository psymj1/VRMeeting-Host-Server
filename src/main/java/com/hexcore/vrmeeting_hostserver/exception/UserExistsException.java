package main.java.com.hexcore.vrmeeting_hostserver.exception;

import main.java.com.hexcore.vrmeeting_hostserver.meeting.Meeting;

/**
 * Thrown by a {@link Meeting} if a participant tries to join a meeting they're already in
 * @author Psymj1 (Marcus)
 *
 */
@SuppressWarnings("serial")
public class UserExistsException extends Exception {

}
