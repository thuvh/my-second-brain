package org.brain2.test.concurency;

import static akka.actor.Actors.actorOf;
import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.brain2.test.utils.JavaClasspathUtil;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.CyclicIterator;
import akka.routing.InfiniteIterator;
import akka.routing.Routing.Broadcast;
import akka.routing.UntypedLoadBalancer;

public class AkkaTest {

	public static void main(String[] args) throws Exception {
		AkkaTest pi = new AkkaTest();
		pi.calculate(4, 10000, 10000);
		
		
		// server code
		class HelloWorldActor1 extends UntypedActor {
			public void onReceive(Object msg) {
				System.out.println("HelloWorldActor1 tryReply: "+msg);
				getContext().tryReply(msg + " World");
				
				if(msg.equals("AkkaTest2_started")){
					try {
						ActorRef actor2 = remote().actorFor("hello-service2", "localhost", 2553);
						System.out.println(actor2.getUuid());
						System.out.println(actor2.getId());
						
						
						Object res2 = actor2.ask("ping hello-service2 ").get();
						System.out.println(res2);	
						
						Object res3 = actor2.ask("exit").get();
						System.out.println(res3);
						
												
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
					
				}				
			}
		}			
		
		remote().start("localhost", 2552).register("hello-service",actorOf(HelloWorldActor1.class));
		

		// client code
		ActorRef actor = remote().actorFor("hello-service", "localhost", 2552);
		Object res = actor.ask("Self ping Hello 1 ").get();
		System.out.println(res);
		
		
		//start slave agents
		int min = 256, max = 1024;
		if(args.length == 2){
			min = Integer.parseInt(args[0]);
			max = Integer.parseInt(args[1]);
		}		
		JavaClasspathUtil util = new JavaClasspathUtil();
		File currentFolder = new File("");
		ArrayList<String> jarFileNames = util.lookupFiles(new JavaClasspathUtil.FileNameFilter() {			
			@Override
			public boolean check(String name) {			
				//System.out.println(name);
				return name.startsWith("agent-slave-") && name.endsWith(".jar");
			}
		},currentFolder.getAbsolutePath(), "", false);			
		for (String jarFileName : jarFileNames) {			
			String cmd = "java -jar -Xms"+min+"m -Xmx"+max+"m -XX:-UseParallelGC " + jarFileName;
			System.out.println("... exec: " + cmd);
			Runtime.getRuntime().exec(cmd);
		}
		
			
				
		//System.exit(1);

	}

	// ====================
	// ===== Messages =====
	// ====================
	static class Calculate {
	}

	static class Work {
		private final int start;
		private final int nrOfElements;

		public Work(int start, int nrOfElements) {
			this.start = start;
			this.nrOfElements = nrOfElements;
		}

		public int getStart() {
			return start;
		}

		public int getNrOfElements() {
			return nrOfElements;
		}
	}

	static class Result {
		private final double value;

		public Result(double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}
	}

	// ==================
	// ===== Worker =====
	// ==================
	static class Worker extends UntypedActor {

		// define the work
		private double calculatePiFor(int start, int nrOfElements) {
			double acc = 0.0;
			for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
				acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
			}
			return acc;
		}

		// message handler
		public void onReceive(Object message) {
			if (message instanceof Work) {
				Work work = (Work) message;

				// perform the work
				double result = calculatePiFor(work.getStart(),
						work.getNrOfElements());

				// reply with the result
				getContext().reply(new Result(result));

			} else
				throw new IllegalArgumentException("Unknown message ["
						+ message + "]");
		}
	}

	// ==================
	// ===== Master =====
	// ==================
	static class Master extends UntypedActor {
		private final int nrOfMessages;
		private final int nrOfElements;
		private final CountDownLatch latch;

		private double pi;
		private int nrOfResults;
		private long start;

		private ActorRef router;

		static class PiRouter extends UntypedLoadBalancer {
			private final InfiniteIterator<ActorRef> workers;

			public PiRouter(ActorRef[] workers) {
				this.workers = new CyclicIterator<ActorRef>(asList(workers));
			}

			public InfiniteIterator<ActorRef> seq() {
				return workers;
			}
		}

		public Master(int nrOfWorkers, int nrOfMessages, int nrOfElements,
				CountDownLatch latch) {
			this.nrOfMessages = nrOfMessages;
			this.nrOfElements = nrOfElements;
			this.latch = latch;

			// create the workers
			final ActorRef[] workers = new ActorRef[nrOfWorkers];
			for (int i = 0; i < nrOfWorkers; i++) {
				workers[i] = actorOf(Worker.class).start();
			}

			// wrap them with a load-balancing router
			router = actorOf(new UntypedActorFactory() {
				public UntypedActor create() {
					return new PiRouter(workers);
				}
			}).start();
		}

		// message handler
		public void onReceive(Object message) {

			if (message instanceof Calculate) {
				// schedule work
				for (int start = 0; start < nrOfMessages; start++) {
					router.tell(new Work(start, nrOfElements), getContext());
				}

				// send a PoisonPill to all workers telling them to shut down
				// themselves
				router.tell(new Broadcast(poisonPill()));

				// send a PoisonPill to the router, telling him to shut himself
				// down
				router.tell(poisonPill());

			} else if (message instanceof Result) {

				// handle result from the worker
				Result result = (Result) message;
				pi += result.getValue();
				nrOfResults += 1;
				if (nrOfResults == nrOfMessages)
					getContext().stop();

			} else
				throw new IllegalArgumentException("Unknown message ["
						+ message + "]");
		}

		@Override
		public void preStart() {
			start = System.currentTimeMillis();
		}

		@Override
		public void postStop() {
			// tell the world that the calculation is complete
			System.out.println(String.format(
					"\n\tPi estimate: \t\t%s\n\tCalculation time: \t%s millis",
					pi, (System.currentTimeMillis() - start)));
			latch.countDown();
		}
	}

	// ==================
	// ===== Run it =====
	// ==================
	public void calculate(final int nrOfWorkers, final int nrOfElements,
			final int nrOfMessages) throws Exception {

		// this latch is only plumbing to know when the calculation is completed
		final CountDownLatch latch = new CountDownLatch(1);

		// create the master
		ActorRef master = actorOf(new UntypedActorFactory() {
			public UntypedActor create() {
				return new Master(nrOfWorkers, nrOfMessages, nrOfElements,
						latch);
			}
		}).start();

		// start the calculation
		master.tell(new Calculate());

		// wait for master to shut down
		latch.await();
	}
}
