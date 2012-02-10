package org.brain2.test.concurency;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.remote;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class AkkaTest3 {
	
	static Logger logger = Logger.getLogger(AkkaTest3.class);
	
	public static void main(String[] args) {
		// Read properties file.
		Properties properties = new Properties();
		try {
			String name = AkkaTest3.class.getName();
		    //properties.load(new FileInputStream(name+".properties"));
			properties.load(new FileInputStream("log4j.properties"));
		    properties.put("log4j.appender.rollingFile.File", name+".log");
		} catch (IOException e) {
		}
		PropertyConfigurator.configure(properties);
		
		final RemoteServerModule serverModule = remote().start("localhost", 2554);
		
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
				
		serverModule.register("hello-service3",actorOf(HelloWorldActor2.class));
		
		ActorRef actor2 = remote().actorFor("hello-service", "localhost", 2552);
		Object res2 = actor2.ask("AkkaTest3_started").get();
		logger.info(res2);
	}
}
