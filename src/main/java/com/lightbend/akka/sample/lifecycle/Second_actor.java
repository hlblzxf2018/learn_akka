package com.lightbend.akka.sample.lifecycle;

import akka.actor.AbstractActor;

public class Second_actor extends AbstractActor {
    @Override
    public void preStart() throws Exception {
        System.out.println("second start");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("second stop");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
