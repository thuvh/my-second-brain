package org.brain2.ws.core.utils;

public class CounterFunctorUtil {
	public static class CounterFunctor {
		private int counter  = 0;
		public CounterFunctor(int counter) {
			this.counter = counter;
		}
		public CounterFunctor() {
			
		}
		public int getCounter() {
			return counter;
		}
		public int hitCounter() {
			this.counter = this.counter+1;
			return counter;
		}
	}
	
	public static CounterFunctor makeCounterFunctor(int initialValue){
		return new CounterFunctor(initialValue);
	}

}
