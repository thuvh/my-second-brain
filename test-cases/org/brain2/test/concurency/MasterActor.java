package org.brain2.test.concurency;

import static akka.actor.Actors.remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class MasterActor extends UntypedActor{

	@Override
	public void onReceive(final Object msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("MasterActor: "+msg);
		getContext().tryReply(" Processed done " + msg);
		
		if(msg.equals("AkkaTest2_started") ){
			try {
				ActorRef actor2 = remote().actorFor("hello-service2", "localhost", 2553);
				System.out.println(actor2.getUuid());
				System.out.println(actor2.getId());
				
				Object res2 = actor2.ask("ping hello-service2 ").get();
				System.out.println(res2);
				
//				Object res3 = actor2.ask("exit").get();
//				System.out.println(res3);						
										
				Object res4 = actor2.ask("are you die").get();
				System.out.println(res4);
			} catch (Exception e) {
				if(e instanceof akka.dispatch.FutureTimeoutException){
					System.out.println("[localhost:2553][actor:hello-service2][status:die]");
				} else {
					e.printStackTrace();
					System.err.println(e.getMessage());
				}
			}
			
		} else if(msg.equals("AkkaTest3_started")){
			try {
				ActorRef actor3 = remote().actorFor("hello-service3", "localhost", 2554);
				System.out.println(actor3.getUuid());
				System.out.println(actor3.getId());
				
				Object res2 = actor3.ask("ping hello-service3 ").get();
				System.out.println(res2);	
				
//				Object res3 = actor3.ask("exit").get();
//				System.out.println(res3);
				
										
				Object res4 = actor3.ask("are you die").get();
				System.out.println(res4);
			} catch (Exception e) {
				if(e instanceof akka.dispatch.FutureTimeoutException){
					System.out.println("[localhost:2554][actor:hello-service2][status:die]");
				} else {
					e.printStackTrace();
					System.err.println(e.getMessage());
				}
			}
			
		}	
		
	}
	
	
	

}
