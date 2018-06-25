package test.java.com.hexcore.vrmeeting_hostserver.connection;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.Message;
import main.java.com.hexcore.vrmeeting_hostserver.communication.protocol.MessageGenerator;
import main.java.com.hexcore.vrmeeting_hostserver.connection.ConnectionState;
import main.java.com.hexcore.vrmeeting_hostserver.connection.MessageWriter;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;
import test.java.com.hexcore.vrmeeting_hostserver.mock.ScriptedMockConnection;

public class TestMessageWriter {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void messageShouldBeSentToConnection() throws ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageWriter writer = new MessageWriter(connection);
		Message testMessage = MessageGenerator.generateValidatedMessage();
		writer.sendMessage(testMessage);
		byte[][] sentPackets = connection.getAllSentPackets();
		assertTrue("No packets were received by the connection",sentPackets.length > 0);
		byte[] packetSent = sentPackets[0]; //There should only be 1 sent packet
		byte[] transmittableTestMessage = testMessage.getNewTransmittableMessage(); //Convert the test message to a byte array that would be sent
		assertTrue("The message received by the connection is not the same as that which should have been sent",Arrays.equals(packetSent, transmittableTestMessage));
	}
	
	@Test
	public void sendMessageShouldThrowExceptionIfConnectionInErrorState() throws ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageWriter writer = new MessageWriter(connection);
		connection.setState(ConnectionState.ERROR);
		Message testMessage = MessageGenerator.generateValidatedMessage();
		thrown.expect(ConnectionErrorException.class);
//		thrown.expectMessage("Cannot send " + testMessage.getSignal() + " message to " + connection.getName() + " as it is not open");
		writer.sendMessage(testMessage);
	}
	
	@Test
	public void sendMessageShouldThrowExceptionifConnectionClosed() throws ConnectionErrorException
	{
		ScriptedMockConnection connection = new ScriptedMockConnection();
		MessageWriter writer = new MessageWriter(connection);
		connection.close();
		Message testMessage = MessageGenerator.generateValidatedMessage();
		thrown.expect(ConnectionErrorException.class);
//		thrown.expectMessage("Cannot send " + testMessage.getSignal() + " message to " + connection.getName() + " as it is not open");
		writer.sendMessage(testMessage);
	}
}
