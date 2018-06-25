package main.java.com.hexcore.vrmeeting_hostserver.user;

import java.util.Objects;

public class User {
	
	private String firstName;
	private String surName;
	private String company;
	private String jobTitle;
	private String workEmail;
	private String phoneNumber;
	private int avatarID;
	private boolean presenting;
	private String meetingCode;
	private int userID;
	
	public User(int userID,String firstName,String surName,String company,String jobTitle,String workEmail,String phoneNumber,int avatarID)
	{
		this.userID = userID;
		this.firstName = firstName;
		this.surName = surName;
		this.company = company;
		this.jobTitle = jobTitle;
		this.workEmail = workEmail;
		this.phoneNumber = phoneNumber;
		this.avatarID = avatarID;
	}
	
	public int getUserID(){
		return userID;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSurName() {
		return surName;
	}

	public String getCompany() {
		return company;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public String getWorkEmail() {
		return workEmail;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public int getAvatarID() {
		return avatarID;
	}
	
	public boolean isPresenter()
	{
		return presenting;
	}
	
	public String getMeetingID()
	{
		return meetingCode;
	}

	public void setPresenting(boolean presenting) {
		this.presenting = presenting;
	}

	public void setMeetingCode(String meetingCode) {
		this.meetingCode = meetingCode;
	}
	
	@Override
	public boolean equals(Object toCompare)
	{
		if(toCompare.getClass() == this.getClass())
		{
			User comparisonUser = (User)toCompare;
			return comparisonUser.userID == this.userID 
					&& comparisonUser.presenting == this.presenting
					&& comparisonUser.avatarID == this.avatarID
					&& comparisonUser.company.equals(this.company)
					&& comparisonUser.firstName.equals(this.firstName)
					&& comparisonUser.jobTitle.equals(this.jobTitle)
					&& comparisonUser.meetingCode.equals(this.meetingCode)
					&& comparisonUser.phoneNumber.equals(this.phoneNumber)
					&& comparisonUser.surName.equals(this.surName)
					&& comparisonUser.workEmail.equals(this.workEmail);
		}
		return false;
	}
	
	/**
	 * Converts all of the values stored in the user to a single String in JSON format
	 */
	public String convertToJSON()
	{
		String json = "{";
		json += "\"userID\":" + userID + ",";
		json += "\"firstName\":" + "\"" + firstName + "\",";
		json += "\"surName\":" + "\"" + surName + "\",";
		json += "\"company\":" + "\"" + company + "\",";
		json += "\"jobTitle\":" + "\"" + jobTitle + "\",";
		json += "\"workEmail\":" + "\"" + workEmail + "\",";
		json += "\"phoneNumber\":" + "\"" + phoneNumber + "\",";
		json += "\"avatarID\":" + avatarID + ",";
		json += "\"presenting\":" + new Boolean(presenting).toString();
		json += "}";
		return json;
	}
	
	/**
	 * Overridden so that the hash can be used to identify identical users
	 */
	@Override
	public int hashCode(){
		return Objects.hash(userID,avatarID,company,firstName,jobTitle,meetingCode,phoneNumber,surName,workEmail);
	}
}
