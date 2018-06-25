package test.java.com.hexcore.vrmeeting_hostserver.communication.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals;
import main.java.com.hexcore.vrmeeting_hostserver.config.ProtocolConfig;
import test.java.com.hexcore.vrmeeting_hostserver.TestingUtilities;

/**
 * the {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage} Class
 * @author Psymj1 (Marcus)
 *
 */
public class TestAuthTokenMessageClass {
	
	/**
	 * Tests to make sure that a string encoded into the payload is identical to the authentication token returned from {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.AuthTokenMessage#getAuthenticationToken()}
	 */
	@Test
	public void tokenInMessageShouldBeIdenticalToTokenBeforeMessageCreation()
	{
		String token = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE); //Generate a random string the max length of the payload
		AuthTokenMessage message = new AuthTokenMessage(token.getBytes(ProtocolConfig.AUTHENTICATION_TOKEN_CHARACTER_SET));
		assertEquals("The token retrieved from the message payload was not the same as the token that was passed into the constructor",token,message.getAuthenticationToken());
	}
	
	/**
	 * Tests to make sure that when a AuthTokenMessage is created it has the {@link main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.signal.ClientSignals#TOKE} signal
	 */
	@Test
	public void signalInAuthTokenMessageShouldBeTOKE()
	{
		String token = TestingUtilities.GenerateRandomString(ProtocolConfig.MAX_SERIALIZED_PAYLOAD_SIZE); //Generate a random string the max length of the payload
		AuthTokenMessage message = new AuthTokenMessage(token.getBytes(ProtocolConfig.AUTHENTICATION_TOKEN_CHARACTER_SET));
		assertEquals("The signal produced by the constructor for AuthTokenMessage is not " + ClientSignals.TOKE.toString(),ClientSignals.TOKE.toString(),message.getSignal());
	}
}
