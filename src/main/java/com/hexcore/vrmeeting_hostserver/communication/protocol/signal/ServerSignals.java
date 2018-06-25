package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal;

/**
 * Contains signals that will be in messages sent from the server to a client based on the VRMeeting Messaging Protocol
 * @author Psymj1 (Marcus)
 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol under 'Developer Documentation'</a>
 */
public enum ServerSignals {
	AUTH,
	VAL,
	NVAL,
	CHNG,
	AUDI,
	EAUD,
	END,
	MEET,
	UDM,
	LEFT
}
