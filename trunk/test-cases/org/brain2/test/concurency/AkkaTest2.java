package org.brain2.test.concurency;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class AkkaTest2 {
	public static void main(String[] args) {
		class HelloWorldActor2 extends UntypedActor {
			public void onReceive(Object msg) {
				System.out.println("HelloWorldActor2 tryReply: "+msg);
				getContext().tryReply(msg + " World");
			}
		}
		remote().start("localhost", 2553).register("hello-service",actorOf(HelloWorldActor2.class));
		
		ActorRef actor2 = remote().actorFor("hello-service", "localhost", 2552);
		Object res2 = actor2.ask("Hello 2 ").get();
		System.out.println(res2);		
				
	}
}
