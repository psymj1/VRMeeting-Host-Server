package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal;

/**
 * Contains Signals that will be contained within messages sent from clients to the server based on the VRMeeting Messaging Protocol
 * @author Psymj1 (Marcus)
 * @see <a href="https://github.com/psymj1/VRMeeting-Documentation">VRMeeting Messaging Protocol under 'Developer Documentation'</a>
 */
public enum ClientSignals {
	TOKE,
	CHNG,
	AUDI,
	END,
	MID,
	GAP,
	HERE,
	GONE,
	HRTB,
	EAUD
}
