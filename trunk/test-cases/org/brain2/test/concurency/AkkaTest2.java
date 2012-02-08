package org.brain2.test.concurency;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class AkkaTest2 {
	public static void main(String[] args) {
		
		final RemoteServerModule serverModule = remote().start("localhost", 2553);
		
		class HelloWorldActor2 extends UntypedActor {
			public void onReceive(final Object msg) {
//				System.out.println("HelloWorldActor2 tryReply: "+msg);
				
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
			}
		}
				
		serverModule.register("hello-service2",actorOf(HelloWorldActor2.class));
		
		ActorRef actor2 = remote().actorFor("hello-service", "localhost", 2552);
		Object res2 = actor2.ask("AkkaTest2_started").get();
		System.out.println(res2);
	}
}
