package test.java.com.hexcore.vrmeeting_hostserver.dataserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import main.java.com.hexcore.vrmeeting_hostserver.dataserver.JSONParser;
import main.java.com.hexcore.vrmeeting_hostserver.exception.MalformedJSONException;

public class TestJSONParser {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void parserThrowsExceptionWhenFirstCurlyBracketMissing() throws MalformedJSONException
	{
		String badJSON = "\"hi\":\"hello\"}";
		thrown.expect(MalformedJSONException.class);
		JSONParser.parseJSON(badJSON);
	}
	
	@Test
	public void parserThrowsExceptionWhenLastCurlyBracketMissing() throws MalformedJSONException
	{
		String badJSON = "{\"hi\":\"hello\"";
		thrown.expect(MalformedJSONException.class);
		JSONParser.parseJSON(badJSON);
	}
	
	@Test
	public void parserThrowsExceptionWhenColonMissing() throws MalformedJSONException
	{
		String badJSON = "{\"hi\"\"hello\"}";
		thrown.expect(MalformedJSONException.class);
		JSONParser.parseJSON(badJSON);
	}
	
	@Test
	public void parserParsesJSONWithoutSpeechMarksAroundKey() throws MalformedJSONException
	{
		String goodJSON = "{hi:\"hello\"}";
		JSONParser.parseJSON(goodJSON);
	}
	
	@Test
	public void parserParsesJSONWithoutSpeechMarksAroundValue() throws MalformedJSONException
	{
		String goodJSON = "{\"hi\":hello}";
		JSONParser.parseJSON(goodJSON);
	}
	
	@Test
	public void parserParsesJSONContainingMultipleEntries() throws MalformedJSONException
	{
		String key = "key";
		String value = "value";
		String key2 = "key2";
		String value2 = "value2";
		String JSON = "{\"" + key + "\":\"" + value + "\",\"" + key2 + "\":\"" + value2 +"\"}";
		Map<String,String> pairs = JSONParser.parseJSON(JSON);
		assertEquals("2 pairs of key-value pairs were not returned from the parser in the map",2,pairs.size());
		assertTrue("The first key is not in the map returned from the parser",pairs.containsKey(key));
		assertTrue("The second key is not in the map returned from the parser",pairs.containsKey(key2));
		assertEquals("The value assigned to the first key does not match the original value",pairs.get(key),value);
		assertEquals("The value assigned to the second key does not match the original second value",pairs.get(key2),value2);

	}
	
	@Test
	public void parserParsesJSONWithSingleEntry() throws MalformedJSONException
	{
		String key = "key";
		String value = "value";
		String JSON = "{\"" + key + "\":\"" + value + "\"}";
		Map<String,String> pair = JSONParser.parseJSON(JSON);
		assertEquals("The key value pair was not present in the map returned from the parser",pair.size(),1);
		assertTrue("The original key is not in the map returned from the parser",pair.containsKey(key));
		assertEquals("The value assigned to the key does not match the original value",pair.get(key),value);
	}
}
