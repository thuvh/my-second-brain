package org.brain2.crawler.core.actors;

import static akka.actor.Actors.remote;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class ParsingActor extends UntypedActor {
	static Logger logger = Logger.getLogger(ParsingActorService.class);
	public void onReceive(final Object msg) {
		logger.info("ParsingActorService msg: " + msg);
		
		try {
			String html = msg.toString();
			if( ! html.isEmpty() ){
				String content = ArticleExtractor.INSTANCE.getText(html);
				logger.info("#content: \n " + content + "\n");
				
				getContext().tryReply("Parsed");
			}
			if(msg.equals("exit")){
				logger.info("exit received ");
				getContext().tryReply("Shutting down ...ParsingActorService" );
				remote().shutdown();
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.exit(1);							
					}
				}).start();
			} else {
				getContext().tryReply(msg + " from ParsingActorService");
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}
}