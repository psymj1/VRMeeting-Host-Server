package test.java.com.hexcore.vrmeeting_hostserver.user;

import static org.junit.Assert.*;

import org.junit.Test;

import main.java.com.hexcore.vrmeeting_hostserver.user.User;

public class TestUser {
	private User testValidUser = new User(1,"FirstName","SurName","CompanyName","JobTitle","WorkEmail","01234567891",1);
	
	@Test
	public void convertToJSONReturnsCorrectJSON()
	{
		testValidUser.setPresenting(true);
		String actualJson = "{\"userID\":1," + "\"firstName\":\"FirstName\"," + "\"surName\":\"SurName\"," + "\"company\":\"CompanyName\"," + "\"jobTitle\":\"JobTitle\"," + "\"workEmail\":\"WorkEmail\"," + "\"phoneNumber\":\"01234567891\"," + "\"avatarID\":1," + "\"presenting\":true}";
		assertEquals("The json generated in the user class did not match the json generated manually in the test",actualJson,testValidUser.convertToJSON());
	}
}
