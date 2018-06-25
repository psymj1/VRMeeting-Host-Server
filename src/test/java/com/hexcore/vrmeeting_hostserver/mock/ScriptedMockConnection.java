package test.java.com.hexcore.vrmeeting_hostserver.mock;

import java.util.HashMap;

import main.java.com.hexcore.vrmeeting_hostserver.exception.ConnectionErrorException;

/**
 * A specific implementation of a MockConnection which has a different number of responses which will be returned using receive, or not at all in the case that willReadBlock returns true
 * When a response is read i.e {@link #receiveNextPacket()} is called, the response number is incremented by 1
 * @author Psymj1 (Marcus)
 */
public class ScriptedMockConnection extends MockConnection {
		private int responseNumber = 0;
		private HashMap<Integer,byte[]> responses = new HashMap<Integer,byte[]>();
		
		/**
		 * Add a new response so that when the response number of the connection is set to x, the associated packet is returned
		 * @param responseNumber The response number for the packet
		 * @param packet The packet to return
		 */
		public void addResponse(int responseNumber,byte[] packet)
		{
			responses.put(responseNumber, packet);
		}
		
		@Override
		protected byte[] receive() throws ConnectionErrorException {
			if(responses.containsKey(responseNumber))
			{
				int response = responseNumber;
				responseNumber++;
				return responses.get(response);
			}else
			{
				return null;
			}
		}

		@Override
		protected boolean willReadBlock() throws ConnectionErrorException {
			if(responses.containsKey(responseNumber))
			{
				return false;
			}else
			{
				return true;
			}
		}

		@Override
		public String getName() {
			return "A Scripted Connection";
		}
		
		@Override
		public String toString(){
			return getName();
		}
}
