package main.java.com.hexcore.vrmeeting_hostserver.dataserver;

import java.util.HashMap;
import java.util.Map;

import main.java.com.hexcore.vrmeeting_hostserver.exception.MalformedJSONException;

/**
 * A utility class used to parse the JSON responses from a web server to key value maps
 * @author Psymj1 (Marcus)
 *
 */
public class JSONParser {
	private JSONParser(){}
	
	/**
	 * Converts a String containing JSON to a map containing the key value pairs
	 * Note this can only convert single level json objects and does not support imbedded objects
	 * @param jsonString The JSON to parse
	 * @return The map of key value pairs that was stored in the JSON
	 */
	public static Map<String,String> parseJSON(String jsonString) throws MalformedJSONException
	{
		if( jsonString.indexOf('{') == -1)
		{
			throw new MalformedJSONException("Missing opening curly bracket \"{\"");
		}
		
		if(jsonString.indexOf("}") == -1)
		{
			throw new MalformedJSONException("Missing closing curly bracket \"}\"");
		}
		
		Map<String,String> map = new HashMap<String,String>();
		
		String json = jsonString.substring(1); //Remove first curly bracket
		json = json.substring(0,json.length()-1); //Remove end curly bracket
		String[] pairs = json.split(","); //Split the string into key value pairs in the format key:value
		for(String keyvalue : pairs)
		{
			String[] pair = keyvalue.split(":"); //Break the key:value pair into two values
			if(pair.length != 2)
			{
				throw new MalformedJSONException("Missing : separating a key value pair");
			}
			//If either of the pair start with and end with speech marks remove them
			for(int i = 0;i < 2;i++)
			{
				String s = pair[i];
				if(s.substring(0, 1).equals("\"") && s.substring(s.length()-1).equals("\""))
				{
					s = s.substring(1);
					s = s.substring(0,s.length()-1);
				}
				pair[i] = s;
			}
			
			map.put(pair[0], pair[1]);
		}
		return map;
	}
}
