package com.lightbend.akka.sample.lifecycle;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class First_actor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("stop", s -> getContext().stop(getSelf())).build();
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("first start");
        ActorRef second_actor = getContext().actorOf(Props.create(Second_actor.class),"second_actor");
        System.out.println("second_actor: " + second_actor);
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("first stop");
    }
}
