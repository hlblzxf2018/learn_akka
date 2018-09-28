package com.lightbend.akka.sample.lifecycle;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Poc_actor_lifecycle {
    public static void main(String[] args){
        ActorSystem system = ActorSystem.create("test_life_cycle");
        ActorRef firstActor = system.actorOf(Props.create(First_actor.class), "firstActor");
        System.out.println("first_Actor: " + firstActor);
        firstActor.tell("stop", ActorRef.noSender());
    }
}
