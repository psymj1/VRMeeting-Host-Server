package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;

public class MeetingIDMessage extends Message {

	public MeetingIDMessage(byte[] payload) {
		super(ClientSignals.MID.toString(), payload);
	}
	
	public String getMeetingID()
	{
		return new String(getPayload(),ProtocolConfig.MEETING_ID_CHARACTER_SET);
	}

}
