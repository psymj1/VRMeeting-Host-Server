package test.java.com.hexcore.vrmeeting_hostserver;

/**
 * A utility class used during testing
 * @author Psymj1 (Marcus)
 *
 */
public final class TestingUtilities {
	public static final String ALPHA_NUMERIC_CHARACTERS = "ABCDEFGHIJKLNOPQRSTUVWXYZ1234567890";
	private TestingUtilities(){}
	
	/**
	 * Generates a string of random Alpha-Numeric Characters
	 * @param length The length of the string to generate
	 * @return Returns a String of length <code>length</code> consisting of random Alpha-Numeric Characters
	 */
	public static String GenerateRandomString(int length){
		if(length <= 0){
			throw new IllegalArgumentException("The length cannot be less than or equal to 0");
		}
		String newString = "";
		for(int i = 0;i < length;i++)
		{
			newString += ALPHA_NUMERIC_CHARACTERS.charAt((int)Math.floor((Math.random()*100)) % ALPHA_NUMERIC_CHARACTERS.length());
		}
		return newString;
	}
}
