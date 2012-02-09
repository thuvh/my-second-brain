package org.brain2.test.concurency;

import static akka.actor.Actors.remote;

import java.util.Map;

import akka.actor.ActorRef;

public class ActorStarter {
	public void start(){		
		try {
			
			//start slave agents
			ActorStarterUtil.startChildActors();

			ChildActorDefinition actorDef = new ChildActorDefinition();
			actorDef.setHost("localhost");
			actorDef.setPort(2552);
			actorDef.addActorService("hello-service", "org.brain2.test.concurency.MasterActor" );
			Map<String, ActorRef > map = actorDef.getActorServices();
			
			remote().start("localhost", 2552).register("hello-service",map.get("hello-service"));
			
			// client code
			ActorRef actor = remote().actorFor("hello-service", "localhost", 2552);
			Object res = actor.ask("is master loaded ?").get();
			System.out.println(res);
			

			
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ActorStarter masterActor = new ActorStarter();
		masterActor.start();
	}
}
