package org.brain2.crawler.core.actors;

import static akka.actor.Actors.remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class CrawlerActor extends UntypedActor{

	@Override
	public void onReceive(final Object msg) throws Exception {
		
		System.out.println("MasterActor: "+msg);
		
		if("DownloadActorService.started".equals(msg)){
			// client code
			ActorRef actor = remote().actorFor("DownloadActorService", "localhost", 20001);
			Object res = actor.ask("http://chungta.vn/tin-tuc/giai-tri/2012/02/nhung-ca-khuc-lang-man-trong-mua-valentine/").get();
			System.out.println(res);
		} else {		
			getContext().tryReply(" Processed done " + msg);
		}
		
		
	}
	
	
	

}
