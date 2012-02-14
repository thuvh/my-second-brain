package org.brain2.crawler.core.actors;

import static akka.actor.Actors.remote;

import org.apache.log4j.Logger;
import org.brain2.ws.core.utils.HttpClientUtil;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class DownloaderActor extends UntypedActor {
	static Logger logger = Logger.getLogger(DownloadActorService.class);
	public void onReceive(final Object msg) {
		logger.info("DownloadActorService msg: " + msg);
		
		try {
			if(msg.toString().startsWith("http")){
				String html = HttpClientUtil.executeGet(msg.toString());
				logger.info("Downloaded");
				ActorRef actor = remote().actorFor("ParsingActorService", "localhost", 20002);
				Object res = actor.ask(html).get();	
				logger.info(res);
				getContext().tryReply("Downloaded");
			}
			if(msg.equals("exit")){
				logger.info("exit received ");
				getContext().tryReply("Shutting down DownloadActorService..." );
				remote().shutdown();
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.exit(1);							
					}
				}).start();
			} else {
				getContext().tryReply(msg + " from DownloadActorService");
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}
}