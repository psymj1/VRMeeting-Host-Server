package test.java.com.hexcore.vrmeeting_hostserver.mock;

import java.util.ArrayList;

import main.java.com.hexcore.vrmeeting_hostserver.connection.Connection;
import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;

/**
 * Class to be implemented during testing to try and make the test classes easier to read
 * Provides an internal buffer of packets sent via {@link #sendPacket(byte[])}
 * @author Psymj1 (Marcus)
 *
 */
public abstract class MockConnection extends Connection {
	
	private ArrayList<byte[]> sentPackets = new ArrayList<byte[]>();

	@Override
	protected void send(byte[] packet) throws ConnectionErrorException {
		byte[] clonePacket = packet.clone();
		synchronized(sentPackets)
		{
			sentPackets.add(clonePacket);
		}
	}
	
	/**
	 * Retrieves all packets sent via {@link #sendPacket(byte[])}
	 * @return All packets that have been sent or null if no packets have been sent
	 */
	public byte[][] getAllSentPackets()
	{
		synchronized(sentPackets)
		{
			if(sentPackets.isEmpty())
			{
				return null;
			}
			
			byte[][] packets = sentPackets.toArray(new byte[0][0]);
			sentPackets.clear();
			return packets;
		}
	}

	@Override
	protected void onClose() {
		//Do nothing because there's nothing to close
	}
}
