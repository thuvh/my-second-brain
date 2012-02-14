package org.brain2.crawler.core.actors;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;

import org.apache.log4j.Logger;
import org.brain2.ws.core.utils.Log;

import akka.remoteinterface.RemoteServerModule;

public class DownloadActorService {

	static Logger logger = Logger.getLogger(DownloadActorService.class);
	
	public static void main(String[] args) {
		Log.setPropertyConfiguratorLog4J(DownloadActorService.class.getSimpleName());
		
		String hostname = "localhost";
		int port = 20001;
		if(args.length == 2){
			hostname = args[0];
			port = Integer.parseInt(args[1]);
		}
		final RemoteServerModule serverModule = remote().start(hostname, port);
		
		serverModule.register("DownloadActorService",actorOf(DownloaderActor.class));
		
		remote().actorFor("CrawlerActorService", "localhost", 20000).ask("DownloadActorService.started");	
		
	}
}
