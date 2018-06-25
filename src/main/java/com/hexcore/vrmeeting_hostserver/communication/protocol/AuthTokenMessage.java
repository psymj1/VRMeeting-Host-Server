/**
 * 
 */
package main.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;

/**
 * This class aims to provide additional power to the {@linkplain main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message} Class Allowing the Authentication Token encoded into the payload to be extracted
 * @author Psymj1 (Marcus)
 *
 */
public class AuthTokenMessage extends Message {
	
	public AuthTokenMessage(byte[] payload) {
		super(ClientSignals.TOKE.toString(), payload);
	}
	
	public String getAuthenticationToken()
	{
		return new String(getPayload(),ProtocolConfig.AUTHENTICATION_TOKEN_CHARACTER_SET);
	}
}
