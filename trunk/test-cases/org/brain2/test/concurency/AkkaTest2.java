package org.brain2.test.concurency;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;

import org.apache.log4j.Logger;
import org.brain2.ws.core.utils.HttpClientUtil;
import org.brain2.ws.core.utils.Log;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class AkkaTest2 {
	
	static Logger logger = Logger.getLogger(AkkaTest2.class);
	
	public static void main(String[] args) {
		Log.setPropertyConfiguratorLog4J(AkkaTest2.class.getSimpleName());		
		
		final RemoteServerModule serverModule = remote().start("localhost", 2553);		
		class HelloWorldActor2 extends UntypedActor {
			public void onReceive(final Object msg) {
//				System.out.println("HelloWorldActor2 tryReply: "+msg);
				
				try {
					if(msg.toString().startsWith("http")){
						String html = HttpClientUtil.executeGet(msg.toString());
						getContext().tryReply(html);
					}
					
					System.out.println("msg: " + msg);
					if(msg.equals("exit")){
						System.out.println("exit received ");
						getContext().tryReply("I'm die");
						remote().shutdown();
						new Thread(new Runnable() {
							@Override
							public void run() {
								System.exit(1);							
							}
						}).start();
					} else {
						getContext().tryReply(msg + " from HelloWorldActor2");
					}
				} catch (Exception e) {
					logger.info(e);
				}
			}
		}				
		serverModule.register("hello-service2",actorOf(HelloWorldActor2.class));
		
		ActorRef actor2 = remote().actorFor("hello-service", "localhost", 2552);
		Object res2 = actor2.ask("AkkaTest2_started").get();
		logger.info(res2);
	}
}
