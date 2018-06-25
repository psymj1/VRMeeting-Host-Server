package main.java.com.hexcore.vrmeeting_hostserver.connection.validation;

import main.java.com.hexcore.vrmeeting_hostserver.ServerComponent;
import main.java.com.hexcore.vrmeeting_hostserver.ServerComponentState;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.dataserver.DataServerConnector;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.StartupException;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ValidationFailedException;
import main.java.com.hexcore.vrmeeting_hostserver.log.ServerLogger;
import main.java.com.hexcore.vrmeeting_hostserver.meeting.MeetingClient;

/**
 * A class which will create a new thread to run validation on a given connection in
 * It will also send a VAL or NVAL message to the connection if it passes or fails validation respectively
 * @author Psymj1 (Marcus)
 *
 */
public class ValidationThread extends ServerComponent{	
	private Connection connection;
	private DataServerConnector dataServer;
	private ValidConnectionOutput output;
	private ServerLogger logger;
	
	/**
	 * @param connection The connection run through the {@link ConnectionValidator}
	 * @param dataServer The data server to use when looking for information on the user on the other end of the connection
	 * @param output The class to output the MeetingClient of the valid connection to
	 */
	public ValidationThread(ServerComponent parent,Connection connection,DataServerConnector dataServer,ValidConnectionOutput output)
	{
		super(parent);
		this.connection = connection;
		this.dataServer = dataServer;
		this.output = output;
	}
	
	@Override
	protected void startUp() throws StartupException {
		logger = new ServerLogger("Validation Thread for " + connection.getName());
		logger.logInfo("Starting validation");
		InternalThread internal = new InternalThread();
		internal.start();
	}

	@Override
	protected void shutdown() {
		
	}
	
	private class InternalThread extends Thread{
		@Override
		public void run()
		{
			try
			{
				MeetingClient client = validate();
				try {
					connection.sendPacket(MessageGenerator.generateValidatedMessage().getNewTransmittableMessage());
					output.connectionValidationOutput(client);
					logger.logInfo("Connection successfully validated");
				} catch (ConnectionErrorException e) {
					logger.logInfo("Failed to send VAL message");
				}
			}catch(ValidationFailedException v)
			{
				logger.logWarning(v.getMessage());
				try {
					connection.sendPacket(MessageGenerator.generateNotValidatedMessage().getTransmittableMessage());
					connection.close();
				} catch (ConnectionErrorException e) {
					logger.logIgnore("Failed to send NVAL message");
				}
			}
			setState(ServerComponentState.STOPPED);
			if(getParentComponent() != null)
			{
				getParentComponent().removeSubServerComponent(ValidationThread.this);
			}
			logger.logInfo("Validation thread stopped");
		}
	}
	
	/**
	 * Performs validation on the stored details from the constructor. If this method is executed then the validation will not occur in a separate thread.
	 * {@link #start()} must be called for the validation to occur in a separate thread
	 * @throws ValidationFailedException Thrown if there is an error during validation or if it fails
	 * @return The meeting client containing the user information and connection retrieved during validation
	 */
	public MeetingClient validate() throws ValidationFailedException
	{
		return ConnectionValidator.validateConnection(connection, dataServer);
	}

}
