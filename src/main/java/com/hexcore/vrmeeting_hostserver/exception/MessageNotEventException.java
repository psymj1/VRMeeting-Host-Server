package main.java.com.hexcore.vrmeeting_hostserver.exception;

import main.java.com.hexcore.vrmeeting_hostserver.meeting.event.MessageEventParser;

/**
 * Thrown by the {@link MessageEventParser} when {@link MessageEventParser#parseMessageToEvent(main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message)} is called with an invalid message
 * @author Psymj1 (Marcus)
 *
 */
@SuppressWarnings("serial")
public class MessageNotEventException extends Exception {

}
