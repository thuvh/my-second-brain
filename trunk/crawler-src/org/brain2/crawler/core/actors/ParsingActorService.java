package org.brain2.crawler.core.actors;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;

import org.apache.log4j.Logger;
import org.brain2.ws.core.utils.Log;

import akka.remoteinterface.RemoteServerModule;

public class ParsingActorService {
	static Logger logger = Logger.getLogger(ParsingActorService.class);
	
	public static void main(String[] args) {
		Log.setPropertyConfiguratorLog4J(ParsingActorService.class.getSimpleName());
		
		String hostname = "localhost";
		int port = 20002;
		if(args.length == 2){
			hostname = args[0];
			port = Integer.parseInt(args[1]);
		}
		final RemoteServerModule serverModule = remote().start(hostname, port);
		
		
		serverModule.register("ParsingActorService",actorOf(ParsingActor.class));
		
	}
	

}
