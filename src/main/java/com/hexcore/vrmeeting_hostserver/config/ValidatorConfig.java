package main.java.com.hexcore.vrmeeting_hostserver.config;

/**
 * Class to hold the config for {@link main.java.com.hexcore.vrmeeting_hostserver.connection.validation.ConnectionValidator}
 * @author Marcus
 *
 */
public final class ValidatorConfig {
	@Deprecated
	public static final String WEB_SERVER_IP = "localhost";
	@Deprecated
	public static final int WEB_SERVER_PORT = 25561;
	public static final int RESPONSE_TIMEOUT = 1000; //In ms, The maximum time waited for a client to respond before the connection is closed
	public static final int POLL_RATE = 10; // In ms, how often the validator checks to see if the client has responded
}
