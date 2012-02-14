package org.brain2.crawler.core.actors;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;

public class CrawlerActorService {
	
	public void start(){		
		try {			
			//start slave agents
			ActorStarterUtil.startChildActors();			
			remote().start("localhost", 20000).register("CrawlerActorService", actorOf(CrawlerActor.class) );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		CrawlerActorService masterActor = new CrawlerActorService();
		masterActor.start();
	}
}
