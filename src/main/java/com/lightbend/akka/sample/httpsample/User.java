package com.lightbend.akka.sample.httpsample;

public class User {
	private final String	password;
	private final String	username;



	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}



	public String getPassword() {
		return password;
	}



	public String getUsername() {
		return username;
	}
}
