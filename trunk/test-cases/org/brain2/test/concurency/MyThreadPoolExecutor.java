package org.brain2.test.concurency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.brain2.test.io.HttpClientTest;

class MyThreadPoolExecutor {
	int poolSize = 100;

	int maxPoolSize = 100;

	long keepAliveTime = 20;

	ThreadPoolExecutor threadPool = null;

	final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(50);

	public MyThreadPoolExecutor() {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);

	}

	public void runTask(Runnable task) {
		// System.out.println("Task count.."+threadPool.getTaskCount() );
		// System.out.println("Queue Size before assigning the
		// task.."+queue.size() );
		threadPool.execute(task);
		// System.out.println("Queue Size after assigning the
		// task.."+queue.size() );
		// System.out.println("Pool Size after assigning the
		// task.."+threadPool.getActiveCount() );
		// System.out.println("Task count.."+threadPool.getTaskCount() );
		System.out.println("Task count.." + queue.size());

	}

	public void shutDown() {
		threadPool.shutdown();
	}

	public static void main(String args[]) {
		MyThreadPoolExecutor mtpe = new MyThreadPoolExecutor();
		for (int i = 0; i < 500; i++) {
			mtpe.runTask(new Runnable() {
				public void run() {
					try {
						HttpClientTest.loadTest1();
						 Thread.sleep(1000);
					} catch (Exception e) {
						
					}
				}
			});
		}		
		mtpe.shutDown();
	}

}
