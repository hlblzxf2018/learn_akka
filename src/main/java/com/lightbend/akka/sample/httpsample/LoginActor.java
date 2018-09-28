package com.lightbend.akka.sample.httpsample;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.actor.Status;

public class LoginActor extends AbstractLoggingActor {
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(User.class, this::onLogin).matchAny(o -> {
			sender().tell(new Status.Failure(new ClassNotFoundException()), self());
		}).build();
	}

	private final String	username;
	private final String	password;



	public LoginActor(String password, String username) {
		this.username = username;
		this.password = password;
	}



	private void onLogin(User user) {
		if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
			log().info("login success");
			sender().tell(new Status.Success("success"), self());
		} else {
			log().info("login failed");
            sender().tell(new Status.Success("login failed"), self());
		}
	}



	public static Props props(String password, String username) {
		return Props.create(LoginActor.class, password, username);
	}
}
