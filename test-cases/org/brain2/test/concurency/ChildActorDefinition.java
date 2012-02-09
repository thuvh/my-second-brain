package org.brain2.test.concurency;

import static akka.actor.Actors.actorOf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import akka.actor.ActorRef;

public class ChildActorDefinition {
	private String host;
	private int port;
	private Map<String, String> actorServices = new HashMap<String, String>();
	
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Map<String, ActorRef > getActorServices() {
		Map<String, ActorRef > map = new HashMap<String, ActorRef >();
		Set<String> keys = this.actorServices.keySet();
		ClassLoader classLoader = ActorRef.class.getClassLoader();
		for (String k : keys) {			
			try {
				Class actorClass = classLoader.loadClass(this.actorServices.get(k));
				map.put(k, actorOf(actorClass));
			} catch (ClassNotFoundException e) {				
				e.printStackTrace();
			}	
		}
		return map;
	}	

	public void setActorServices(Map<String, String> actorServices) {
		this.actorServices = actorServices;
	}
	
	public void addActorService(String key, String className) {
		this.actorServices.put(key, className);
	}

}
